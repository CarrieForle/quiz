package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import utils.*;
import utils.QuizBuilder.PartialQuestionWithAnswer;
import utils.exceptions.CorruptedQuestionsException;
import networking.*;

public class MakeQuizFrame extends JFrame {
    private static final String DEFAULT_NAME = "Untitled";
    private File file = new File(DEFAULT_NAME);
    private QuizBuilder quizBuilder = QuizBuilder.init(DEFAULT_NAME);
    private QuizBuilder.PartialQuestionWithAnswer editing;
    private JTextArea questionArea = new JTextArea();
    private JRadioButton[] correctAnswerButtons = new JRadioButton[4];
    private JTextArea[] optionAreas = new JTextArea[4];
    private JPanel buttonPanel = new JPanel();
    private List<QuestionButton> questionButtons = new ArrayList<QuestionButton>();
    private IncompleteDialog incompleteFrame = new IncompleteDialog(this);
    private JLabel status = new JLabel("Status: Editing Q1", SwingConstants.CENTER);
    private static LoginDialog.Info loginInfo;

    public static void main(String[] args) {
        new MakeQuizFrame();
    }

    public MakeQuizFrame() {
        setTitle(String.format("出題趣！ (%s)", this.file));
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        final MakeQuizFrame self = this;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                switch (JOptionPane.showConfirmDialog(self, "Do you want to save before exiting?", "Save before quit", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
                    case JOptionPane.YES_OPTION:
                        if (saveQuiz()) {
                            dispose();
                        }
                        break;
                    case JOptionPane.NO_OPTION:
                        dispose();
                        break;
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                new MainMenu();
            }
        });

        setIconImage(Resource.icon.getImage());
        
        this.constructButtons();
        this.editing = this.quizBuilder.get(0);
        this.questionButtons.get(0).select();
        
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
                    Common.errorMessage(this, "Number is out of bound");
                    continue;
                }

                break;
            } catch (NumberFormatException e) {
                Common.errorMessage(this, "You must enter a number");
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
        this.questionButtons.get(this.quizBuilder.indexOf(this.editing)).deselect();
        this.editing = this.quizBuilder.insert_new(i);
        this.addButton(this.editing);
        this.questionButtons.get(i).select();
        this.updateStatusToCurrentEditing();
        this.clearFields();
        this.updateButtonPanel();
    }

    public void deleteQuestion() {
        if (this.quizBuilder.size() <= 1) {
            return;
        }

        int new_index = this.quizBuilder.indexOf(this.editing);
        this.quizBuilder.remove(this.editing);

        for (QuestionButton button : this.questionButtons) {
            if (button.question == this.editing) {
                this.questionButtons.remove(button);
                break;
            }
        }

        if (new_index >= this.quizBuilder.size()) {
            new_index = this.quizBuilder.size() - 1;
        }

        this.editing = this.quizBuilder.get(new_index);
        this.questionButtons.get(new_index).select();
        this.updateStatusToCurrentEditing();
        this.updateQuestionUI();
        this.updateButtonPanel();
    }

    public boolean saveQuiz() {
        JFileChooser fileChooser = this.getFileChooser();

        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
            this.saveQuestion();

            File file = fileChooser.getSelectedFile();

            try {
                Path path = QuizStorage.save(file.toPath(), this.quizBuilder);
                this.setFile(path.toFile());
                JOptionPane.showMessageDialog(this, "Quiz saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save quiz.");

                return false;
            }

            return true;
        } else {
            return false;
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

                this.questionButtons.get(this.quizBuilder.indexOf(this.editing)).deselect();
                this.editing = this.quizBuilder.get(0);
                this.questionButtons.get(0).select();
                this.setFile(file);
                this.updateQuestionUI();
                this.updateStatusToCurrentEditing();
                this.constructButtons();
            } catch (IOException e) {
                e.printStackTrace();
                Common.errorMessage(this, "Failed to load quiz", e);
            } catch (CorruptedQuestionsException e) {
                Common.errorMessage(this, "Failed to parse quiz", e);
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
                Common.errorMessage(this, "The quiz name must not be blank");
                continue;
            }

            break;
        }

        this.quizBuilder.setName(name);
    }

    public void uploadQuiz() {
        this.saveQuestion();
        List<PartialQuestionWithAnswer> questions = this.quizBuilder.getIncompleteQuestions();

        if (questions.isEmpty()) {       
            if (quizBuilder.getName().equals(DEFAULT_NAME)) {
                renameQuiz();
            }

            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Do you want to save before uploading?", "Save", JOptionPane.YES_NO_OPTION)) {
                saveQuiz();
            }

            loginInfo = LoginDialog.get(this, new QuizUploadHandler(this), loginInfo);
        } else {
            this.incompleteFrame.removeAll();

            for (PartialQuestionWithAnswer question : questions) {
                String name = this.getQuestionName(question);
                this.incompleteFrame.addTab(name, question);
            }
            
            this.incompleteFrame.setVisible(true);
        }
    }

    private void constructButtons() {
        this.buttonPanel.removeAll();
        this.questionButtons.clear();

        for (int i = 0; i < this.quizBuilder.size(); i++) {
            addButton(this.quizBuilder.get(i));
            this.buttonPanel.add(this.questionButtons.get(this.questionButtons.size() - 1));
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
        JFileChooser fileChooser = QuizStorage.getFileChooser();
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
            this.questionButtons.get(this.quizBuilder.indexOf(editing)).deselect();
            editing = referenceButton.question;
            referenceButton.select();
            updateStatusToCurrentEditing();
            updateQuestionUI();
        });

        int index = this.quizBuilder.indexOf(referenceButton.question);
        this.questionButtons.add(index, referenceButton);
    }

    private String getQuestionName(PartialQuestionWithAnswer question) {
        return String.format("Q%d", this.quizBuilder.indexOf(question) + 1);
    }

    private void setFile(File file) {
        this.file = file;
        this.setTitle(String.format("出題趣！ (%s)", file.getName()));
    }

    QuizBuilder getQuizBuilder() {
        return this.quizBuilder;
    }
}

