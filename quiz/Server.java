package quiz;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

import java.util.List;

import networking.ServerTransmission;
import utils.*;
import utils.exceptions.*;

public class Server implements ServerEventHandler {
    private static final int CLIENT_NUM = 4;
    private ServerSocket server_socket;
    private List<Participant> clients = new ArrayList<>(CLIENT_NUM);
    private Thread[] client_threads = new Thread[CLIENT_NUM];
    private final Queue<Integer> available_ids = new ArrayDeque<>();
    private static final Path QUESTION_DIRECTORY = Path.of("quiz_questions");
    private boolean is_in_game = false;
    private ServerStorage storage_manager = new ServerStorage();
    private QuestionSet question_set;
    private QuestionWithAnswer running_question;
    private static final int MINIMUM_CLIENT_NUM = 2;
    private EventBus eventBus = new EventBus();
    private Lock lock = new ReentrantLock();
    private Condition game_start = lock.newCondition();

    public static void main(String[] args) {
        int port = 12345;

        try {
            ServerSocket socket = new ServerSocket(port);
            Server server = new Server(socket);
            server.run();
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

        if (Files.notExists(QUESTION_DIRECTORY)) {
            Files.createDirectory(QUESTION_DIRECTORY);
        }

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
                        synchronized (this.clients) {
                            this.clients.add(client);
                            this.eventBus.subscribe(client);
                            System.out.format("Thread #%d: %d players\n", thread_id, this.clients.size());
                            client_size = this.clients.size();
                        }

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
                    } catch (SocketException e) {
                        this.freeClient(client, thread_id);
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.freeClient(client, thread_id);
                    } catch (InterruptedException e) {

                    }
                }
            });
        }
    }

    public QuestionWithAnswer getRunningQuestion() {
        return this.running_question;
    }

    private void freeClient(Participant client, int thread_id) {
        if (client == null) {
            return;
        }

        System.out.format("\n\nClient.%d is disconnected. Thread #%d is about to be freed.\n", client.id, thread_id);

        synchronized (this.clients) {
            this.clients.remove(client);
        }

        this.pullBackID(client.id);
    }

    private Participant waitAndInitClient() throws IOException {
        Participant client = new Participant(this);
        client.socket = this.server_socket.accept();
        client.id = assignID();

        return client;
    }

    private void run() throws InterruptedException, IOException {
        for (Thread t : this.client_threads) {
            t.start();
        }

        System.out.println("Server on!");

        try {
            this.question_set = loadQuestions(Path.of("quiz_questions/程式設計與運算思維 Programming.quiz"));

            System.out.println("Question is loaded.");
            
            this.lock.lock();

            try {
                this.game_start.await();
            } finally {
                this.lock.unlock();
            }
            
            this.is_in_game = true;
            this.eventBus.publish(ClientEvent.GAME_START);
            System.out.println("Main thread: Game started.");
            System.out.println("Start dispatching question.");

            for (QuestionWithAnswer question : this.question_set.getQuestions()) {
                this.running_question = question;
                this.eventBus.publish(ClientEvent.ROUND_START);
                System.out.println("Dispatched question");
            }
        } catch (CorruptedQuestionsException e) {
            System.out.format("Failed to parse quiz: %s\n", e.getMessage());
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
        client.name = ServerTransmission.receiveName(client.socket.getInputStream());

        System.out.format("Receive client.%d's name: %s\n", client.id, client.name);

        while (true) {
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

    @Override
    public void handleEvent(ServerEvent e) {
        switch (e) {
            case ServerEvent.GAME_OVER:
                break;
            case ServerEvent.ROUND_OVER:
                break;
            default:
                break;
        }
    }

    public EventBus getEventBus() {
        return this.eventBus;
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
    private List<ClientEventHandler> subs = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void subscribe(ClientEventHandler participant) {
        subs.add(participant);
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
            case ClientEvent.GAME_START:
                System.out.format("The game started!\n");

                this.score = 0;
                break;
            case ClientEvent.ROUND_START:
                playRound(false);
                break;
            case ClientEvent.LAST_ROUND_START:
                playRound(true);
                break;
            default:
                break;
        }

        this.e = null;

        EventBus eventBus = this.server.getEventBus();

        synchronized (eventBus) {
            eventBus.try_pop();
        }
    }

    @Override
    public boolean isEventPending() {
        return this.e != null;
    }

    private void playRound(boolean is_final) throws IOException {
        QuestionWithAnswer qa = this.server.getRunningQuestion();
        Instant now = Instant.now();

        System.out.format("%s\n", qa.question);
        ServerTransmission.transmitQuestion(this.socket.getOutputStream(), qa, Duration.ofMillis(10000));

        System.out.format("They have %d ms to answer\n", 10000);

        QuizAnswerResponse qar = ServerTransmission.receiveAnswer(this.socket.getInputStream());

        System.out.format("%s choice: %d\n", this.name, qar.choice_id);
        System.out.format("Question Time: %d\n", now.toEpochMilli());
        System.out.format("%s remainig time: %d\n", this.name, qar.send_timestamp);

        if (qar.is_correct(qa.answer)) {
            System.out.format("Correct!\n");
        } else {
            System.out.format("Incorrect!\n");
        }

        this.score += Server.calculateScore(qar, qa.answer, now.toEpochMilli());
        System.out.format("Participant score: %d\n\n", this.score);

        ServerTransmission.sendRoundResult(this.socket.getOutputStream(), is_final, score, 1);
    }
}