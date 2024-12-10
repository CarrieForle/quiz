package gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import utils.*;

public class MakeQuizFrame extends JFrame {
    private QuizBuilder quizBuilder = new QuizBuilder("Untitled");
    private QuizBuilder.PartialQuestionWithAnswer editing = new QuizBuilder.PartialQuestionWithAnswer();
    private JTextArea questionArea = new JTextArea();
    private JRadioButton[] correctAnswerButtons = new JRadioButton[4];
    private JTextArea[] optionAreas = new JTextArea[4];

    public static void main(String[] args) {
        MakeQuizFrame f = new MakeQuizFrame();
    }

    public MakeQuizFrame() {
        setTitle("Quiz Builder");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel questionPanel = new JPanel(new BorderLayout());

        JLabel questionLabel = new JLabel("Question", SwingConstants.CENTER);
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        questionPanel.add(new JScrollPane(this.questionArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        questionPanel.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

        // for (int i = 0; i < 20; i++) {
        //     JButton btn = new JButton(String.format("Q%d", i));
        //     buttonPanel.add(btn);
        // }

        JPanel answerPanel = new JPanel(new BorderLayout());
        JPanel optionAreaPanel = new JPanel();
        optionAreaPanel.setLayout(new GridLayout(4, 1, 0, 10));
        JScrollPane[] optionAreaScrollPane = new JScrollPane[4];

        for (int i = 0; i < 4; i++) {
            this.optionAreas[i] = new JTextArea();
            this.optionAreas[i].setColumns(20);
            optionAreaScrollPane[i] = new JScrollPane(this.optionAreas[i]);
            optionAreaPanel.add(optionAreaScrollPane[i]);
        }

        JPanel correctAnswerButtonPanel = new JPanel();
        correctAnswerButtonPanel.setLayout(new GridLayout(4, 1));
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            this.correctAnswerButtons[i] = new JRadioButton(String.valueOf((char) (65 + i)));
            this.correctAnswerButtons[i].addActionListener(new SelectAsAnswer(optionAreaScrollPane, optionAreaScrollPane[i]));
            group.add(this.correctAnswerButtons[i]);
            correctAnswerButtonPanel.add(this.correctAnswerButtons[i]);
        }

        answerPanel.add(new JLabel("Options", SwingConstants.CENTER), BorderLayout.NORTH);
        answerPanel.add(optionAreaPanel, BorderLayout.CENTER);
        answerPanel.add(correctAnswerButtonPanel, BorderLayout.EAST);

        JPanel actionButtonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton copyButton = new JButton("Copy");
        JButton saveButton = new JButton("Save");
        JButton uploadButton = new JButton("Upload");

        actionButtonPanel.add(addButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(copyButton);
        actionButtonPanel.add(saveButton);
        actionButtonPanel.add(uploadButton);

        add(questionPanel, BorderLayout.CENTER);
        add(answerPanel, BorderLayout.EAST);
        add(actionButtonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class SelectAsAnswer implements ActionListener {
    private JComponent[] components;
    private JComponent component;

    public SelectAsAnswer(JComponent[] components, JComponent component) {
        this.components = components;
        this.component = component;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (JComponent component : this.components) {
            component.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }

        this.component.setBorder(BorderFactory.createLineBorder(Color.RED));
    }
}