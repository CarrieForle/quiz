package networking;

import java.io.IOException;
import java.net.Socket;

public class ClientMessenger extends Messenger {
    public ClientMessenger(Socket s) throws IOException {
        super(s);
    }

    @Override
    protected void onCommand(String s) throws IOException {
        if (s.equals("ping")) {
            System.out.println("ping");
            this.writeCommand("pong");
        } else if (s.startsWith("message ")) {

        }
    }
}
