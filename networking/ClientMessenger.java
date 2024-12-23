package networking;

import java.io.IOException;
import java.net.Socket;

import gui.MultiplayerClient;

public class ClientMessenger extends Messenger {
    MultiplayerClient client;

    public ClientMessenger(Socket s, MultiplayerClient client) throws IOException {
        super(s);
        this.client = client;
    }

    @Override
    public void onCommand(String command, String[] args) throws IOException {
        if (command.equals("ping")) {
            this.writeCommand("pong");
        } else if (command.equals("message")) {
            String name = args[0];
            String message = args[1];

            client.addChat(name, message);
        } else if (command.equals("join")) {
            String name = args[0];
            client.joinChat(name);
        } else if (command.equals("leave")) {
            String name = args[0];
            client.leaveChat(name);
        } else if (command.equals("notenough")) {
            int minimum = Integer.parseInt(args[0]);
            client.notEnoughPlayer();
        } else if (command.equals("start")) {
            int start_in = Integer.parseInt(args[0]);
            client.startIn(start_in);
        }
    }
}
