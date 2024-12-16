package quiz;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;
import java.util.List;

import networking.ServerStorage;
import networking.ServerTransmission;
import utils.*;
import utils.exceptions.*;

public class Server implements ServerEventHandler, AutoCloseable {
    private static final int CLIENT_NUM = 4;
    private ServerSocket server_socket;
    // Thread-safe list
    private List<Participant> clients = Collections.synchronizedList(new ArrayList<>(CLIENT_NUM));
    private Thread[] client_threads = new Thread[CLIENT_NUM];
    private Thread quiz_transmission;
    private Thread multiplayer = new Thread(() -> runQuiz());
    private final Queue<Integer> available_ids = new ArrayDeque<>();
    private static final Path QUESTION_DIRECTORY = Path.of("quiz_questions");
    private ServerStorage storage = new ServerStorage(QUESTION_DIRECTORY);
    private QuestionSet question_set;
    private QuestionWithAnswer running_question;
    private static final int MINIMUM_CLIENT_NUM = 2;
    private EventBus eventBus = new EventBus();
    private Lock lock = new ReentrantLock();
    private Condition game_start = lock.newCondition();
    private Condition new_client = lock.newCondition();
    private AtomicBoolean is_game_end = new AtomicBoolean(false);
    private SocketDispatcher dispatcher = new SocketDispatcher(this);
    private Object data;

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket socket = new ServerSocket(port)) {
            try (Server server = new Server(socket)) {
                server.run();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Server(ServerSocket server_socket) throws IOException, InterruptedException {
        this.server_socket = server_socket;
        
        for (int i = 0; i < CLIENT_NUM; i++) {
            this.available_ids.add(i);
        }
        
        this.initMultiplayer();
        this.multiplayer.start();
    }

    public QuestionWithAnswer getRunningQuestion() {
        return this.running_question;
    }

    private void freeClient(Participant client, int thread_id) {
        if (client == null) {
            return;
        }

        System.out.format("\n\nClient.%d is disconnected. Thread #%d is about to be freed.\n", client.id, thread_id);

        this.clients.remove(client);
        this.eventBus.unsubscribe(client);

        this.pullBackID(client.id);

        try {
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventBus.try_pop();

        synchronized (this.clients) {
            if (this.clients.isEmpty()) {
                this.stopAndInitMultiplayer();
            }
        }
    }

    private void stopAndInitMultiplayer() {
        this.is_game_end.set(true);
        System.out.println("Game stopped");

        try {
            this.multiplayer.join();

            for (Thread t : this.client_threads) {
                t.join();
            }
            
            initMultiplayer();
            this.multiplayer = new Thread(() -> runQuiz());
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
            this.question_set = loadQuestions(Path.of("quiz_questions/程式設計與運算思維 Programming.quiz"));

            System.out.println("Quiz is loaded.");

            this.lock.lock();

            try {
                this.game_start.await();
            } finally {
                this.lock.unlock();
            }

            this.eventBus.publish(ClientEvent.GAME_START);
            System.out.println("Game started.");

            for (QuestionWithAnswer question : this.question_set.getQuestions()) {
                synchronized (this.is_game_end) {
                    if (this.is_game_end.get()) {
                        return;
                    }
                }

                this.running_question = question;
                System.out.println(question.question);
                this.eventBus.publish(ClientEvent.ROUND_START);
                System.out.println("Dispatched question");
            }
        } catch (CorruptedQuestionsException e) {
            System.out.format("Failed to parse quiz: %s\n", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            
        }
    }
    
    private void initMultiplayer() {
        for (int i = 0; i < this.client_threads.length; i++) {
            final int thread_id = i;

            this.client_threads[i] = new Thread(() -> {
                Participant client = null;
                boolean exit = false;

                while (!exit) {
                    try {
                        System.out.format("Thread #%d is ready to serve a client.\n", thread_id);

                        client = waitAndInitClient();

                        System.out.format("Client.%d is connected and served by thread #%d.\n", client.id, thread_id);

                        final int client_size;
                        this.clients.add(client);
                        this.eventBus.subscribe(client);
                        System.out.format("Thread #%d: %d players\n", thread_id, this.clients.size());
                        client_size = this.clients.size();

                        this.lock.lock();
                        try {
                            if (client_size < MINIMUM_CLIENT_NUM) {
                                this.game_start.await();
                            } else {
                                this.game_start.signalAll();
                            }
                        } finally {
                            this.lock.unlock();
                        }

                        this.eventLoop(client);
                        exit = true;
                    } catch (IOException e) {
                        this.freeClient(client, thread_id);
                    } catch (InterruptedException e) {

                    }
                }
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
            synchronized (this.is_game_end) {
                if (this.is_game_end.get()) {
                    return;
                }
            }

            synchronized (client) {
                if (client.isEventPending()) {
                    client.handleEvent();
                }
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

    public EventBus getEventBus() {
        return this.eventBus;
    }
    
    private void run() {
        try {
            while (true) {
                SocketDispatcher.Data data = this.dispatcher.accept();
                
                this.data = data.data;
                this.lock.lock();

                System.out.format("A new connection of %s\n", data.type);

                try {
                    switch (data.type) {
                        case SINGLEPLAYER:
                            break;
                        case MULTIPLAYER:
                            this.new_client.signal();
                            break;
                        case QUIZ_UPLOAD:
                            if (this.quiz_transmission != null) {
                                this.quiz_transmission.join();
                            }

                            this.quiz_transmission = new Thread(() -> {
                                try {
                                    this.storage.saveClientDataToFile(data.socket);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public Socket accept() throws IOException {
        return this.server_socket.accept();
    }

    @Override
    public void handleEvent(ServerEvent e) {
        switch (e) {
            case GAME_OVER:
                break;
            case ROUND_OVER:
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
    GAME_OVER,
    ROUND_OVER,
}

enum ClientEvent {
    GAME_START,
    ROUND_START,
    LAST_ROUND_START,
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
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void subscribe(ClientEventHandler participant) {
        subs.add(participant);
    }

    public void unsubscribe(ClientEventHandler participant) {
        subs.remove(participant);
    }

    public void publish(ClientEvent e) throws InterruptedException {
        try {
            this.lock.lock();

            while (!this.popCondition()) {
                this.condition.await();
            }
        } finally {
            this.lock.unlock();
        }

        for (ClientEventHandler sub : subs) {
            synchronized (sub) {
                sub.setEvent(e);
            }
        }
    }

    public void try_pop() {
        this.lock.lock();

        if (this.popCondition()) {
            this.condition.signal();
        }

        this.lock.unlock();
    }

    private boolean popCondition() {
        return !this.subs.stream().anyMatch(x -> x.isEventPending());
    }
}

class Participant implements ClientEventHandler {
    Server server;
    Socket socket;
    String name;
    int id;
    int score = 0;
    ClientEvent e;

    public Participant(Server server) {
        this.server = server;
    }

    public Participant(Server server, Participant controller) {
        this.server = server;
    }

    @Override
    public void setEvent(ClientEvent e) {
        this.e = e;
    }

    @Override
    public void handleEvent() throws IOException {
        switch (this.e) {
            case GAME_START:
                System.out.format("The game started!\n");
                this.score = 0;
                break;
            case ROUND_START:
                playRound(false);
                break;
            case LAST_ROUND_START:
                playRound(true);
                break;
            default:
                break;
        }

        this.e = null;
        this.server.getEventBus().try_pop();
    }

    @Override
    public boolean isEventPending() {
        return this.e != null;
    }

    private void playRound(boolean is_final) throws IOException {
        QuestionWithAnswer qa = this.server.getRunningQuestion();
        Instant now = Instant.now();

        ServerTransmission.transmitQuestion(this.socket.getOutputStream(), qa, Duration.ofMillis(10000));

        QuizAnswerResponse qar = ServerTransmission.receiveAnswer(this.socket.getInputStream());

        System.out.format("%s choice: %d ", this.name, qar.choice_id);

        if (qar.is_correct(qa.answer)) {
            System.out.println("O");
        } else {
            System.out.println("X");
        }

        this.score += Server.calculateScore(qar, qa.answer, now.toEpochMilli());
        System.out.format("%s score: %d\n\n", this.name, this.score);

        ServerTransmission.sendRoundResult(this.socket.getOutputStream(), is_final, score, 1);
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
        } else if (identifier.startsWith("#")) {
            type = Type.QUIZ_UPLOAD;
        } else {
            type = Type.MULTIPLAYER;
            Participant p = new Participant(this.server);
            p.name = identifier;
            p.socket = socket;
            data = p;
        }

        this.data = new Data(type, socket, data);

        return this.data;
    }

    public Data getData() {
        return this.data;
    }
}