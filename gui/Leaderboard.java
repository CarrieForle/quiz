package gui;

import javax.swing.*;
import java.awt.*;

public class Leaderboard extends JFrame {
    private static JLabel scoreLabel;
    private static JLabel rankLabel;

    public static void main(String[] args) {
        new Leaderboard();

    }

    public Leaderboard() {
        this.setTitle("Leaderboard");
        this.setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);
        this.setIconImage(Resource.iconSmall.getImage());

        // Title Label
        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(0, 10, 400, 30);
        this.add(titleLabel);

        // Podium Panel
        JPanel podiumPanel = new JPanel(null) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Resource.podium.paintIcon(this, g, 25, 80);
            }
        };

        podiumPanel.setBounds(40, 30, 300, 200);

        // First place
        JLabel firstName = new JLabel("Player1", SwingConstants.CENTER);
        firstName.setBounds(120, 30, 60, 20);
        JLabel firstScore = new JLabel("0", SwingConstants.CENTER);
        firstScore.setBounds(120, 50, 60, 20);

        // Second place
        JLabel secondName = new JLabel("Player2", SwingConstants.CENTER);
        secondName.setBounds(40, 60, 60, 20);
        JLabel secondScore = new JLabel("0", SwingConstants.CENTER);
        secondScore.setBounds(40, 80, 60, 20);

        // Third place
        JLabel thirdName = new JLabel("Player3", SwingConstants.CENTER);
        thirdName.setBounds(200, 80, 60, 20);
        JLabel thirdScore = new JLabel("0", SwingConstants.CENTER);
        thirdScore.setBounds(200, 100, 60, 20);

        // Add components to the podium panel
        podiumPanel.add(firstName);
        podiumPanel.add(firstScore);
        podiumPanel.add(secondName);
        podiumPanel.add(secondScore);
        podiumPanel.add(thirdName);
        podiumPanel.add(thirdScore);

        this.add(podiumPanel);

        // Score and Rank (Dynamic)
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(140, 220, 200, 30);
        this.add(scoreLabel);

        rankLabel = new JLabel("Ranking: 0");
        rankLabel.setBounds(140, 250, 200, 30);
        this.add(rankLabel);

        // Buttons
        JButton continueButton = new JButton("Continue");
        continueButton.setBounds(80, 300, 100, 30);
        continueButton.addActionListener(e -> updateGame(firstScore, secondScore, thirdScore));
        this.add(continueButton);

        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(220, 300, 100, 30);
        quitButton.addActionListener(e -> System.exit(0));
        this.add(quitButton);

        // Set this visibility
        this.setVisible(true);
    }

    // Method to update game data dynamically
    private static void updateGame(JLabel firstScore, JLabel secondScore, JLabel thirdScore) {
        // Simulate new scores
        int score1 = (int) (Math.random() * 5000);
        int score2 = (int) (Math.random() * 5000);
        int score3 = (int) (Math.random() * 5000);

        // Update podium scores
        firstScore.setText(String.valueOf(score1));
        secondScore.setText(String.valueOf(score2));
        thirdScore.setText(String.valueOf(score3));

        // Calculate player's rank and score
        int playerScore = (int) (Math.random() * 5000);
        scoreLabel.setText("Score: " + playerScore);

        // Determine player's ranking
        int[] scores = {score1, score2, score3, playerScore};
        int rank = 1;
        for (int score : scores) {
            if (score > playerScore) {
                rank++;
            }
        }
        rankLabel.setText("Ranking: " + rank);
    }
}
