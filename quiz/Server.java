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
import java.util.concurrent.atomic.AtomicReference;

import gui.Leaderboard;

import java.util.List;

import networking.*;
import utils.*;
import utils.exceptions.*;

public class Server implements ServerEventHandler, AutoCloseable {
    private static final Path QUIZ_DIRECTORY = Path.of("quiz_questions");
    public final int MAX_NUM;
    public final int MIN_NUM;
    public final Duration TIME_FRAME = Duration.ofSeconds(30);
    public final Duration FULL_TIME_FRAME = Duration.ofSeconds(10);
    private final Object new_client = new Object();
    private final Object is_ready = new Object();
    private Duration countDown = TIME_FRAME;
    private ServerSocket server_socket;
    private List<Participant> clients;
    private Thread[] client_threads;
    private Thread quiz_transmission = new Thread();
    private Thread multiplayer = new Thread();
    private ServerStorage storage = new ServerStorage(QUIZ_DIRECTORY);
    private QuestionSet quiz;
    private AtomicReference<QuestionWithAnswer> running_question = new AtomicReference<>();
    private EventBus eventBus = new EventBus();
    private AtomicBoolean is_before_game = new AtomicBoolean(true);
    private AtomicBoolean is_game_end = new AtomicBoolean(false);
    private SocketDispatcher dispatcher = new SocketDispatcher(this);
    private AtomicReference<Object> data = new AtomicReference<>();
    private AtomicReference<ArrayList<Leaderboard.Player>> leaderboard = new AtomicReference<>();

