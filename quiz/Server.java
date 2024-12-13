package quiz;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

import networking.ServerTransmission;
import utils.*;
import utils.exceptions.*;

public class Server {
    private static final int CLIENT_NUM = 4;
    private ServerSocket server_socket;
    private List<Participant> clients = new ArrayList<>(CLIENT_NUM);
    private Thread[] client_threads = new Thread[CLIENT_NUM];
    private final Queue<Integer> available_ids = new ArrayDeque<>();
    private static final Path QUESTION_DIRECTORY = Path.of("quiz_questions");
    private boolean is_in_game = false;
    private ServerStorage storage_manager = new ServerStorage();
    
    private class Participant {
        Socket socket;
        String name;
        int id;
        int score = 0;
    }

    public static void main(String[] args) {
        int port = 12345;

        try {
            ServerSocket socket = new ServerSocket(port);
            Server server = new Server(socket);
            server.run();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Server(ServerSocket server_socket) throws IOException {
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
                        client = wait_and_init_client();
                        System.out.format("Client.%d is connected and served by thread #%d.\n", client.id, thread_id);

                        synchronized (this.clients) {
                            this.clients.add(client);
                        }

                        eventLoop(client);
                        exit = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        
                        if (client == null)  {
                            continue;
                        }

                        System.out.format("\n\nClient.%d is disconnected. Thread #%d is about to be freed.\n", client.id, thread_id);

                        synchronized (this.clients) {
                            this.clients.remove(client);
                        }

                        pullBackID(client.id);
                        client = null;
                    }
                }
            });
        }
    }

    private Participant wait_and_init_client() throws IOException {
        Participant client = new Participant();
        client.socket = this.server_socket.accept();
        client.id = assignID();

        return client;
    }

    private void run() {
        for (Thread t : this.client_threads) {
            t.start();
        }

        System.out.println("Server on!");

        while (true) {

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

        QuestionSet qs = new QuestionSet();

        try {
            qs = loadQuestions(Path.of("quiz_questions/網際網路概論.quiz"));
        } catch (Exception e) {

        }

        System.out.format("Question is loaded.\n");
        System.out.format("The game started!\n");

        int score = 0;
        List<QuestionWithAnswer> questions = qs.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            QuestionWithAnswer qa = questions.get(i);
            Instant now = Instant.now();
        
            System.out.println(i + ". " + qa.question);
            ServerTransmission.transmitQuestion(client.socket.getOutputStream(), qa, now);

            System.out.println(now.toEpochMilli());

            QuizAnswerResponse qar = ServerTransmission.receiveAnswer(client.socket.getInputStream());

            System.out.format("Participant choice: %d\n", qar.choice_id);
            System.out.format("Participant remainig time: %d\n", qar.send_timestamp);

            if (qar.is_correct(qa.answer)) {
                System.out.format("Correct!\n");
            } else {
                System.out.format("Incorrect!\n");
            }

            score += calculateScore(qar, qa.answer, now.toEpochMilli());
            System.out.format("Participant score: %d\n", score);

            System.out.println();

            ServerTransmission.sendRoundResult(client.socket.getOutputStream(), false, score, 1);
        }

        ServerTransmission.sendRoundResult(client.socket.getOutputStream(), true, 1450, 2);
    }

    private static int calculateScore(QuizAnswerResponse qar, int answer, long question_sending_time) {
        final int MAX_SCORE = 1000;
        final int MAX_REMAINING_TIME = 10000; // milliseconds
        final double RATE = MAX_SCORE / (double) MAX_REMAINING_TIME;

        if (qar.is_correct(answer)) {
            return (int) Math.ceil(RATE * (MAX_REMAINING_TIME -(qar.send_timestamp - question_sending_time) * 0.001));
        } else {
            return 0;
        }
    }

    // $$<問題>::::<正確答案數字(0, 1, 2, 3)其中一個><換行><Answer0>:::<Answer1>:::<Answer2>:::<Answer3>:::
    public static QuestionSet loadQuestions(Path filepath) throws IOException, CorruptedQuestionsException {
        return new QuestionSet(Files.readString(filepath, StandardCharsets.UTF_8));
    }
}