package gui;

import javax.swing.*;

import java.util.Timer;
import java.util.TimerTask;

public class SinglePlayerMode {
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
