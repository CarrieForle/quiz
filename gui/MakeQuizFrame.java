package gui;

import javax.swing.*;
import utils.*;

public class MakeQuizFrame extends JFrame {
    private QuizBuilder quizBuilder = new QuizBuilder("Untitled");

    public MakeQuizFrame() {
        setTitle("Quiz Builder");
        this.setSize(600, 600);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);

        JLabel questionLabel = new JLabel("Enter Question:");
        questionLabel.setBounds(30, 30, 150, 30);
        this.add(questionLabel);

        JTextField questionField = new JTextField();
        questionField.setBounds(150, 30, 400, 30);
        this.add(questionField);

        JLabel answerLabel = new JLabel("Answers:");
        answerLabel.setBounds(30, 80, 150, 30);
        this.add(answerLabel);

        JTextField[] answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            answerFields[i] = new JTextField("Answer " + i);
            answerFields[i].setBounds(150, 80 + i * 50, 300, 30);
            this.add(answerFields[i]);
        }

        JLabel correctLabel = new JLabel("Correct Answer:");
        correctLabel.setBounds(30, 300, 150, 30);
        this.add(correctLabel);

        JRadioButton[] correctAnswerButtons = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            correctAnswerButtons[i] = new JRadioButton(String.valueOf(i + 1));
            correctAnswerButtons[i].setBounds(460, 80 + i * 50, 50, 30);
            group.add(correctAnswerButtons[i]);
            this.add(correctAnswerButtons[i]);
        }

        JButton saveButton = new JButton("Save");
        saveButton.setBounds(150, 400, 100, 30);
        saveButton.addActionListener(e -> {
            String question = questionField.getText();
            if (question.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a question!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            StringBuilder questionData = new StringBuilder(question + "\n");
            for (int i = 0; i < 4; i++) {
                questionData.append((correctAnswerButtons[i].isSelected() ? "*" : "") + answerFields[i].getText() + "\n");
            }
            JOptionPane.showMessageDialog(this, "Question saved successfully!");
        });
        this.add(saveButton);

        JButton uploadButton = new JButton("Upload");
        uploadButton.setBounds(400, 400, 100, 30);
        this.add(uploadButton);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
