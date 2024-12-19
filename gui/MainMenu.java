package gui;

import java.io.IOException;
import java.net.Socket;

import javax.swing.*;

import utils.Common;

public class MainMenu {
    JFrame menuFrame;

    public static void main(String[] args) {
        new MainMenu();
    }

    public MainMenu() {
        menuFrame = new JFrame("Game Menu");
        menuFrame.setSize(400, 300);
        menuFrame.setLayout(null);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setResizable(false);
        menuFrame.setIconImage(Resource.icon.getImage());

        // Single Player Mode Button
        JButton singleplayerButton = new JButton("Singleplayer");
        singleplayerButton.setBounds(130, 50, 140, 30);
        menuFrame.add(singleplayerButton);
        singleplayerButton.addActionListener(e -> {
            SingleplayerClient.runQuizDialog(menuFrame);
        });

        // Multiplayer Mode Button
        JButton multiplayerButton = new JButton("Multiplayer");
        multiplayerButton.setBounds(130, 100, 140, 30);
        menuFrame.add(multiplayerButton);
        multiplayerButton.addActionListener(e -> { 
            new LoginDialog(menuFrame, new MultiplayerLogin(menuFrame));
        });

        // Ask Question Button
        JButton askQuestionButton = new JButton("Create Quiz");
        askQuestionButton.setBounds(130, 150, 140, 30);
        menuFrame.add(askQuestionButton);
        askQuestionButton.addActionListener(e -> {
            MakeQuizFrame f = new MakeQuizFrame();
            menuFrame.dispose();
        });

        // Exit Button
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(130, 200, 140, 30);
        menuFrame.add(exitButton);
        exitButton.addActionListener(e1 -> System.exit(0));

        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }
}

class MultiplayerLogin extends LoginHandler {
    private JFrame parent;

    public MultiplayerLogin(JFrame parent) {
        this.parent = parent;
    }

    @Override
    public void login(LoginDialog dialog, String address, String name) {  
        try {
            Socket socket = new Socket(address, 12345);
            parent.dispose();
            MultiplayerClient client = new MultiplayerClient(socket, name);
            dialog.dispose();
        } catch (IOException e) {
            Common.connectionFailedMessage(parent, e);
        }
    }
}