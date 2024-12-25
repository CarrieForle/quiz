package gui;

import javax.swing.*;

import utils.OpenMenuOnClosing;

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

        new Leaderboard(List.of(players), new CorrectPlayer(players[0], 10, 10)) {
            @Override
            protected void onContinue() {
                
            }
        };
    }

    public static class Player implements Serializable {
        public final String name;
        public final int score;

        public Player(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public static class CorrectPlayer extends Player {
        public final int correct;
        public final int outOfAll;

        public CorrectPlayer(String name, int score, int correct, int outOfAll) {
            super(name, score);
            this.correct = correct;
            this.outOfAll = outOfAll;
        }

        public CorrectPlayer(Player player, int correct, int outOfAll) {
            super(player.name, player.score);
            this.correct = correct;
            this.outOfAll = outOfAll;
        }
    }

    public Leaderboard(List<Player> players, CorrectPlayer me) {
        this.setTitle("Leaderboard");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        // this.setResizable(false);
        this.setIconImage(Resource.icon.getImage());
        this.setResizable(false);
        this.addWindowListener(new OpenMenuOnClosing(this));

        // Title Label
        JLabel titleLabel = new JLabel("LEADERBOARD");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));
        this.add(titleLabel);
        titleLabel.setAlignmentX(0.5f);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Podium Panel
        JPanel podiumPanel = new JPanel(null) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Resource.podium.paintIcon(this, g, 0, 65);
            }
        };

        podiumPanel.setPreferredSize(new Dimension(Resource.podium.getIconWidth(), 200));
        podiumPanel.setMaximumSize(new Dimension(Resource.podium.getIconWidth(), 200));

        JLabel[] names = new JLabel[3];
        JLabel[] scores = new JLabel[3];

        for (int i = 0; i < 3; i++) {
            JLabel name;
            JLabel score;

            if (i < players.size()) {
                name = new JLabel(players.get(i).name, SwingConstants.CENTER);
                name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));
                score = new JLabel(String.valueOf(players.get(i).score), SwingConstants.CENTER);
                score.setFont(score.getFont().deriveFont(Font.BOLD, 14f));
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
        names[0].setBounds(25, 15, 200, 20);
        scores[0].setBounds(95, 35, 60, 20);

        // Second place
        names[1].setBounds(-60, 50, 200, 20);
        scores[1].setBounds(10, 70, 60, 20);

        // Third place
        names[2].setBounds(110, 70, 200, 20);
        scores[2].setBounds(180, 90, 60, 20);

        podiumPanel.setAlignmentX(0.5f);
        this.add(podiumPanel);

        int rank = players.stream()
            .dropWhile(x -> !x.name.equals(me.name))
            .map(x -> players.indexOf(x) + 1)
            .findFirst()
            .orElse(-1);

        if (rank == 1) {
            JLabel winner = new JLabel("YOU WIN!");
            Color nameColor = new Color(228, 94, 16);
            names[rank - 1].setForeground(nameColor);
            scores[rank - 1].setForeground(nameColor);
            winner.setFont(winner.getFont().deriveFont(24f));
            winner.setAlignmentX(0.5f);
            winner.setForeground(nameColor);
            this.add(winner);
        } else if (rank > 1 && rank <= 3) {
            JLabel goodJob = new JLabel("You are in the TOP 3");
            names[rank - 1].setForeground(Color.RED);
            scores[rank - 1].setForeground(Color.RED);
            goodJob.setFont(goodJob.getFont().deriveFont(18f));
            goodJob.setForeground(Color.RED);
            goodJob.setAlignmentX(0.5f);
            this.add(goodJob);
        } else {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
            JLabel infoLabel = new JLabel("Score: " + me.score);
            JLabel rankLabel = new JLabel("Rank: " + rank);

            JLabel[] labels = { infoLabel, rankLabel };

            for (JLabel label : labels) {
                label.setFont(label.getFont().deriveFont(14f));
                panel.add(label);
            }

            panel.setAlignmentX(0.5f);
            this.add(panel);
        }

        JLabel rateLabel = new JLabel(String.format("Correctness: %d/%d", me.correct, me.outOfAll));
        rateLabel.setFont(rateLabel.getFont().deriveFont(14f));

        if (me.correct >= me.outOfAll) {
            rateLabel.setFont(rateLabel.getFont().deriveFont(Font.BOLD));
            rateLabel.setForeground(new Color(16, 128, 134));
        }

        rateLabel.setAlignmentX(0.5f);
        this.add(rateLabel);

        // Buttons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> {
            this.dispose();
            onContinue();
        });
        continueButton.setPreferredSize(new Dimension(100, 30));

        panel.add(continueButton);

        JButton quitButton = new JButton("Menu");
        quitButton.addActionListener(e -> {
            this.dispose();
        });
        quitButton.setPreferredSize(new Dimension(100, 30));

        panel.add(quitButton);
        panel.setAlignmentX(0.5f);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.add(panel);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    protected abstract void onContinue();
}
