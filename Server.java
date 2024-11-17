import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

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
}

class Client {
    public Socket socket;
    public int id;
    public int score = 0;
}