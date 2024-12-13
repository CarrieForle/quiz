package quiz;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

import gui.Leaderboard;

import java.util.List;

import networking.ServerStorage;
import networking.Transmitter;
import utils.*;
import utils.exceptions.*;

public class Server implements ServerEventHandler, AutoCloseable {
    private final int MAX_NUM;
    private ServerSocket server_socket;
    // Thread-safe list
    private List<Participant> clients;
    private Thread[] client_threads;
    private Thread quiz_transmission;
    private Thread multiplayer;
    private final Queue<Integer> available_ids = new ArrayDeque<>();
    private static final Path QUIZ_DIRECTORY = Path.of("quiz_questions");
    private ServerStorage storage = new ServerStorage(QUIZ_DIRECTORY);
    private QuestionSet question_set;
    private QuestionWithAnswer running_question;
    private final int MIN_NUM;
    private EventBus eventBus = new EventBus();
    private Lock lock = new ReentrantLock();
    private Condition game_start = lock.newCondition();
    private Condition new_client = lock.newCondition();
    private AtomicBoolean is_game_end = new AtomicBoolean(true);
    private SocketDispatcher dispatcher = new SocketDispatcher(this);
    private Object data;
    private ArrayList<Leaderboard.Player> leaderboard;

    public static void main(String[] args) {
        int port = 12345;
        int min = 2;
        int max = 4;
        
        try (ServerSocket socket = new ServerSocket(port)) {
            if (args.length >= 1) {
                min = Integer.parseInt(args[0]);
            }

            if (args.length >= 2) {
                max = Integer.parseInt(args[1]);
            }

            if (min > max) {
                throw new IllegalArgumentException(String.format("min must be smaller than or equal to max, but %d > %d", min, max));
            }
            
            if (min < 1) {
                throw new IllegalArgumentException("min must be at least 1");
            }

            try (Server server = new Server(socket, min, max)) {
                server.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Argument must be a number. The server terminated.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public Server(ServerSocket server_socket, int min_client, int max_client) throws IOException, InterruptedException {
        this.server_socket = server_socket;
        this.MIN_NUM = min_client;
        this.MAX_NUM = max_client;
        this.clients = Collections.synchronizedList(new ArrayList<>(MAX_NUM));
        this.client_threads = new Thread[MAX_NUM];

        for (int i = 0; i < MAX_NUM; i++) {
            this.available_ids.add(i);
        }

        this.initMultiplayer();
        this.multiplayer.start();
    }

    public QuestionWithAnswer getRunningQuestion() {
        return this.running_question;
    }

    private void freeClient(Participant client) {
        if (client == null) {
            return;
        }

        System.out.format("\n\n%s is disconnected.\n", client.name);

        this.clients.remove(client);
        this.eventBus.unsubscribe(client);

        this.pullBackID(client.id);

        try {
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBus.tryPop();

        if (this.clients.isEmpty()) {
            this.stopAndStartGame();
        }
    }

    private void stopAndStartGame() {
        this.is_game_end.set(true);
        // Use another thread otherwise the client thread will join itself and deadlock.
        Thread t = new Thread(() -> this.stopAndInitMultiplayer());
        t.start();
    }

    private void stopAndInitMultiplayer() {
        System.out.println("Stopping game");

        try {
            this.multiplayer.join();

            for (Thread t : this.client_threads) {
                t.interrupt();
            }

            System.out.println("Game stopped");

            this.initMultiplayer();
            this.multiplayer.start();
        } catch (InterruptedException e) {

        }
    }

    private Participant waitAndInitClient() throws InterruptedException {
        Participant res = null;
        this.lock.lock();

        try {
            while (this.data == null) {
                this.new_client.await();
            }

            synchronized (this.data) {
                res = (Participant) this.data;
                this.data = null;
            }

            res.id = assignID();
        } finally {
            this.lock.unlock();
        }

        return res;
    }

    private void runQuiz() {
        for (Thread t : this.client_threads) {
            t.start();
        }
        
        System.out.println("Server on!");

        try {
            // this.question_set = loadRandomQuestions();
            this.question_set = loadQuestions(QUIZ_DIRECTORY.resolve("程式設計與運算思維 Programming.quiz"));

            System.out.println("Quiz is loaded.");

            this.lock.lock();

            try {
                this.game_start.await();
            } finally {
                this.lock.unlock();
            }

            this.is_game_end.set(false);
            this.eventBus.publish(ClientEvent.GAME_START);
            System.out.println("Game started.");

            List<QuestionWithAnswer> questions = this.question_set.getQuestions();

            for (int i = 0; i < questions.size(); i++) {
                QuestionWithAnswer question = questions.get(i);

                if (this.is_game_end.get()) {
                    return;
                }

                this.running_question = question;
                System.out.println(question.question);
                this.eventBus.publish(ClientEvent.ROUND_START);
                this.eventBus.tryWait();

                synchronized (this.clients) {
                    List<Participant> sorted = this.clients.stream().sorted((a, b) -> a.score - b.score).toList();

                    for (int j = 0; j < sorted.size(); j++) {
                        synchronized (sorted.get(j)) {
                            sorted.get(j).ranking = j + 1;
                        }
                    }
                }

                if (i == questions.size() - 1) {
                    this.eventBus.publish(ClientEvent.FINAL_ROUND_END);
                } else {
                    this.eventBus.publish(ClientEvent.ROUND_END);
                }
            }

            this.updateLeaderboard();
            this.eventBus.publish(ClientEvent.GAME_END);
            this.eventBus.tryWait();
        } catch (CorruptedQuestionsException e) {
            System.out.format("Failed to parse quiz: %s\n", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {

        } finally {
            synchronized (this.clients) {
                while (!this.clients.isEmpty()) {
                    freeClient(this.clients.get(0));
                }
            }
        }
    }

    private void updateLeaderboard() {
        synchronized (this.clients) {
            this.leaderboard = new ArrayList<>(
                this.clients
                    .stream()
                    .sorted((a, b) -> b.ranking - a.ranking)
                    .map(x -> new Leaderboard.Player(x.name, x.score))
                    .toList());
        }
    }

    private void initMultiplayer() {
        this.multiplayer = new Thread(() -> runQuiz());

        for (int i = 0; i < this.client_threads.length; i++) {
            final int thread_id = i;

            this.client_threads[i] = new Thread(() -> {
                Participant client = null;
                try {
                    System.out.format("Thread #%d is ready to serve a client.\n", thread_id);

                    client = waitAndInitClient();
                    client.socket.setSoTimeout(1000);

                    System.out.format("%s is connected and served by thread #%d.\n", client.name, thread_id);

                    this.clients.add(client);
                    this.eventBus.subscribe(client);

                    System.out.format("Thread #%d: %d players\n", thread_id, this.clients.size());

                    // Player leaving detection: If a player left by ordinary mean a read will not block, otherwise it blocks. So the one that goes quickly is the one who left.
                    while (true) {
                        this.lock.lock();

                        try {
                            while (this.clients.size() < MIN_NUM) {
                                this.game_start.await();
                            }

                            this.game_start.signalAll();
                        } finally {
                            this.lock.unlock();
                        }

                        try {
                            client.socket.getInputStream().read();
                            freeClient(client);
                        } catch (SocketTimeoutException e) {
                            Thread.sleep(100);
                            if (this.clients.size() >= MIN_NUM) {
                                break;
                            }
                        }
                    }

                    client.socket.setSoTimeout(15000);
                    this.eventLoop(client);
                } catch (IOException e) {
                    this.freeClient(client);
                } catch (InterruptedException e) {

                }

                System.out.format("Thread #%d ends\n", thread_id);
            });
        }

    }

    private void pullBackID(int id) {
        synchronized (this.available_ids) {
            this.available_ids.add(id);
        }
    }

    // Return null if ran out of IDs.
    private Integer assignID() {
        synchronized (this.available_ids) {
            return this.available_ids.poll();
        }
    }

    private void eventLoop(Participant client) throws IOException {
        System.out.format("Client.%d: %s\n", client.id, client.name);

        while (true) {
            if (this.is_game_end.get()) {
                return;
            }

            if (client.isEventPending()) {
                client.handleEvent();
            }
        }
    }

    public static int calculateScore(QuizAnswerResponse qar, int answer, long question_sending_time) {
        final int MAX_SCORE = 1000;
        final int MAX_REMAINING_TIME = 10000; // milliseconds
        final double RATE = MAX_SCORE / (double) MAX_REMAINING_TIME;

        if (qar.is_correct(answer)) {
            return (int) Math.ceil(RATE * (MAX_REMAINING_TIME - (qar.send_timestamp - question_sending_time)));
        } else {
            return 0;
        }
    }

    // $$<問題>::::<正確答案數字(0, 1, 2, 3)其中一個><換行><Answer0>:::<Answer1>:::<Answer2>:::<Answer3>:::
    public static QuestionSet loadQuestions(Path filepath) throws IOException, CorruptedQuestionsException {
        return new QuestionSet(Files.readString(filepath, StandardCharsets.UTF_8));
    }

    public static QuestionSet loadRandomQuestions() throws IOException, CorruptedQuestionsException {
        Random random = new Random();
        List<Path> paths = Files.list(QUIZ_DIRECTORY).toList();

        return loadQuestions(paths.get(random.nextInt(paths.size())));
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public ArrayList<Leaderboard.Player> getLeaderboard() {
        return this.leaderboard;
    }
    
    private void run() throws IOException, InterruptedException {
        while (true) {
            SocketDispatcher.Data data = this.dispatcher.accept();
            
            this.data = data.data;
            this.lock.lock();

            System.out.format("A new connection of %s\n", data.type);

            try {
                switch (data.type) {
                    case SINGLEPLAYER:
                        if (this.quiz_transmission != null) {
                            this.quiz_transmission.join();
                        }

                        this.quiz_transmission = new Thread(() -> {
                            String filename = (String) this.data;

                            try {
                                if (filename == null || filename.isEmpty()) {
                                    this.storage.sendClientQuizList(data.socket);
                                    System.out.println("Quiz list is sent");
                                } else {
                                    this.storage.sendQuiz(data.socket, filename);
                                    System.out.println("Quiz is sent");
                                }
                            }  catch (IOException e) {
                                System.out.format("Failed to send quiz: %s\n", e.getMessage());
                            }
                        });

                        this.quiz_transmission.start();
                        break;
                    case MULTIPLAYER:
                        Participant incoming = (Participant) this.data;
                        DataOutputStream dos = new DataOutputStream(incoming.socket.getOutputStream());

                        if (!is_game_end.get()) {
                            dos.writeUTF("The game has already started");
                            incoming.socket.close();
                            break;
                        }

                        if (this.clients.size() == MAX_NUM) {
                            dos.writeUTF("The room is full");
                            incoming.socket.close();
                            break;
                        }

                        synchronized (this.clients) {
                            boolean is_name_duplicated = false;

                            for (Participant p : this.clients) {
                                if (incoming.name.equals(p.name)) {
                                    is_name_duplicated = true;
                                    break;
                                }
                            }

                            if (is_name_duplicated) {
                                dos.writeUTF(String.format("A player named \"%s\" is already playing", incoming.name));
                                incoming.socket.close();
                                break;
                            }
                        }

                        dos.writeUTF("OK");
                        this.new_client.signal();
                        break;
                    case QUIZ_UPLOAD:
                        if (this.quiz_transmission != null) {
                            this.quiz_transmission.join();
                        }

                        this.quiz_transmission = new Thread(() -> {
                            try {
                                this.storage.saveQuizToFile(data.socket);
                                System.out.println("New quiz is uploaded");
                            } catch (IOException e) {
                                System.out.format("Failed to receive uploaded quiz: %s\n", e.getMessage());
                            }
                        });

                        this.quiz_transmission.start();
                        break;
                    default:
                        break;
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public Socket accept() throws IOException {
        return this.server_socket.accept();
    }

    @Override
    public void handleEvent(ServerEvent e) {
        switch (e) {
            case GAME_END:
                break;
            case ROUND_END:
                break;
            default:
                break;
        }
    }

    @Override
    public void close() {
        try {
            this.quiz_transmission.join();
        } catch (InterruptedException e) {

        }
    }
}

enum ServerEvent {
    HANDLED,
    GAME_END,
    ROUND_END,
}

enum ClientEvent {
    GAME_START,
    ROUND_START,
    ROUND_END,
    FINAL_ROUND_END,
    GAME_END,
}

interface ServerEventHandler {
    void handleEvent(ServerEvent e);
}

interface ClientEventHandler {
    void setEvent(ClientEvent e);
    void handleEvent() throws IOException;
    boolean isEventPending();
}

class EventBus {
    // Thread-safe list
    private List<ClientEventHandler> subs = Collections.synchronizedList(new ArrayList<>());

    public void subscribe(ClientEventHandler participant) {
        this.subs.add(participant);
    }

    public void unsubscribe(ClientEventHandler participant) {
        this.subs.remove(participant);
    }

    public void publish(ClientEvent e) throws InterruptedException {
        while (!this.popCondition()) {
            synchronized (this.subs) {
                this.subs.wait();
            }
        }

        for (ClientEventHandler sub : this.subs) {
            sub.setEvent(e);
        }
    }

    public void tryPop() {
        if (this.popCondition()) {
            synchronized (this.subs) {
                this.subs.notify();
            }

            synchronized (this) {
                this.notify();
            }
        }
    }

    public void tryWait() throws InterruptedException {
        while (!this.popCondition()) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    private boolean popCondition() {
        synchronized (this.subs) {
            return !this.subs.stream().anyMatch(x -> x.isEventPending());
        }
    }
}

class Participant implements ClientEventHandler {
    final Server server;
    final Socket socket;
    final Transmitter transmitter;
    final String name;
    int id;
    int score = 0;
    int ranking = 1;
    ClientEvent event;

    public Participant(Server server, Socket socket, String name) {
        this.server = server;
        this.socket = socket;
        this.name = name;
        this.transmitter = new Transmitter(socket);
    }

    @Override
    public synchronized void setEvent(ClientEvent e) {
        this.event = e;
    }

    @Override
    public synchronized void handleEvent() throws IOException {
        switch (this.event) {
            case GAME_START:
                System.out.format("The game started!\n");
                this.score = 0;
                break;
            case ROUND_START:
                playRound();
                break;
            case ROUND_END:
                this.transmitter.sendRoundResult(false, this.score, this.ranking);
                break;
            case FINAL_ROUND_END:
                this.transmitter.sendRoundResult(true, this.score, this.ranking);
                break;
            case GAME_END:
                this.transmitter.sendLeaderboard(this.server.getLeaderboard());
            default:
                break;
        }

        this.event = null;
        this.server.getEventBus().tryPop();
    }

    @Override
    public boolean isEventPending() {
        return this.event != null;
    }

    private void playRound() throws IOException {
        QuestionWithAnswer qa = this.server.getRunningQuestion();
        Instant now = Instant.now();

        this.transmitter.sendQuestion(qa, Duration.ofMillis(10000));

        QuizAnswerResponse qar = this.transmitter.getAnswer();

        System.out.format("%s choice: %d ", this.name, qar.choice_id);

        if (qar.is_correct(qa.answer)) {
            System.out.println("O");
        } else {
            System.out.println("X");
        }

        this.score += Server.calculateScore(qar, qa.answer, now.toEpochMilli());
        System.out.format("%s score: %d\n\n", this.name, this.score);
    }
}

class SocketDispatcher {
    public enum Type {
        MULTIPLAYER,
        SINGLEPLAYER,
        QUIZ_UPLOAD,
    }

    public class Data {
        public final Type type;
        public final Socket socket;
        public final Object data;

        private Data(Type type, Socket socket, Object data) {
            this.type = type;
            this.socket = socket;
            this.data = data;
        }
    }
    
    private Server server;
    private Data data;

    public SocketDispatcher(Server server) {
        this.server = server;
    }

    public Data accept() throws IOException {
        Socket socket = this.server.accept();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String identifier = in.readUTF();

        Type type;
        Object data = null;

        if (identifier.startsWith("$")) {
            type = Type.SINGLEPLAYER;
            data = identifier.substring(1);
        } else if (identifier.startsWith("#")) {
            type = Type.QUIZ_UPLOAD;
        } else {
            type = Type.MULTIPLAYER;
            Participant p = new Participant(this.server, socket, identifier);
            data = p;
        }

        this.data = new Data(type, socket, data);

        return this.data;
    }

    public Data getData() {
        return this.data;
    }
}