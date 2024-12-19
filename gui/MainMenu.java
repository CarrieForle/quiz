package gui;

import javax.swing.*;

public class MainMenu {
    private JFrame menuFrame;

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
            new LoginDialog(menuFrame, new MultiplayerLoginHandler(menuFrame));
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
    
    public JFrame getFrame() {
        return menuFrame;
    }
}