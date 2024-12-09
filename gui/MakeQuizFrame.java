package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import utils.*;

public class MakeQuizFrame extends JFrame {
    private QuizBuilder quizBuilder = QuizBuilder.init("Untitled");
    private QuizBuilder.PartialQuestionWithAnswer editing;
    private JTextArea questionArea = new JTextArea();
    private JRadioButton[] correctAnswerButtons = new JRadioButton[4];
    private JTextArea[] optionAreas = new JTextArea[4];
    private JPanel buttonPanel = new JPanel();
    private java.util.List<QuestionButton> questionButtons = new ArrayList<QuestionButton>();
    private JLabel status = new JLabel("Status: Editing Q1", SwingConstants.CENTER);

    public static void main(String[] args) {
        MakeQuizFrame f = new MakeQuizFrame();
    }

    public MakeQuizFrame() {
        this.editing = this.quizBuilder.get(0);

        setTitle("Quiz Builder");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        for (int i = 0; i < this.quizBuilder.size(); i++) {
            QuestionButton referenceButton = new QuestionButton(this.quizBuilder.get(i));
            referenceButton.setText("Q" + String.valueOf(i + 1));

            referenceButton.addActionListener(e -> {
                if (editing != referenceButton.question) {
                    saveQuestion();
                    this.updateStatusToCurrentEditing();
                    editing = referenceButton.question;
                    loadQuestion();
                }
            });

            this.questionButtons.add(referenceButton);
        }

        JPanel questionPanel = new JPanel(new BorderLayout());

        JLabel questionLabel = new JLabel("Question", SwingConstants.CENTER);
        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(new JScrollPane(this.questionArea), BorderLayout.CENTER);
        questionPanel.add(new JScrollPane(this.buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

        this.updateButtonsUI();

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

        this.correctAnswerButtons[0].setSelected(true);

        answerPanel.add(new JLabel("Options", SwingConstants.CENTER), BorderLayout.NORTH);
        answerPanel.add(optionAreaPanel, BorderLayout.CENTER);
        answerPanel.add(correctAnswerButtonPanel, BorderLayout.EAST);

        JPanel actionButtonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton openButton = new JButton("Open");
        JButton uploadButton = new JButton("Upload");

        addButton.addActionListener(e -> addQuestion());
        deleteButton.addActionListener(e -> deleteQuestion());
        saveButton.addActionListener(e -> saveQuiz());
        openButton.addActionListener(e -> loadQuiz());

        actionButtonPanel.add(addButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(saveButton);
        actionButtonPanel.add(openButton);
        actionButtonPanel.add(uploadButton);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(questionPanel, BorderLayout.CENTER);
        mainPanel.add(answerPanel, BorderLayout.EAST);
        mainPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        this.add(this.status, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setQuizBuilder(QuizBuilder qb) {
        this.quizBuilder = qb;
    }

    public String getQuestion() {
        return editing.question;
    }

    public String getOption(int i) {
        return editing.getOption(i);
    }

    public Integer getAnswer() {
        return editing.answer;
    }

    public void addQuestion() {
        this.saveQuestion();

        this.editing = this.quizBuilder.append_new();
        final int id = this.quizBuilder.size();

        QuestionButton referenceButton = new QuestionButton(this.editing);
        referenceButton.setText("Q" + String.valueOf(id));

        referenceButton.addActionListener(e -> {
            if (editing == referenceButton.question) {
                return;
            }

            saveQuestion();
            this.updateStatusToCurrentEditing();
            editing = referenceButton.question;
            loadQuestion();
        });

        this.questionButtons.add(referenceButton);
        this.updateStatusToCurrentEditing();
        this.updateButtonsUI();
        this.clearFields();
    }

    public void deleteQuestion() {
        if (this.quizBuilder.size() <= 1) {
            return;
        }

        int new_index = 0;
        this.quizBuilder.remove(this.editing);

        for (QuestionButton button : this.questionButtons) {
            if (button.question == this.editing) {
                this.questionButtons.remove(button);
                break;
            }
        }

        this.editing = this.quizBuilder.get(new_index);
        this.updateStatusToCurrentEditing();
        this.loadQuestion();
        this.updateButtonsUI();
    }
    
    public void saveQuiz() {
        JFileChooser fileChooser = new JFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
            saveQuestion();

            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(".quiz")) {
                file = new File(file.getAbsolutePath() + ".quiz");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(this.quizBuilder.getName());
                writer.write("\n");

                for (int i = 0; i < quizBuilder.size(); i++) {
                    QuizBuilder.PartialQuestionWithAnswer question = quizBuilder.get(i);
                    writer.write(String.format("\n$$%s::::%d\n", question, question.answer));

                    for (int j = 0; j < 4; j++) {
                        writer.write(String.format("%s:::", question.getOption(j)));
                    }

                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Question bank saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save question bank.");
            }
        }
    }

    public void loadQuiz() {
        JFileChooser fileChooser = new JFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String contents = Files.readString(file.toPath());

                // TODO
                System.out.println(contents);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load question bank.");
            }
        }
    }

    public void updateButtonsUI() {
        this.buttonPanel.removeAll();

        for (int i = 0; i < this.questionButtons.size(); i++) {
            this.questionButtons.get(i).setText("Q" + String.valueOf(i + 1));
            this.buttonPanel.add(this.questionButtons.get(i));
        }

        revalidate();
        repaint();
    }

    public void clearFields() {
        this.questionArea.setText("");

        for (JTextArea area : this.optionAreas) {
            area.setText("");
        }

        this.correctAnswerButtons[0].setSelected(true);
    }

    public void loadQuestion() {
        this.questionArea.setText(this.editing.question);

        for (int i = 0; i < this.optionAreas.length; i++) {
            this.optionAreas[i].setText(this.editing.getOption(i));
        }

        this.correctAnswerButtons[this.editing.answer].setSelected(true);
    }

    public void saveQuestion() {
        this.editing.question = this.questionArea.getText();

        for (int i = 0; i < this.optionAreas.length; i++) {
            this.editing.setOptions(i, this.optionAreas[i].getText());
        }

        for (int i = 0; i < this.correctAnswerButtons.length; i++) {
            if (this.correctAnswerButtons[i].isSelected()) {
                this.editing.answer = i;
                break;
            }
        }
    }

    private void updateStatus(String s) {
        this.status.setText(String.format("Status: %s", s));
    }

    private void updateStatusToCurrentEditing() {
        this.updateStatus(String.format("Editing Q%d", this.quizBuilder.indexOf(this.editing) + 1));
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

class QuestionButton extends JButton {
    public QuizBuilder.PartialQuestionWithAnswer question;

    public QuestionButton(QuizBuilder.PartialQuestionWithAnswer q) {
        this.question = q;
    }
}