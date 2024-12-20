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
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        // this.setResizable(false);
        this.setIconImage(Resource.icon.getImage());
        this.setResizable(false);

        // Title Label
        JLabel titleLabel = new JLabel("LEADERBOARD");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        this.add(titleLabel);
        titleLabel.setAlignmentX(0.5f);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Podium Panel
        JPanel podiumPanel = new JPanel(null) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Resource.podium.paintIcon(this, g, 25, 65);
            }
        };

        podiumPanel.setPreferredSize(new Dimension(100, 200));

        JLabel[] names = new JLabel[3];
        JLabel[] scores = new JLabel[3];

        for (int i = 0; i < 3; i++) {
            JLabel name;
            JLabel score;

            if (i < players.size()) {
                name = new JLabel(players.get(i).name, SwingConstants.CENTER);
                name.setFont(name.getFont().deriveFont(16f));
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
        names[0].setBounds(50, 15, 200, 20);
        scores[0].setBounds(120, 35, 60, 20);

        // Second place
        names[1].setBounds(-35, 50, 200, 20);
        scores[1].setBounds(35, 70, 60, 20);

        // Third place
        names[2].setBounds(135, 70, 200, 20);
        scores[2].setBounds(205, 90, 60, 20);

        podiumPanel.setAlignmentX(0.5f);
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
        } else if (rank <= 3) {
            JLabel goodJob = new JLabel("You are in the TOP 3");
            names[rank - 1].setForeground(Color.RED);
            scores[rank - 1].setForeground(Color.RED);
            goodJob.setFont(goodJob.getFont().deriveFont(16f));
            goodJob.setForeground(Color.RED);
            goodJob.setBounds(110, 240, 200, 30);
            goodJob.setAlignmentX(0.5f);
            this.add(goodJob);
        } else {
            JPanel panel = new JPanel();
            JLabel scoreLabel = new JLabel("Score: " + me.score);
            panel.add(scoreLabel);
            
            JLabel rankLabel = new JLabel("Rank: " + (players.indexOf(me) + 1));
            rankLabel.setAlignmentX(0.5f);
            panel.add(rankLabel);
            panel.setAlignmentX(0.5f);
            this.add(panel);
        }

        // Buttons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> {
            this.dispose();
            onContinue();
        });
        continueButton.setPreferredSize(new Dimension(100, 30));

        panel.add(continueButton);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> {
            this.dispose();
            new MainMenu();
        });
        quitButton.setPreferredSize(new Dimension(100, 30));

        panel.add(quitButton);
        panel.setAlignmentX(0.5f);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.add(panel);

        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }
    
    protected abstract void onContinue();
}
