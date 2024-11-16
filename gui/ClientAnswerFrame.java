package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;

public class ClientAnswerFrame extends JFrame {
    private AnswerButton[] answer_buttons = new AnswerButton[4];
    private JProgressBar timer_progress_bar = new JProgressBar();
    private JLabel timer_label = new JLabel("10");
    private Score score = new Score();
    private JLabel question_label = new JLabel("Question here.");

    public ClientAnswerFrame() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setTitle("JavaHoot!");

        for (int i = 0; i < answer_buttons.length; i++) {
            answer_buttons[i] = new AnswerButton(i);
        }

        JPanel answer_panel = new JPanel();
        answer_panel.setLayout(new GridLayout(2, 2));

        for (AnswerButton btn : answer_buttons) {
            answer_panel.add(btn);
        }

        JPanel timer_panel = new JPanel();
        timer_panel.add(timer_label);
        timer_panel.add(timer_progress_bar);

        JPanel head_panel = new JPanel();
        head_panel.setLayout(new BorderLayout());
        head_panel.add(question_label, BorderLayout.CENTER);
        head_panel.add(timer_panel, BorderLayout.SOUTH);

        add(head_panel, BorderLayout.NORTH);
        add(answer_panel, BorderLayout.CENTER);
        add(score, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        ClientAnswerFrame gui = new ClientAnswerFrame();
    }
}

class AnswerButton extends JButton implements ActionListener {
    private int identifier;

    AnswerButton(int id) {
        this.identifier = id;
        this.addActionListener(this);
        setText(String.format("Answer %d", this.identifier));
    }

    public int getId() {
        return this.identifier;
    }

    public void actionPerformed(ActionEvent e) {
        long timestamp = e.getWhen();

        try (FileWriter writer = new FileWriter(String.format("Answer_%d.txt", this.getId()))) {
            writer.write(String.format("Timestamp: %d\nLocal Datetime: %s", timestamp, LocalDateTime.now()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class Score extends JLabel {
    private int score = 0;
    private int ranking = -1;

    public Score() {
        setHorizontalAlignment(SwingConstants.CENTER);
        updateScoreText();
    }

    public void setScore(int score) {
        this.score = score;
        updateScoreText();
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
        updateScoreText();
    }

    public int getRanking() {
        return this.ranking;
    }

    public int getScore() {
        return this.score;
    }

    private void updateScoreText() {
        this.setText(String.format("Score: %d   Ranking: %d", this.score, this.ranking));
    }
}