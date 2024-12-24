package gui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.JFrame;

import utils.Common;

class MultiplayerLoginHandler extends LoginHandler {
    private JFrame parent;

    public MultiplayerLoginHandler(JFrame parent) {
        this.parent = parent;
    }

    @Override
    public void login(LoginDialog dialog, InetSocketAddress address, String name) {
        try {
            Socket socket = new Socket(address.getHostString(), address.getPort());
            dialog.dispose();
            parent.dispose();
            new MultiplayerClient(socket, name, address);
        } catch (IOException e) {
            Common.connectionFailedMessage(parent, e);
        }
    }
}