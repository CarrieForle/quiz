package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import utils.*;
import utils.QuizBuilder.PartialQuestionWithAnswer;
import utils.exceptions.CorruptedQuestionsException;

public class MakeQuizFrame extends JFrame {
    private File file = new File("Untitled");
    private QuizBuilder quizBuilder = QuizBuilder.init("Untitled");
    private QuizBuilder.PartialQuestionWithAnswer editing;
    private JTextArea questionArea = new JTextArea();
    private JRadioButton[] correctAnswerButtons = new JRadioButton[4];
    private JTextArea[] optionAreas = new JTextArea[4];
    private JPanel buttonPanel = new JPanel();
    private java.util.List<QuestionButton> questionButtons = new ArrayList<QuestionButton>();
    private IncompleteFrame incompleteFrame = new IncompleteFrame(this);
    private JLabel status = new JLabel("Status: Editing Q1", SwingConstants.CENTER);

    public static void main(String[] args) {
        MakeQuizFrame f = new MakeQuizFrame();
    }

    public MakeQuizFrame() {
        setTitle(String.format("Quiz Builder (%s)", this.file));
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        this.constructButtons();
        this.editing = this.quizBuilder.get(0);
        
        JPanel questionPanel = new JPanel(new BorderLayout());

        JLabel questionLabel = new JLabel("Question", SwingConstants.CENTER);
        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(new JScrollPane(this.questionArea), BorderLayout.CENTER);

        JScrollPane buttonScrollPane = new JScrollPane(this.buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        buttonScrollPane.getHorizontalScrollBar().setUnitIncrement(15);

        questionPanel.add(buttonScrollPane, BorderLayout.SOUTH);

        JPanel answerPanel = new JPanel(new BorderLayout());
        JPanel optionAreaPanel = new JPanel();
        optionAreaPanel.setLayout(new GridLayout(4, 1, 0, 10));
        JScrollPane[] optionAreaScrollPane = new JScrollPane[4];

        for (int i = 0; i < 4; i++) {
            this.optionAreas[i] = new JTextArea();
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

        this.correctAnswerButtons[0].doClick();

        answerPanel.add(new JLabel("Options", SwingConstants.CENTER), BorderLayout.NORTH);
        answerPanel.add(optionAreaPanel, BorderLayout.CENTER);
        answerPanel.add(correctAnswerButtonPanel, BorderLayout.EAST);

        JPanel actionButtonPanel = new JPanel();
        JButton renameButton = new JButton("Rename");
        JButton addButton = new JButton("Add");
        JButton insertButton = new JButton("Insert");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton openButton = new JButton("Open");
        JButton uploadButton = new JButton("Upload");

        renameButton.addActionListener(e -> renameQuiz());
        addButton.addActionListener(e -> appendQuestion());
        insertButton.addActionListener(e -> insertQuestion());
        deleteButton.addActionListener(e -> deleteQuestion());
        saveButton.addActionListener(e -> saveQuiz());
        openButton.addActionListener(e -> loadQuiz());
        uploadButton.addActionListener(e -> uploadQuiz());

        actionButtonPanel.add(renameButton);
        actionButtonPanel.add(addButton);
        actionButtonPanel.add(insertButton);
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

    public void insertQuestion() {
        int index = 0;

        while (true) {
            try {
                String input = JOptionPane.showInputDialog(this, "Input the index of new question", "Inset new question", JOptionPane.QUESTION_MESSAGE);

                if (input == null) {
                    return;
                }

                index = Integer.parseInt(input);

                if (index < 1 || index > this.quizBuilder.size() + 1) {
                    JOptionPane.showMessageDialog(this, "Number is out of bound.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                break;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "You must enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        index -= 1;

        this.addQuestion(index);
    }

    public void appendQuestion() {
        this.addQuestion(this.quizBuilder.size());
    }

    public void addQuestion(int i) {
        this.saveQuestion();
        this.editing = this.quizBuilder.insert_new(i);
        this.addButton(editing);
        this.updateStatusToCurrentEditing();
        this.clearFields();
        this.updateButtonPanel();
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
        this.updateButtonPanel();
    }

    public void saveQuiz() {
        JFileChooser fileChooser = this.getFileChooser();

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
        JFileChooser fileChooser = this.getFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
            File file = fileChooser.getSelectedFile();

            try {
                String contents = Files.readString(file.toPath());
                this.quizBuilder = new QuizBuilder(contents);

                if (this.quizBuilder.size() == 0) {
                    this.quizBuilder.append_new();
                }

                this.editing = this.quizBuilder.get(0);
                this.file = file;
                this.updateQuestionUI();
                this.updateStatusToCurrentEditing();
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
            name = JOptionPane.showInputDialog(this, "Enter new quiz name", this.quizBuilder.getName());

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

    public void uploadQuiz() {
        this.saveQuestion();
        java.util.List<PartialQuestionWithAnswer> questions = this.quizBuilder.getIncompleteQuestions();

        if (questions.isEmpty()) {
            // do upload work
        } else {
            StringBuilder errorText = new StringBuilder();

            for (PartialQuestionWithAnswer question : questions) {
                String name = this.getQuestionName(question);

                errorText.append(name);
                errorText.append(":\n");

                for (String field : question.getIncompleteField()) {
                    String fieldText = "";

                    if (field.equals("question")) {
                        fieldText = "Question must not be blank";
                    } else {
                        char option = (char) (65 + Integer.valueOf(field));
                        fieldText = String.format("Option %c must not be blank", option);
                    }

                    errorText.append(fieldText);
                    errorText.append("\n");
                }

                errorText.append("\n");
            }
            
            this.incompleteFrame.setText(errorText.toString());
            this.incompleteFrame.setVisible(true);
        }
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

    private void updateButtonPanel() {
        this.buttonPanel.removeAll();

        for (QuestionButton button : this.questionButtons) {
            button.setText(this.getQuestionName(button.question));
            this.buttonPanel.add(button);
        }

        revalidate();
        repaint();
    }

    private void clearFields() {
        this.questionArea.setText("");

        for (JTextArea area : this.optionAreas) {
            area.setText("");
        }

        this.correctAnswerButtons[0].doClick();
    }

    private void updateQuestionUI() {
        this.questionArea.setText(this.editing.question);

        for (int i = 0; i < this.optionAreas.length; i++) {
            this.optionAreas[i].setText(this.editing.getOption(i));
        }

        this.correctAnswerButtons[this.editing.answer].doClick();
    }

    private void saveQuestion() {
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

        for (QuestionButton button : this.questionButtons) {
            if (button.question == this.editing) {
                button.updateToolTip();
                break;
            }
        }
    }

    private void updateStatus(String s) {
        this.status.setText(String.format("Status: %s", s));
    }

    private void updateStatusToCurrentEditing() {
        this.updateStatus(String.format("Editing %s", this.getQuestionName(this.editing)));
    }

    private JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz File (.quiz)", "quiz"));
        fileChooser.setSelectedFile(this.file);

        return fileChooser;
    }

    private void addButton(PartialQuestionWithAnswer question) {
        QuestionButton referenceButton = new QuestionButton(question);
        referenceButton.setText(this.getQuestionName(question));
        
        referenceButton.addActionListener(e -> {
            if (editing == referenceButton.question) {
                return;
            }
            
            saveQuestion();
            editing = referenceButton.question;
            updateStatusToCurrentEditing();
            updateQuestionUI();
        });

        int index = this.quizBuilder.indexOf(referenceButton.question);
        this.questionButtons.add(index, referenceButton);
    }

    private String getQuestionName(PartialQuestionWithAnswer question) {
        return String.format("Q%d", this.quizBuilder.indexOf(question) + 1);
    }
}

class IncompleteFrame extends JFrame {
    private JTextArea text = new JTextArea();

    public IncompleteFrame(Component parent) {
        setTitle("Quiz failed to upload");
        setLayout(new BorderLayout());

        this.text.setEditable(false);
        this.text.setFont(this.text.getFont().deriveFont(14f));

        add(new JScrollPane(this.text), BorderLayout.CENTER);
        setSize(300, 400);
        setLocationRelativeTo(parent);
    }

    public void setText(String text) {
        this.text.setText(text);
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
        this.updateToolTip();
    }

    public void updateToolTip() {
        String question = this.question.question;

        if (question == null) {
            return;
        }

        String text;

        if (question.length() > 30) {
            text = question.substring(0, 27) + "...";
        } else {
            text = question;
        }

        this.setToolTipText(text);
    }
}