package quiz;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

import networking.ServerTransmission;
import utils.*;

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

        Question sample_question = new Question();

        sample_question.question = "1+1等於幾？";
        sample_question.setOptions(new String[] {
                "2", "4", "-5", "19"
        });

        ServerTransmission.transmitQuestion(client.socket.getOutputStream(), sample_question);

        QuizAnswerResponse qar = ServerTransmission.receiveAnswer(client.socket.getInputStream());

        System.out.format("Participant choice: %d\n", qar.choice_id);
        System.out.format("Participant remainig time: %d\n", qar.remaining_time);

        ServerTransmission.sendRoundResult(client.socket.getOutputStream(), true, 1450, 2);
    }

    private static int caculateScore(int remaining_time) {
        final int MAX_SCORE = 1000;
        final int MAX_REMAINING_TIME = 10000; // milliseconds
        final double RATE = MAX_SCORE / (double) MAX_REMAINING_TIME;

        return (int) Math.ceil(RATE * remaining_time);
    }

    // $$<問題>::::<正確答案數字(0, 1, 2, 3)其中一個><換行><Answer0>:::<Answer1>:::<Answer2>:::<Answer3>:::
    public static QuestionSet loadQuestions(Path filepath) throws IOException, CorruptedQuestionsException {
        return new QuestionSet(Files.readString(filepath, StandardCharsets.UTF_8));
    }
}

class CorruptedQuestionsException extends Exception {
    public CorruptedQuestionsException(String s) {
        super(s);
    }
}

class QuestionSet {
    public String name;
    private List<QuestionWithAnswer> questions = new ArrayList<>();

    public List<QuestionWithAnswer> getQuestions() {
        return questions;
    }

    public QuestionSet() {

    }

    public QuestionSet(String s) throws CorruptedQuestionsException {
        StringBuilder contents = new StringBuilder(s);

        this.name = popUntil(contents, "\n");

        while (contents.length() > 0) {
            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
                
            }

            contents.delete(0, 1);

            if (!contents.substring(0, 2).equals("$$")) {
                throw new CorruptedQuestionsException(String.format("Expected token `$$`. Found `%s`", contents.substring(0, 2)));
            }

            contents.delete(0, 2);

            QuestionWithAnswer question = new QuestionWithAnswer();
            question.question = popUntil(contents, "::::");
            question.answer = Integer.parseInt(contents.substring(0, 1));
            contents.delete(0, 1);

            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
            }

            contents.delete(0, 1);

            for (int i = 0; i < 4; i++) {
                question.setOptions(i, popUntil(contents, ":::"));
            }

            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
            }

            contents.delete(0, 1);

            this.questions.add(question);
        }
    }

    private static String popUntil(StringBuilder sb, String delimiter) {
        int delimiter_pos = sb.indexOf(delimiter);
        String res = sb.substring(0, delimiter_pos);
        sb.delete(0, delimiter_pos + delimiter.length());

        return res;
    }
}

class QuestionWithAnswer extends Question {
    public int answer = -1;
}