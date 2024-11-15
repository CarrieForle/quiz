package gui;

import javax.swing.*;
import java.awt.*;

public class Quiz extends JFrame {
    private AnswerButton[] answer_buttons = new AnswerButton[4];
    private JProgressBar timer_progress_bar = new JProgressBar();
    private JLabel timer_label = new JLabel("10");
    private Score score = new Score();
    private JLabel question_label = new JLabel("Question here.");

    public Quiz() {
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
}