    public static void main(String[] args) {
        int port = 12345;
        int min = 1;
        int max = 1;

        try (ServerSocket socket = new ServerSocket(port)) {
            if (args.length >= 1 && !args[0].isEmpty()) {
                min = Integer.parseInt(args[0]);
            }

            if (args.length >= 2 && !args[1].isEmpty()) {
                max = Integer.parseInt(args[1]);
            }

            if (args.length >= 3 && !args[2].isEmpty()) {
                port = Integer.parseInt(args[2]);
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
            System.out.println("An error happened during initialization");
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
        System.out.format("Minimum player count: %d\n", min_client);
        System.out.format("Maximum player count: %d\n", max_client);
        System.out.format("Running on port: %d\n", server_socket.getLocalPort());
        this.server_socket = server_socket;
        this.MIN_NUM = min_client;
        this.MAX_NUM = max_client;
        this.clients = Collections.synchronizedList(new ArrayList<>(MAX_NUM));
        this.client_threads = new Thread[MAX_NUM];

        this.initMultiplayer();
        this.multiplayer.start();
    }

    QuestionWithAnswer getRunningQuestion() {
        return this.running_question.get();
    }

    private void freeClient(Participant client) {
        if (client == null) {
            return;
        }

        if (!this.clients.remove(client)) {
            return;
        }

        this.eventBus.unsubscribe(client);
        System.out.format("%s is disconnected.\n", client.name);
        this.updateMessenger(client);

        try {
            client.transmitter.close();

            synchronized (this.clients) {
                for (Participant p : this.clients) {
                    p.transmitter.leave(client.name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBus.tryPop();

        if (!this.is_before_game.get() && this.clients.isEmpty()) {
            this.stopAndStartGame();
        }
    }

    private void stopAndStartGame() {
        this.is_game_end.set(true);
        this.is_before_game.set(true);
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
            is_game_end.set(false);

            this.initMultiplayer();
            this.multiplayer.start();
        } catch (InterruptedException e) {

        }
    }

    private Participant waitAndInitClient() throws InterruptedException {
        Participant res = null;

        while (this.data.get() == null) {
            synchronized (this.new_client) {
                this.new_client.wait();
            }
        }

        res = (Participant) this.data.get();
        this.data.set(null);

        res.id = Thread.currentThread().threadId();

        return res;
    }

    private void runQuiz() {
        for (Thread t : this.client_threads) {
            t.start();
        }

        System.out.println("Server on!");

        try {
            // this.quiz = loadRandomQuestions();
            
            this.quiz = loadQuestions(QUIZ_DIRECTORY.resolve("lol.quiz"));
            System.out.format("lol.quiz is loaded");
            this.waitForEnoughPlayer();
            this.is_before_game.set(false);
            this.eventBus.publish(ClientEvent.GAME_START);
            System.out.println("Game started.");

            List<QuestionWithAnswer> questions = this.quiz.getQuestions();

            for (int i = 0; i < questions.size(); i++) {
                QuestionWithAnswer question = questions.get(i);

                if (this.is_game_end.get()) {
                    return;
                }

                // Wait until participant transmit correct answer;
                this.eventBus.tryWait();
                this.running_question.set(question);
                System.out.println(question.question);
                this.eventBus.publish(ClientEvent.ROUND_START);
                this.eventBus.tryWait();

                synchronized (this.clients) {
                    List<Participant> sorted = this.clients.stream().sorted((a, b) -> b.score - a.score).toList();

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

                Thread.sleep(4000);
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
            this.leaderboard.set(new ArrayList<>(
                this.clients
                    .stream()
                    .sorted((a, b) -> b.score - a.score)
                    .map(x -> new Leaderboard.Player(x.name, x.score))
                    .toList()));
        }
    }
    
    private void initMultiplayer() {
        this.multiplayer = new Thread(() -> runQuiz());

        for (int i = 0; i < this.client_threads.length; i++) {
            this.client_threads[i] = new Thread(() -> {
                Participant client = null;
                final long threadId = Thread.currentThread().threadId();

                try {
                    System.out.format("Thread #%d is ready to serve a client.\n", Thread.currentThread().threadId());

                    client = waitAndInitClient();
                    client.transmitter.getMessenger().getSocket().setSoTimeout(15000);

                    System.out.format("%s is connected and served by thread #%d.\n", client.name, threadId);
                    
                    synchronized (this.clients) {
                        for (Participant p : this.clients) {
                            p.transmitter.join(client.name);
                        }
                    }

                    this.eventBus.subscribe(client);
                    this.clients.add(client);

                    synchronized (this.clients) {
                        for (Participant p : this.clients) {
                            this.updateMessenger(p);
                        }
                    }

                    if (this.clients.size() > MIN_NUM) {
                        client.transmitter.startIn(this.countDown);
                    }

                    System.out.format("%d/%d (min: %d)\n", this.clients.size(), MAX_NUM, MIN_NUM);

                    this.eventLoop(client);
                } catch (IOException e) {
                    this.freeClient(client);
                } catch (InterruptedException e) {

                }

                System.out.format("Thread #%d ends\n", threadId);
            });
        }

    }

    private void eventLoop(Participant client) throws IOException, InterruptedException {
        while (!this.is_game_end.get() || this.is_before_game.get()) {
            if (client.isEventPending()) {
                synchronized (this.clients) {
                    synchronized (this.is_ready) {
                        if (this.clients.stream().allMatch(x -> x.isReady())) {
                            this.is_ready.wait();       
                        } else {
                            this.is_ready.notifyAll();
                        }
                    }
                }

                client.handleEvent();
            } else {
                client.transmitter.ping();
                client.transmitter.getMessenger().readIncoming();
                Thread.sleep(100);
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
        Path path = paths.get(random.nextInt(paths.size()));
        System.out.format("Load random quiz: %s\n", path.toFile().getName());

        return loadQuestions(path);
    }

    EventBus getEventBus() {
        return this.eventBus;
    }

    ArrayList<Leaderboard.Player> getLeaderboard() {
        return this.leaderboard.get();
    }
    
    private void run() throws InterruptedException {
        while (true) {
            try {
                SocketDispatcher.Data data = this.dispatcher.accept();
    
                this.data.set(data.data);
    
                System.out.format("A new connection of %s\n", data.type);
    
                switch (data.type) {
                    case SINGLEPLAYER:
                        this.quiz_transmission.join();
    
                        this.quiz_transmission = new Thread(() -> {
                            String filename = (String) this.data.get();
    
                            try {
                                if (filename == null || filename.isEmpty()) {
                                    this.storage.sendClientQuizList(data.socket);
                                    System.out.println("Quiz list is sent");
                                } else {
                                    this.storage.sendQuiz(data.socket, filename);
                                    System.out.format("%s is sent", filename);
                                }
                            } catch (IOException e) {
                                System.out.format("Failed to send quiz: %s\n", e.getMessage());
                            }
                        });
    
                        this.quiz_transmission.start();
                        break;
                    case MULTIPLAYER:
                        Participant incoming = (Participant) this.data.get();
    
                        if (!is_before_game.get()) {
                            incoming.transmitter.getMessenger().writeUTF("The game has already started");
                            incoming.transmitter.close();
                            break;
                        }
    
                        if (this.clients.size() == MAX_NUM) {
                            incoming.transmitter.getMessenger().writeUTF("The room is full");
                            incoming.transmitter.close();
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
                                incoming.transmitter.getMessenger().writeUTF(String.format("A player named \"%s\" is already playing", incoming.name));
                                incoming.transmitter.close();
                                break;
                            }
                        }
    
                        incoming.transmitter.getMessenger().writeUTF("OK");
    
                        synchronized (this.new_client) {
                            this.new_client.notify();
                        }
    
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
            } catch (IOException e) {
                System.out.format("Error on handling incoming connection: %s\n", e);
            }
        }
    }
    
    private void waitForEnoughPlayer() throws IOException, InterruptedException {
        if (MIN_NUM == MAX_NUM) {
            while (true) {
                if (this.clients.size() >= MAX_NUM) {
                    this.countDown = FULL_TIME_FRAME;
                    this.eventBus.publish(ClientEvent.FULL_PLAYER);

                    Instant t = Instant.now();

                    boolean is_player_enough = true;

                    while (this.countDown.compareTo(Duration.ZERO) > 0) {
                        if (this.clients.size() < MAX_NUM) {
                            is_player_enough = false;
                            break;
                        }

                        this.countDown = this.countDown.minus(Duration.between(t, Instant.now()));
                        t = Instant.now();
                        Thread.sleep(10);
                    }

                    if (is_player_enough) {
                        break;
                    } else {
                        this.eventBus.publish(ClientEvent.NOT_ENOUGH_PLAYER);
                    }
                }
            }
        } else {
            while (true) {
                if (this.clients.size() >= MIN_NUM) {
                    this.countDown = TIME_FRAME;
                    this.eventBus.publish(ClientEvent.ENOUGH_PLAYER);

                    Instant t = Instant.now();

                    boolean is_player_enough = true;
                    boolean published_full_player = false;

                    while (this.countDown.compareTo(Duration.ZERO) > 0) {
                        if (this.clients.size() < MIN_NUM) {
                            is_player_enough = false;
                            break;
                        } else if (this.clients.size() >= MAX_NUM) {
                            if (!published_full_player) {
                                if (this.countDown.compareTo(FULL_TIME_FRAME) >= 0) {
                                    this.countDown = FULL_TIME_FRAME;
                                    this.eventBus.publish(ClientEvent.FULL_PLAYER);
                                    published_full_player = true;
                                }
                            }
                        } else {
                            published_full_player = false;
                        }

                        this.countDown = this.countDown.minus(Duration.between(t, Instant.now()));
                        t = Instant.now();
                        Thread.sleep(10);
                    }

                    if (is_player_enough) {
                        break;
                    } else {
                        this.eventBus.publish(ClientEvent.NOT_ENOUGH_PLAYER);
                    }
                }

                Thread.sleep(10);
            }
        }
    }

    Socket accept() throws IOException {
        return this.server_socket.accept();
    }

    void updateMessenger(Participant client) {
        synchronized (this.clients) {
            client.transmitter.updateMessenger(this.clients.stream().map(x -> x.transmitter.getMessenger()).toList());
        }
    }

    QuestionSet getQuiz() {
        return this.quiz;
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
    FULL_PLAYER,
    ENOUGH_PLAYER,
    NOT_ENOUGH_PLAYER,
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
    final Transmitter transmitter;
    final String name;
    long id;
    int score = 0;
    int ranking = 1;
    private AtomicBoolean is_ready = new AtomicBoolean(false);
    ClientEvent event;

    public Participant(Server server, ServerMessenger messenger, String name) {
        this.server = server;
        this.name = name;
        this.transmitter = new Transmitter(messenger);
    }

    @Override
    public synchronized void setEvent(ClientEvent e) {
        this.event = e;
    }

    @Override
    public synchronized void handleEvent() throws IOException {
        switch (this.event) {
            case FULL_PLAYER:
                this.transmitter.startIn(server.FULL_TIME_FRAME);
                break;
            case ENOUGH_PLAYER:
                this.transmitter.startIn(server.TIME_FRAME);
                break;
            case NOT_ENOUGH_PLAYER:
                this.transmitter.notEnough(server.MIN_NUM);
                break;
            case GAME_START:
                this.transmitter.sendQuizInfo(server.getQuiz());
                break;
            case ROUND_START:
                playRound();
                break;
            case ROUND_END:
                this.transmitter.sendRoundResult(false, this.server.getRunningQuestion().answer, this.score, this.ranking);
                break;
            case FINAL_ROUND_END:
                this.transmitter.sendRoundResult(true, this.server.getRunningQuestion().answer, this.score, this.ranking);
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

    public boolean isReady() {
        return this.is_ready.get();
    }

    private void playRound() throws IOException {
        QuestionWithAnswer qa = this.server.getRunningQuestion();
        Instant now = Instant.now();

        this.transmitter.sendQuestion(qa, Duration.ofSeconds(10));

        QuizAnswerResponse qar = this.transmitter.getAnswer();

        System.out.format("%s choice: %d ", this.name, qar.choice_id);

        if (qar.is_correct(qa.answer)) {
            System.out.println("O");
        } else {
            System.out.println("X");
        }

        this.score += Server.calculateScore(qar, qa.answer, now.toEpochMilli());
        System.out.format("%s score: %d\n", this.name, this.score);
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
        Socket socket;
        DataInputStream in;
        String identifier = "";
        
        while (true) {
            try {
                socket = this.server.accept();
                int oldTimeout = socket.getSoTimeout();
                socket.setSoTimeout(10000);
                in = new DataInputStream(socket.getInputStream());
                identifier = in.readUTF();
                socket.setSoTimeout(oldTimeout);
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("An incoming connection timed out");
            } catch (IOException e) {
                System.out.format("Incoming Socket Exception: %s\n", e);
            }
        }

        Type type;
        Object data = null;

        if (identifier.startsWith("$")) {
            type = Type.SINGLEPLAYER;
            data = identifier.substring(1);
        } else if (identifier.startsWith("#")) {
            type = Type.QUIZ_UPLOAD;
        } else {
            type = Type.MULTIPLAYER;
            Participant p = new Participant(this.server, new ServerMessenger(socket), identifier);
            data = p;
        }

        this.data = new Data(type, socket, data);

        return this.data;
    }

    public Data getData() {
        return this.data;
    }
}