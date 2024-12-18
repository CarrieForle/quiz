package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import quiz.Server;

import java.awt.BorderLayout;
import java.awt.event.*;

import utils.OpenMenuOnClosing;
import utils.QuestionSet;
import utils.QuestionWithAnswer;
import utils.QuizAnswerResponse;
import utils.exceptions.CorruptedQuestionsException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.TimerTask;
import java.util.List;
import java.util.Timer;

public class SingleplayerClient {
    private JFrame frame;
    private QuestionSet questionSet;
    private int runningQuestionID = 0;
    private TimerTask tt;
    private Timer t = new Timer();
    private JLabel questionLabel = new JLabel();
    private JLabel scoreLabel = new JLabel("Score: ");
    private JLabel timeLabel = new JLabel("Time: ");
    private long startTimestamp = 0;
    private JButton[] answerButtons = new JButton[4];
    private int score = 0;
    private Socket socket;

    public static void main(String[] args) {
        new SingleplayerClient();
    }

    public SingleplayerClient() {
        questionSet = GetQuizDialog.get();
        frame = new JFrame("刷題趣！");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.setIconImage(Resource.icon.getImage());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.dispose();

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        });

        questionLabel.setBounds(50, 50, 500, 30);
        frame.add(questionLabel);
        scoreLabel.setBounds(0, 300, 100, 50);
        frame.add(scoreLabel);
        timeLabel.setBounds(0, 280, 100, 50);
        frame.add(timeLabel);

        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JButton();
            int x = (i % 2 == 0) ? 100 : 300;
            int y = 100 + (i / 2) * 60;
            answerButtons[i].setBounds(x, y, 150, 40);
            final int id = i;
            answerButtons[i].addActionListener(e -> {
                QuestionWithAnswer question = questionSet.get(runningQuestionID);

                QuizAnswerResponse qar = new QuizAnswerResponse();
                qar.choice_id = id;
                qar.send_timestamp = e.getWhen();
                score += Server.calculateScore(qar, question.answer, startTimestamp);
                runningQuestionID++;

                if (runningQuestionID >= questionSet.size()) {
                    stopAndShowLeaderboard();
                } else {
                    updateUI(questionSet.get(runningQuestionID));
                    startTimestamp = System.currentTimeMillis();
                }
            });
            frame.add(answerButtons[i]);
        }

        updateUI(questionSet.get(runningQuestionID));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        startTimestamp = System.currentTimeMillis();
    }

    private void updateUI(QuestionWithAnswer question) {
        questionLabel.setText(question.question);
        scoreLabel.setText("Score: " + score);
        timeLabel.setText("Time: 10");

        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(question.getOption(i));
        }
    }

    private void stopAndShowLeaderboard() {
        Leaderboard.Player me = new Leaderboard.Player("Player", score);
        new Leaderboard(List.of(me), me);
        frame.dispose();
    }

    private static TimerTask sendInvalidAnswer(){
        return new TimerTask() {
            public void run() {

            }
        };
    }
}

class GetQuizDialog extends JDialog {
    private QuestionSet res;

    public static QuestionSet get() {
        GetQuizDialog dialog = new GetQuizDialog();
        QuestionSet res = dialog.res;
        dialog.dispose();

        return res;
    }

    private GetQuizDialog() {     
        setLayout(new BorderLayout());
        setSize(250, 130);
        setResizable(false);
        setTitle("Single Player");
        setIconImage(Resource.icon.getImage());

        JPanel buttonPanel = new JPanel();
        JButton local = new JButton("Local");
        JButton remote = new JButton("Remote");
        JLabel message = new JLabel("Get quiz from", SwingConstants.CENTER);

        add(message, BorderLayout.CENTER);

        final GetQuizDialog self = this;

        local.addActionListener(e -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz File (.quiz)", "quiz"));

                res = new QuestionSet(new String(Files.readString(fileChooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)));
                self.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load quiz.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (CorruptedQuestionsException ex) {
                JOptionPane.showMessageDialog(this, String.format("Failed to parse quiz: %s", ex.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
            } 
        });

        remote.addActionListener(e -> {
            GetQuizHandler handler = new GetQuizHandler();
            new LoginDialog(null, handler).dispose();
            if (handler.questionSet != null) {
                res = handler.questionSet;
                
            }
        });
        
        buttonPanel.add(local);
        buttonPanel.add(remote);

        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}

class GetQuizHandler extends LoginHandler {
    public QuestionSet questionSet;
    private JFrame parent;

    @Override
    public void login(LoginDialog dialog, String address, String name) {
        try (Socket socket = new Socket(address, 12345)) {
            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream())) {
                out.writeUTF("$");
                questionSet = new QuestionSet(in.readUTF());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (CorruptedQuestionsException e) {

        }
    }
}