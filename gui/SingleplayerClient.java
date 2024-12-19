package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import quiz.Server;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import java.net.Socket;

import utils.*;
import utils.exceptions.CorruptedQuestionsException;

public class SingleplayerClient extends AnswerFrame {
    private int score = 0;
    private QuestionSet questionSet;
    private QuestionWithAnswer running;
    private Iterator<QuestionWithAnswer> iterator;
    private long running_timestamp = 0;

    public static void main(String[] args) {
        // try {
        //     new SingleplayerClient(Server.loadQuestions(Path.of("quiz_questions/程式設計與運算思維 Programming.quiz")));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // } catch (CorruptedQuestionsException e) {

        // }

        SingleplayerClient.runQuizDialog(null);
    }

    public SingleplayerClient(QuestionSet questionSet) {
        this.questionSet = questionSet;
        iterator = questionSet.getQuestions().iterator();
        running = iterator.next();
        start();
    }

    public static void runQuizDialog(Window parent) {
        QuestionSet qs = GetQuizDialog.get(parent);

        if (qs != null) {
            new SingleplayerClient(qs);
            
            if (parent != null) {
                parent.dispose();
            }
        }
    }

    @Override
    protected int getScore() {
        return score;
    }

    protected int getRank() {
        return 1;
    }

    @Override
    protected Question getNextQuestion() {
        if (iterator.hasNext()) {
            running = iterator.next();
            running_timestamp = System.currentTimeMillis();
            return running;
        } else {
            return null;
        }
    }

    @Override
    protected void onAnswering(int id, ActionEvent e) {
        QuizAnswerResponse qar = new QuizAnswerResponse();
        qar.choice_id = id;
        qar.send_timestamp = e.getWhen();

        score += Server.calculateScore(qar, running.answer, running_timestamp);
    }

    @Override
    protected void onTimeExceed() {
        
    }
}

class GetQuizDialog extends JDialog {
    public QuestionSet res;

    public static QuestionSet get(Window parent) {
        GetQuizDialog dialog = new GetQuizDialog(parent);
        QuestionSet res = dialog.res;

        return res;
    }

    private GetQuizDialog(Window parent) {
        super(parent, "Singleplayer", JDialog.ModalityType.DOCUMENT_MODAL);
        setLayout(new BorderLayout());
        setSize(250, 130);
        setResizable(false);
        setIconImage(Resource.icon.getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        JButton local = new JButton("Local");
        JButton remote = new JButton("Remote");
        JLabel message = new JLabel("Get quiz from", SwingConstants.CENTER);
        add(message, BorderLayout.CENTER);

        local.addActionListener(e -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz File (.quiz)", "quiz"));
                
                if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
                    res = new QuestionSet(new String(Files.readString(fileChooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)));
                    this.dispose();
                }
            } catch (IOException ex) {
                Common.errorMessage(this, "Failed to upload quiz", ex);
            } catch (CorruptedQuestionsException ex) {
                Common.errorMessage(this, "Failed to parse quiz", ex);
            } 
        });

        remote.addActionListener(e -> {
            RemoteQuizHandler handler = new RemoteQuizHandler();
            new LoginDialog(this, handler);

            if (handler.questionSet != null) {
                res = handler.questionSet;
                this.dispose();
            }
        });
        
        buttonPanel.add(local);
        buttonPanel.add(remote);

        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}

class RemoteQuizHandler extends LoginHandler {
    public QuestionSet questionSet;

    @Override
    public void login(LoginDialog dialog, String address, String name) {
        try (Socket socket = new Socket(address, 12345)) {
            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream())) {
                out.writeUTF("$");
                questionSet = new QuestionSet(in.readUTF());
                dialog.dispose();
            }
        } catch (IOException e) {
            Common.connectionFailedMessage(dialog, e);
        } catch (CorruptedQuestionsException e) {
            Common.errorMessage(dialog, "Failed to parse quiz");
        }
    }
}