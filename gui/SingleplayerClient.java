package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import quiz.Server;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.net.InetSocketAddress;
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
        SingleplayerClient.runQuizDialog(null);
    }

    public SingleplayerClient(QuestionSet questionSet) {
        this.questionSet = questionSet;
        iterator = questionSet.getQuestions().iterator();
        running = iterator.next();
        start();
        frame.setVisible(true);
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
    protected void showLeaderboard() {
        Leaderboard.Player me = new Leaderboard.Player("You", score);
        new Leaderboard(new ArrayList<Leaderboard.Player>(List.of(me)), me) {
            @Override
            protected void onContinue() {
                SingleplayerClient.runQuizDialog(new MainMenu().getFrame());
            }
        };
    }

    @Override
    protected int getAnswer() {
        return running.answer;
    }

    @Override
    protected void onRoundEnd() {
        countDownTimebar(4000, false);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected int getTimeLimit() {
        return 10000;
    }
}

class GetQuizDialog extends JDialog {
    private static LoginDialog.Info loginInfo;
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
            RemoteQuizHandler handler = new RemoteQuizHandler(this);
            loginInfo = LoginDialog.get(this, handler, loginInfo);

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
    private Window parent;

    public RemoteQuizHandler(Window parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void login(LoginDialog dialog, InetSocketAddress address, String name) {
        try (Socket socket = new Socket(address.getHostString(), address.getPort())) {
            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                out.writeUTF("$");
                LinkedHashMap<String, String> quizs;

                try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
                    quizs = (LinkedHashMap<String, String>) ois.readObject();
                }

                dialog.dispose();
                ChooseQuizDialog chooseQuizDialog = new ChooseQuizDialog(this.parent, quizs, address);
                this.questionSet = chooseQuizDialog.questionSet;
            }
        } catch (IOException e) {
            Common.connectionFailedMessage(dialog, e);
        } catch (ClassNotFoundException e) {

        }
    }
}

class ChooseQuizDialog extends JDialog {
    public QuestionSet questionSet;

    public ChooseQuizDialog(Window parent, LinkedHashMap<String, String> quizs, InetSocketAddress address) {
        super(parent, "Quiz List", JDialog.ModalityType.DOCUMENT_MODAL);
        setSize(400, 400);
        setLayout(new BorderLayout(0, 10));
        setIconImage(Resource.icon.getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] names = quizs.values().toArray(new String[0]);
        String[] ids = quizs.keySet().toArray(new String[0]);

        JList<String> list = new JList<>(names);
        list.setSelectedIndex(0);;
        JButton submitButton = new JButton("Submit");
        add(new JScrollPane(list), BorderLayout.CENTER);

        submitButton.addActionListener(e -> {
            String id = ids[list.getSelectedIndex()];

            try (Socket socket = new Socket(address.getHostString(), address.getPort())) {
                try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream())) {
                        
                    out.writeUTF("$" + id);
                    questionSet = new QuestionSet(in.readUTF());
                    this.dispose();
                }
            } catch (IOException ex) {
                Common.connectionFailedMessage(this, ex);
            } catch (CorruptedQuestionsException ex) {
                Common.errorMessage(this, "Failed to parse quiz from server");
            }
        });

        add(new JLabel("Choose a quiz to play", JLabel.CENTER), BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.add(submitButton);
        add(panel, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}