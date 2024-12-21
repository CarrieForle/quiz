package gui;

import java.awt.*;
import java.net.URI;

import javax.swing.*;

public class MainMenu {
    private JFrame menuFrame;
    private Desktop desktop;

    public static void main(String[] args) {
        new MainMenu();
    }

    public MainMenu() {
        desktop = Desktop.getDesktop();
        menuFrame = new JFrame("刷題趣！");
        menuFrame.setPreferredSize(new Dimension(300, 430));
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // menuFrame.setResizable(false);
        menuFrame.setIconImage(Resource.icon.getImage());
        Box box = Box.createVerticalBox();
        JLabel icon = new JLabel(new ImageIcon(Resource.icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        icon.setAlignmentX(0.5f);
        box.add(icon);
        box.add(Box.createVerticalGlue());
        int fillerHeight = 20;

        // Single Player Mode Button
        Dimension buttonSize = new Dimension(140, 30);
        JButton singleplayerButton = new JButton("Singleplayer");
        singleplayerButton.setMaximumSize(buttonSize);
        singleplayerButton.setPreferredSize(buttonSize);
        singleplayerButton.setAlignmentX(0.5f);
        box.add(singleplayerButton);
        singleplayerButton.addActionListener(e -> {
            SingleplayerClient.runQuizDialog(menuFrame);
        });

        box.add(Box.createVerticalStrut(fillerHeight));

        // Multiplayer Mode Button
        JButton multiplayerButton = new JButton("Multiplayer");
        multiplayerButton.setMaximumSize(buttonSize);
        multiplayerButton.setPreferredSize(buttonSize);
        multiplayerButton.setAlignmentX(0.5f);
        box.add(multiplayerButton);
        multiplayerButton.addActionListener(e -> {
            new LoginDialog(menuFrame, new MultiplayerLoginHandler(menuFrame));
        });

        box.add(Box.createVerticalStrut(fillerHeight));

        // Ask Question Button
        JButton askQuestionButton = new JButton("Create Quiz");
        askQuestionButton.setMaximumSize(buttonSize);
        askQuestionButton.setPreferredSize(buttonSize);
        askQuestionButton.setAlignmentX(0.5f);
        box.add(askQuestionButton);
        askQuestionButton.addActionListener(e -> {
            new MakeQuizFrame();
            menuFrame.dispose();
        });

        box.add(Box.createVerticalStrut(fillerHeight));

        // Exit Button
        JButton exitButton = new JButton("Exit");
        exitButton.setMaximumSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        exitButton.setAlignmentX(0.5f);
        box.add(exitButton);
        exitButton.addActionListener(e -> System.exit(0));
        box.add(Box.createVerticalStrut(20));

        JPanel logoPanel = new JPanel();
        JButton github = new JButton(new ImageIcon(Resource.github.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        github.addActionListener(e -> {
            try {
                desktop.browse(new URI("https://github.com/CarrieForle/quiz"));
            } catch (Exception ex) {
                System.out.println("Failed to browse webpage");
            }
        });

        github.setPreferredSize(new Dimension(32, 32));
        logoPanel.add(github);
        
        logoPanel.setMaximumSize(new Dimension(100, 48));
        box.add(logoPanel);

        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        menuFrame.add(box);
        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }
    
    public JFrame getFrame() {
        return menuFrame;
    }
}