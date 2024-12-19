package gui;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

public abstract class Leaderboard extends JFrame {
    public static void main(String[] args) {
        Player[] players = { 
            new Player("Bob", 1),
            new Player("Jennifer", 2),
            new Player("Jimmy", 3),
            new Player("Patrick", 4),
            new Player("Becky", 5),
            new Player("Tom", 6)
        };

        new Leaderboard(List.of(players), players[1]) {
            @Override
            protected void onContinue() {
                
            }
        };
    }

    public static class Player implements Serializable {
        public String name;
        public int score;

        public Player(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public Leaderboard(List<Player> players, Player me) {
        this.setTitle("Leaderboard");
        this.setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);
        this.setIconImage(Resource.icon.getImage());

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

        JLabel[] names = new JLabel[3];
        JLabel[] scores = new JLabel[3];

        for (int i = 0; i < 3; i++) {
            JLabel name;
            JLabel score;

            if (i < players.size()) {
                name = new JLabel(players.get(i).name, SwingConstants.CENTER);
                score = new JLabel(String.valueOf(players.get(i).score), SwingConstants.CENTER);
            } else {
                name = new JLabel();
                score = new JLabel();
            }

            names[i] = name;
            scores[i] = score;

            podiumPanel.add(name);
            podiumPanel.add(score);
        }

        // First place
        names[0].setBounds(120, 30, 60, 20);
        scores[0].setBounds(120, 50, 60, 20);

        // Second place
        names[1].setBounds(35, 60, 60, 20);
        scores[1].setBounds(35, 80, 60, 20);

        // Third place
        names[2].setBounds(205, 80, 60, 20);
        scores[2].setBounds(205, 100, 60, 20);

        this.add(podiumPanel);

        int rank = players.indexOf(me) + 1;

        if (rank == 1) {
            JLabel winner = new JLabel("YOU WIN!");
            Color nameColor = new Color(228, 94, 16);
            names[rank - 1].setForeground(nameColor);
            scores[rank - 1].setForeground(nameColor);
            winner.setFont(winner.getFont().deriveFont(20f));
            winner.setForeground(nameColor);
            winner.setBounds(145, 240, 200, 30);
            this.add(winner);
        }
        else if (rank <= 3) {
            JLabel goodJob = new JLabel("You are in the TOP 3");
            names[rank - 1].setForeground(Color.RED);
            scores[rank - 1].setForeground(Color.RED);
            goodJob.setFont(goodJob.getFont().deriveFont(16f));
            goodJob.setForeground(Color.RED);
            goodJob.setBounds(110, 240, 200, 30);
            this.add(goodJob);
        } else {
            JLabel scoreLabel = new JLabel("Score: " + me.score);
            scoreLabel.setBounds(160, 230, 200, 30);
            this.add(scoreLabel);

            JLabel rankLabel = new JLabel("Rank: " + (players.indexOf(me) + 1));
            rankLabel.setBounds(160, 250, 200, 30);
            this.add(rankLabel);
        }

        // Buttons
        JButton continueButton = new JButton("Continue");
        continueButton.setBounds(80, 300, 100, 30);
        continueButton.addActionListener(e -> {
            this.dispose();
            onContinue();
        });
        this.add(continueButton);

        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(220, 300, 100, 30);
        quitButton.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });

        this.add(quitButton);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    protected abstract void onContinue();
}