class IncompleteDialog extends JDialog {
    private JTabbedPane pane = new JTabbedPane();

    public IncompleteDialog(MakeQuizFrame parent) {
        super(parent, "Quiz failed to upload", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(300, 400);
        setLocationRelativeTo(parent);
        add(this.pane);
    }

    public void addTab(String tabName, PartialQuestionWithAnswer question) {
        StringBuilder text = new StringBuilder();

        for (String field : question.getIncompleteField()) {
            String fieldText = "";

            if (field.equals("question")) {
                fieldText = "Question must not be blank";
            } else {
                char option = (char) (65 + Integer.valueOf(field));
                fieldText = String.format("Option %c must not be blank", option);
            }

            text.append(fieldText);
            text.append("\n");
        }

        JTextArea area = new JTextArea(text.toString());
        area.setEditable(false);
        this.pane.addTab(tabName, new JScrollPane(area));
    }

    public void removeAll() {
        this.pane.removeAll();
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

class QuizUploadHandler extends LoginHandler {
    private MakeQuizFrame frame;

    public QuizUploadHandler(MakeQuizFrame frame) {
        this.frame = frame;
    }

    @Override
    public void login(LoginDialog dialog, InetSocketAddress address, String b) {
        try (Socket socket = new Socket(address.getHostString(), address.getPort())) {
            ClientUploadQuestion.uploadToServer(this.frame.getQuizBuilder().toQuestionSet(), socket.getOutputStream());
            dialog.dispose();
            JOptionPane.showMessageDialog(this.frame, "Upload succeeded", "Success",JOptionPane.INFORMATION_MESSAGE);
        } catch (UnknownHostException ex) {
            Common.errorMessage(this.frame, "Failed to resolve server address", ex);
        } catch (IOException ex) {
            Common.errorMessage(this.frame, "Failed to upload quiz to server", ex);
        }
    }
}

class QuestionButton extends JButton {
    public QuizBuilder.PartialQuestionWithAnswer question;

    public QuestionButton(QuizBuilder.PartialQuestionWithAnswer q) {
        this.question = q;

        this.deselect();
        setPreferredSize(new Dimension(50, 30));

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

    public void select() {
        setBorder(BorderFactory.createLineBorder(Color.RED));
    }
    
    public void deselect() {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}