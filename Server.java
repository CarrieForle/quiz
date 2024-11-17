import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private ServerSocket server_socket;

    public static void main(String[] args) {
        int port = 12345;

        try {
            Server server = new Server(new ServerSocket(port));
            server.run();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Server(ServerSocket server_socket) {
        this.server_socket = server_socket;
    }
    
    public void run() {

    }
}
