package networking;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ServerMessenger extends Messenger {
    List<? extends Messenger> messengers = Collections.emptyList();

    public ServerMessenger(Socket s) throws IOException {
        super(s);
    }

    public void updateMessenger(List<? extends Messenger> messengers) {
        synchronized (this.messengers) {
            this.messengers = messengers;
        }
    }

    @Override
    public void onCommand(String command, String[] args) throws IOException {
        System.out.format("Server got command: %s\n", command);

        if (command.equals("message")) {
            synchronized (this.messengers) {
                for (Messenger m : this.messengers) {
                    m.onCommand("transmit_message", args);
                }
            }
        } else if (command.equals("transmit_message")) {
            this.writeCommand("message", args);
        }
    }
}
