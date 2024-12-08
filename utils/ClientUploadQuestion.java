package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientUploadQuestion extends JFrame {
    private List<Question> questions = new ArrayList<>();
    private JTextArea questionField;
    private JTextField answer0Field, answer1Field, answer2Field, answer3Field;
    private JComboBox<String> correctAnswerBox;
    private JPanel buttonPanel;

    public ClientUploadQuestion() {
        setTitle("Question Setting Mode");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));
        add(buttonPanel, BorderLayout.CENTER); 

        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BorderLayout());

        // Question input
        JPanel questionInputPanel = new JPanel();
        questionInputPanel.setLayout(new GridLayout(1, 1));
        questionInputPanel.add(new JLabel("Question:"));
        questionField = new JTextArea(5, 20); 
        questionField.setWrapStyleWord(true);
        questionField.setLineWrap(true);
        questionField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        questionInputPanel.add(new JScrollPane(questionField));
        questionPanel.add(questionInputPanel, BorderLayout.NORTH);

        //answer options input 
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(5, 1));
        optionsPanel.add(new JLabel("Answer 0:"));
        answer0Field = new JTextField();
        optionsPanel.add(answer0Field);

        optionsPanel.add(new JLabel("Answer 1:"));
        answer1Field = new JTextField();
        optionsPanel.add(answer1Field);

        optionsPanel.add(new JLabel("Answer 2:"));
        answer2Field = new JTextField();
        optionsPanel.add(answer2Field);

        optionsPanel.add(new JLabel("Answer 3:"));
        answer3Field = new JTextField();
        optionsPanel.add(answer3Field);

        optionsPanel.add(new JLabel("Correct Answer:"));
        correctAnswerBox = new JComboBox<>(new String[]{"Answer 0", "Answer 1", "Answer 2", "Answer 3"});
        optionsPanel.add(correctAnswerBox);

        //buttons
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton addButton = new JButton("Add Question");
        addButton.addActionListener(e -> addQuestion());
        actionButtonPanel.add(addButton);

        JButton saveButton = new JButton("Save Question Bank");
        saveButton.addActionListener(e -> saveQuestionBank());
        actionButtonPanel.add(saveButton);

        JButton uploadButton = new JButton("Upload to Server");
        uploadButton.addActionListener(e -> uploadToServer(new File ("test2")));
        actionButtonPanel.add(uploadButton);

        add(questionPanel, BorderLayout.WEST);
        add(optionsPanel, BorderLayout.EAST);
        add(actionButtonPanel, BorderLayout.SOUTH);

        showInitialDialog();
    }

    private void showInitialDialog() {
        String[] options = {"Create New Question Bank", "Select Local Question Bank"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose an option to start:",
                "Question Bank",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            questions.clear();
            buttonPanel.removeAll();
            updateButtons();
        } else if (choice == 1) {
            loadQuestionBank();
        } else {
            System.exit(0);
        }
    }

    private void loadQuestionBank() {
        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                questions.clear();
                buttonPanel.removeAll();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Q")) {
                        String questionText = line.substring(1, line.indexOf("a"));
                        String correctAnswerIndex = line.substring(line.indexOf("a") + 1);
                        String[] answers = new String[4];
                        for (int i = 0; i < 4; i++) {
                            answers[i] = reader.readLine().substring(1);
                        }
                        questions.add(new Question(questionText, answers, Integer.parseInt(correctAnswerIndex)));
                    }
                }
                updateButtons();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load question bank.");
            }
        }
    }

    private void updateButtons() {
        buttonPanel.removeAll();
        for (int i = 0; i < questions.size(); i++) {
            int questionIndex = i + 1;
            JButton button = new JButton("Q" + questionIndex);
            int finalI = i;
            button.addActionListener(e -> displayQuestion(finalI));
            buttonPanel.add(button);
        }
        revalidate();
        repaint();
    }

    private void displayQuestion(int index) {
        Question question = questions.get(index);
        questionField.setText(question.getQuestion());
        answer0Field.setText(question.getAnswers()[0]);
        answer1Field.setText(question.getAnswers()[1]);
        answer2Field.setText(question.getAnswers()[2]);
        answer3Field.setText(question.getAnswers()[3]);
        correctAnswerBox.setSelectedIndex(question.getCorrectAnswer());
    }

    private void addQuestion() {
        String question = questionField.getText().trim();
        String answer0 = answer0Field.getText().trim();
        String answer1 = answer1Field.getText().trim();
        String answer2 = answer2Field.getText().trim();
        String answer3 = answer3Field.getText().trim();
        String correctAnswerText = (String) correctAnswerBox.getSelectedItem();

        if (question.isEmpty() || answer0.isEmpty() || answer1.isEmpty() || answer2.isEmpty() || answer3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields.");
            return;
        }

        int correctAnswerIndex = Integer.parseInt(correctAnswerText.split(" ")[1]);

        Question newQuestion = new Question(question, new String[]{answer0, answer1, answer2, answer3}, correctAnswerIndex);
        questions.add(newQuestion);
        updateButtons();
        clearFields();
    }

    private void clearFields() {
        questionField.setText("");
        answer0Field.setText("");
        answer1Field.setText("");
        answer2Field.setText("");
        answer3Field.setText("");
        correctAnswerBox.setSelectedIndex(0);
    }

    private void saveQuestionBank() {
        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showSaveDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Question question : questions) {
                    writer.write("Q" + question.getQuestion() + "a" + question.getCorrectAnswer());
                    writer.newLine();
                    for (int i = 0; i < 4; i++) {
                        writer.write("A" + question.getAnswers()[i]);
                        writer.newLine();
                    }
                }
                JOptionPane.showMessageDialog(this, "Question bank saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save question bank.");
            }
        }
    }

    private void uploadQuestionBank() {
        JFileChooser fileChooser = new JFileChooser();
        int choice = fileChooser.showOpenDialog(this);

        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            uploadToServer(file);
        }
    }

    private void uploadToServer(File file) {
        try (Socket socket = new Socket("26.198.51.130", 12345);
             FileInputStream fileInputStream = new FileInputStream(file);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {

            dataOutputStream.writeUTF(file.getName());
            System.out.println(file == null);
            System.out.println(file.getName());
            String contents = new String(fileInputStream.readAllBytes());
            dataOutputStream.writeUTF(contents);

            // byte[] buffer = new byte[4096];
            // int bytesRead;
            // while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            //     dataOutputStream.write(buffer, 0, bytesRead);
            // }

            JOptionPane.showMessageDialog(this, "File uploaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to upload the file.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUploadQuestion frame = new ClientUploadQuestion();
            frame.setVisible(true);
        });
    }

    private static class Question {
        private String question;
        private String[] answers;
        private int correctAnswer;

        public Question(String question, String[] answers, int correctAnswer) {
            this.question = question;
            this.answers = answers;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getAnswers() {
            return answers;
        }

        public int getCorrectAnswer() {
            return correctAnswer;
        }
    }
}
