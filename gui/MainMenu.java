package gui;

import java.io.IOException;
import java.net.Socket;

import javax.swing.*;

public class MainMenu {
    JFrame menuFrame;
    private static ImageIcon icon;

    public static void main(String[] args) {
        new MainMenu();
    }

    public MainMenu() {
        this.menuFrame = new JFrame("Game Menu");
        this.menuFrame.setSize(400, 300);
        this.menuFrame.setLayout(null);
        this.menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.menuFrame.setResizable(false);
        this.menuFrame.setIconImage(Resource.icon.getImage());

        // Single Player Mode Button
        JButton singlePlayerButton = new JButton("Single Player");
        singlePlayerButton.setBounds(130, 50, 140, 30);
        menuFrame.add(singlePlayerButton);
        // singleplayerButton.addActionListener();

        // Multiplayer Mode Button
        JButton multiplayerButton = new JButton("Multiplayer");
        multiplayerButton.setBounds(130, 100, 140, 30);
        menuFrame.add(multiplayerButton);
        multiplayerButton.addActionListener(e -> { 
            LoginDialog dialog = login(new MultiplayerLogin(menuFrame));
            dialog.setVisible(true);
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

    private LoginDialog login(LoginHandler handler) {
        return new LoginDialog(menuFrame, handler);
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
            dialog.setVisible(false);
            parent.setVisible(false);
            MultiplayerClient client = new MultiplayerClient(socket, name);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}