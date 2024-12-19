package gui;

import javax.swing.*;
import java.awt.*;
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

        new Leaderboard(List.of(players), players[3]) {
            @Override
            protected void onContinue() {
                
            }
        };
    }

    public static class Player {
        String name;
        int score;

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
        names[1].setBounds(40, 60, 60, 20);
        scores[1].setBounds(40, 80, 60, 20);

        // Third place
        names[2].setBounds(200, 80, 60, 20);
        scores[2].setBounds(200, 100, 60, 20);

        this.add(podiumPanel);

        boolean in_top_three = false;
        for (int i = 0; i < 3; i++) {
            if (me == players.get(i)) {
                in_top_three = true;
                break;
            }
        }

        // Score and Rank (Dynamic)
        if (!in_top_three) {
            JLabel scoreLabel = new JLabel("Score: " + me.score);
            scoreLabel.setBounds(140, 220, 200, 30);
            this.add(scoreLabel);

            JLabel rankLabel = new JLabel("Rank: " + (players.indexOf(me) + 1));
            rankLabel.setBounds(140, 250, 200, 30);
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
