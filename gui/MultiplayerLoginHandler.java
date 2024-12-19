package gui;

import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;

import utils.Common;

class MultiplayerLoginHandler extends LoginHandler {
    private JFrame parent;

    public MultiplayerLoginHandler(JFrame parent) {
        this.parent = parent;
    }

    @Override
    public void login(LoginDialog dialog, String address, String name) {
        try {
            Socket socket = new Socket(address, 12345);
            parent.dispose();
            dialog.dispose();
            MultiplayerClient client = new MultiplayerClient(socket, name);
        } catch (IOException e) {
            Common.connectionFailedMessage(parent, e);
        }
    }
}