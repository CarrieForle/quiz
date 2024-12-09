package gui;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class ShootGUI extends JFrame {
    public static void main(String[] args) {
        new ShootGUI();
    }

    public ShootGUI() {
        setTitle("刷題趣！");
        this.setSize(400, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);

        // Name input label and field
        JLabel nameLabel = new JLabel(" Name:");
        nameLabel.setBounds(30, 30, 150, 30);
        this.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 30, 200, 30);
        this.add(nameField);

        // Server address input label and field
        JLabel serverLabel = new JLabel("IP Address:");
        serverLabel.setBounds(30, 80, 150, 30);
        this.add(serverLabel);

        JTextField serverField = new JTextField();
        serverField.setBounds(150, 80, 200, 30);
        this.add(serverField);

        // Submit Button
        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 130, 100, 30);
        this.add(submitButton);

        // Submit Button action
        submitButton.addActionListener(e -> {
            String playerName = nameField.getText();
            String serverAddress = serverField.getText();

            // Validate inputs
            if (playerName == null || playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            if (serverAddress == null || serverAddress.isEmpty()) {
                serverAddress = "localhost";  // Default to localhost if no input
            }

            // Create final variable to use inside the anonymous class
            final String finalServerAddress = serverAddress;

            // Proceed to the game menu with the entered information
            this.dispose();  // Close the input frame

            // Then show the game menu
            JFrame menuFrame = new JFrame("Game Menu");
            menuFrame.setSize(400, 300);
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menuFrame.setLayout(null);

            // Single Player Mode Button
            JButton singlePlayerButton = new JButton("Single Player");
            singlePlayerButton.setBounds(130, 50, 140, 30);
            menuFrame.add(singlePlayerButton);
            singlePlayerButton.addActionListener(e1 -> new SinglePlayerMode());

            // Multiplayer Mode Button
            JButton multiplayerButton = new JButton("Multiplayer");
            multiplayerButton.setBounds(130, 100, 140, 30);
            menuFrame.add(multiplayerButton);
            multiplayerButton.addActionListener(e1 -> new MultiplayerClient(finalServerAddress));  // Use finalServerAddress here

            // Ask Question Button
            JButton askQuestionButton = new JButton("Ask Question");
            askQuestionButton.setBounds(130, 150, 140, 30);
            menuFrame.add(askQuestionButton);
            askQuestionButton.addActionListener(e1 -> new MakeQuestionFrame());

            // Exit Button
            JButton exitButton = new JButton("Exit");
            exitButton.setBounds(130, 200, 140, 30);
            menuFrame.add(exitButton);
            exitButton.addActionListener(e1 -> System.exit(0));

            menuFrame.setLocationRelativeTo(null);
            menuFrame.setVisible(true);
        });

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}

// Single Player Mode
class SinglePlayerMode {
    private int score = 0;
    private Timer timer;
    private int currentQuestionIndex = 0;

    private String[] questions = {"What is the capital of France?", "What is 2 + 2?"};
    private String[][] answers = {{"Paris", "London", "Berlin", "Madrid"}, {"4", "3", "5", "6"}};
    private String[] correctAnswers = {"Paris", "4"};

    public SinglePlayerMode() {
        JFrame frame = new JFrame("Single Player Mode");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);

        JLabel questionLabel = new JLabel(questions[currentQuestionIndex]);
        questionLabel.setBounds(50, 50, 500, 30);
        frame.add(questionLabel);

        JButton[] answerButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JButton(answers[currentQuestionIndex][i]);
            int x = (i % 2 == 0) ? 100 : 300;
            int y = 100 + (i / 2) * 60;
            answerButtons[i].setBounds(x, y, 150, 40);
            int finalI = i;
            answerButtons[i].addActionListener(e -> checkAnswer(((JButton) e.getSource()).getText(), frame));
            frame.add(answerButtons[i]);
        }

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(50, 10, 100, 30);
        frame.add(scoreLabel);

        JLabel timerLabel = new JLabel("Time Left: 10");
        timerLabel.setBounds(500, 10, 100, 30);
        frame.add(timerLabel);

        // Timer logic
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int timeLeft = 10;

            @Override
            public void run() {
                timeLeft--;
                timerLabel.setText("Time Left: " + timeLeft);
                if (timeLeft <= 0) {
                    timer.cancel();
                    JOptionPane.showMessageDialog(frame, "Time's up! The correct answer is " + correctAnswers[currentQuestionIndex]);
                    moveToNextQuestion(frame);
                }
            }
        }, 0, 1000);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void checkAnswer(String answer, JFrame frame) {
        timer.cancel();
        if (answer.equals(correctAnswers[currentQuestionIndex])) {
            score += 10;
            JOptionPane.showMessageDialog(frame, "Correct! Your score: " + score);
        } else {
            JOptionPane.showMessageDialog(frame, "Incorrect! The correct answer is " + correctAnswers[currentQuestionIndex]);
        }
        moveToNextQuestion(frame);
    }

    private void moveToNextQuestion(JFrame frame) {
        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.length) {
            JOptionPane.showMessageDialog(frame, "Game Over! Your final score is: " + score);
            frame.dispose();
        } else {
            new SinglePlayerMode(); // Create a new question screen
            frame.dispose(); // Close the old screen
        }
    }
}

// Multiplayer Mode Client
class MultiplayerClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public MultiplayerClient(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            JFrame frame = new JFrame("Multiplayer Mode");
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(null);

            JLabel questionLabel = new JLabel("Waiting for a question...");
            questionLabel.setBounds(50, 50, 500, 30);
            frame.add(questionLabel);

            JButton[] answerButtons = new JButton[4];
            for (int i = 0; i < 4; i++) {
                answerButtons[i] = new JButton();
                int x = (i % 2 == 0) ? 100 : 300;
                int y = 100 + (i / 2) * 60;
                answerButtons[i].setBounds(x, y, 150, 40);
                int finalI = i;
                answerButtons[i].addActionListener(e -> {
                    out.println(answerButtons[finalI].getText());
                });
                frame.add(answerButtons[i]);
            }

            new Thread(() -> {
                try {
                    while (true) {
                        String line = in.readLine();
                        if (line.startsWith("QUESTION:")) {
                            String[] parts = line.split(":");
                            questionLabel.setText(parts[1]);
                            for (int i = 0; i < 4; i++) {
                                answerButtons[i].setText(parts[i + 2]);
                            }
                        } else if (line.startsWith("RESULT:")) {
                            JOptionPane.showMessageDialog(frame, line.substring(7));
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Disconnected from the server.");
                    ex.printStackTrace();
                    frame.dispose();
                }
            }).start();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server.");
            e.printStackTrace();
        }
    }
}