import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

import networking.ServerTransmission;
import utils.QuizAnswerResponse;

public class Server {
    private static final int CLIENT_NUM = 4;
    private ServerSocket server_socket;
    private Client[] clients = new Client[CLIENT_NUM];
    private Thread[] client_threads = new Thread[CLIENT_NUM];
    private final Queue<Integer> available_ids = new ArrayDeque<>();

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

    public Server(ServerSocket server_socket) {
        this.server_socket = server_socket;

        for (int i = 0; i < CLIENT_NUM; i++) {
            this.available_ids.add(i);
        }

        for (int i = 0; i < this.client_threads.length; i++) {
            Client client = new Client();
            this.clients[i] = client;

            this.client_threads[i] = new Thread(() -> {
                try {
                    System.out.println("Ready to serve a client.");
                    client.socket = this.server_socket.accept();
                    client.id = assignID();
                    System.out.println("A client has connected");
                    event_loop(client);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        // Thread t = new Thread(() -> {
        //     try {
        //         Socket s = new Socket();
        //         s.connect(new InetSocketAddress("192.168.0.141", 12345));
        //     } catch (IOException ex) {
        //         ex.printStackTrace();
        //     }
        // });

        // t.start();
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
        this.available_ids.add(id);
    }

    // Return null if ran out of IDs.
    private Integer assignID() {
        synchronized (this.available_ids) {
            return this.available_ids.poll();
        }
    }

    private void event_loop(Client client) throws IOException {
        String name = ServerTransmission.receiveName(client.socket.getInputStream());

        ServerTransmission.transmitQuestion(client.socket.getOutputStream(), "Q今天星期幾？a3\nA星期一\nA星期二\nA賈伯斯\nA星\n期日\n");

        QuizAnswerResponse qar = ServerTransmission.receiveAnswer(client.socket.getInputStream());

        System.out.format("Client choice: %d\n", qar.choice_id);
        System.out.format("Client remainig time: %d\n", qar.remaining_time);

        ServerTransmission.sendRoundResult(client.socket.getOutputStream(), true, 1450, 2);
    }

    // TODO
    private int caculateScore(int remaining_time) {
        return 100;
    }

    // $$<問題>::::<正確答案數字(0, 1, 2, 3)其中一個><換行><Answer0>:::<Answer1>:::<Answer2>:::<Answer3>:::
    private static QuestionSet loadQuestions(Path filepath) throws IOException, CorruptedQuestionsException {
        StringBuilder contents = new StringBuilder(Files.readString(filepath, Charset.forName("UTF-8")));
        QuestionSet res = new QuestionSet();

        res.name = popUntil(contents, "\n");

        while (contents.length() > 0) {
            System.out.println(contents.length());
            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException("Expected token `\\n`");
            }

            contents.delete(0, 1);

            if (!contents.substring(0, 2).equals("$$")) {
                throw new CorruptedQuestionsException("Expected token `$$`");
            }

            contents.delete(0, 2);



            Question question = new Question();
            question.question = popUntil(contents, "::::");
            question.answer = Integer.parseInt(contents.substring(0, 1));
            contents.delete(0, 1);
            
            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException("Expected token `\\n`");
            }

            contents.delete(0, 1);



            for (int i = 0; i < 4; i++) {
                question.setOptions(i, popUntil(contents, ":::"));
            }

            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException("Expected token `\\n`");
            }

            contents.delete(0, 1);



            res.getQuestions().add(question);
        }

        return res;
    }
    
    private static String popUntil(StringBuilder sb, String delimiter) {
        int delimiter_pos = sb.indexOf(delimiter);
        String res = sb.substring(0, delimiter_pos);
        sb.delete(0, delimiter_pos + delimiter.length());

        return res;
    }
}

class CorruptedQuestionsException extends Exception {
    public CorruptedQuestionsException(String s) {
        super(s);
    }
}

class QuestionSet {
    public String name;
    private List<Question> questions = new ArrayList<>();

    public List<Question> getQuestions() {
        return questions;
    }
}

class Question {
    public String question;
    private String[] options = new String[4];
    public int answer = -1;

    public void setOptions(int i, String s) {
        this.options[i] = s;
    }

    public void setOptions(String[] s) {
        for (int i = 0; i < this.options.length; i++) {
            this.options[i] = s[i];
        }
    }

    public String getOption(int i) {
        return options[i];
    }

    public int getOptionLength() {
        return this.options.length;
    }
}

class Client {
    public Socket socket;
    public int id;
    public int score = 0;
}