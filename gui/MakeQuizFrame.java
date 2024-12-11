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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import utils.*;
import utils.QuizBuilder.PartialQuestionWithAnswer;
import utils.exceptions.CorruptedQuestionsException;

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

        setTitle("Quiz Builder (Untitled.quiz)");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        this.constructButtons();

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
        JButton renameButton = new JButton("Rename");
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton openButton = new JButton("Open");
        JButton uploadButton = new JButton("Upload");

        renameButton.addActionListener(e -> renameQuiz());
        deleteButton.addActionListener(e -> deleteQuestion());
        saveButton.addActionListener(e -> saveQuiz());
        openButton.addActionListener(e -> loadQuiz());

        actionButtonPanel.add(renameButton);
        actionButtonPanel.add(addButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(saveButton);
        actionButtonPanel.add(openButton);
        actionButtonPanel.add(uploadButton);

        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, questionPanel, answerPanel);
        mainPane.setResizeWeight(0.7);
        
        this.add(this.status, BorderLayout.NORTH);
        this.add(mainPane, BorderLayout.CENTER);
        this.add(actionButtonPanel, BorderLayout.SOUTH);

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
        this.addButton(editing);
        this.updateStatusToCurrentEditing();
        this.clearFields();
        this.updateButtonsUI();
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
        this.updateQuestionUI();
        this.updateButtonsUI();
    }
    
    public void saveQuiz() {
        JFileChooser fileChooser = getFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
            this.saveQuestion();

            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(".quiz")) {
                file = new File(file.getAbsolutePath() + ".quiz");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                writer.write(quizBuilder.toString());

                JOptionPane.showMessageDialog(this, "Quiz saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save quiz.");
            }
        }
    }

    public void loadQuiz() {
        JFileChooser fileChooser = getFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String contents = Files.readString(file.toPath());
                this.quizBuilder = new QuizBuilder(contents);

                if (this.quizBuilder.size() == 0) {
                    this.quizBuilder.append_new();
                }

                this.editing = this.quizBuilder.get(0);
                this.updateQuestionUI();
                this.constructButtons();
                this.setTitle(String.format("Quiz Builder (%s)", file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load quiz.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (CorruptedQuestionsException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, String.format("Failed to parse quiz: %s", e.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void renameQuiz() {
        String name = "";

        while (true) {
            name = JOptionPane.showInputDialog(this, "Enter new quiz name", this.filename.getName());

            if (name == null) {
                return;
            }

            name = name.trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The quiz name must not be blank", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            break;
        }

        this.quizBuilder.setName(name);
    }
    
    private void constructButtons() {
        this.buttonPanel.removeAll();
        this.questionButtons.clear();

        for (int i = 0; i < this.quizBuilder.size(); i++) {
            addButton(this.quizBuilder.get(i));
            this.buttonPanel.add(this.questionButtons.getLast());
        }

        revalidate();
        repaint();
    }

    public void updateButtonsUI() {
        this.buttonPanel.removeAll();

        for (QuestionButton button : this.questionButtons) {
            button.setText("Q" + this.getQuestionID(button.question));
            this.buttonPanel.add(button);
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

    public void updateQuestionUI() {
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
        this.updateStatus(String.format("Editing Q%d", this.getQuestionID(this.editing)));
    }

    private static JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz File (.quiz)", "quiz"));

        return fileChooser;
    }

    private void addButton(PartialQuestionWithAnswer question) {
        QuestionButton referenceButton = new QuestionButton(question);
        referenceButton.setText("Q" + this.getQuestionID(question));
        
        referenceButton.addActionListener(e -> {
            if (editing == referenceButton.question) {
                return;
            }
            
            saveQuestion();
            editing = referenceButton.question;
            updateStatusToCurrentEditing();
            updateQuestionUI();
        });

        this.questionButtons.add(referenceButton);
    }

    private int getQuestionID(PartialQuestionWithAnswer question) {
        return this.quizBuilder.indexOf(question) + 1;
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