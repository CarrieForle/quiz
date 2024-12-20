package networking;

import java.io.IOException;
import java.net.Socket;

public class ServerMessenger extends Messenger {
    public ServerMessenger(Socket s) throws IOException {
        super(s);
    }

    @Override
    protected void onCommand(String s) throws IOException {
        if (s.startsWith("message ")) {
            String contents = s.substring(8);
            System.out.println("Nessage: " + contents);
        }
    }
}
