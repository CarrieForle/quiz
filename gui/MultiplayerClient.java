package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.List;

import javax.swing.*;

import networking.ClientMessenger;
import quiz.Client;
import utils.Common;
import utils.Question;

public class MultiplayerClient extends AnswerFrame {
    private Question question = new Question();
    private JTextArea chat = new JTextArea("You joined the server\n");
    private JTextField inputField = new JTextField();
    private Client p;
    private String name;
    private int score = 0;
    private int rank = 1;
    private int correctAnswer = 1;
    private int timeLimit = 10000;
    private List<Leaderboard.Player> leaderboard;

    public static void main(String[] args) {
        try {
            String address = "0.0.0.0";
            String name = "Bob";
            new MultiplayerClient(new Socket(address, 12345), name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MultiplayerClient(Socket socket, String name) {
        chat.setEditable(false);
        chat.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inputPanel.add(inputField);

        JButton inputButton = new JButton("Send");
        inputButton.addActionListener(e -> {
            try {
                p.message(name, inputField.getText());
                inputField.setText("");
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        inputPanel.add(inputButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        chatPanel.add(chat, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane newPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPane, chatPanel);
        newPane.setResizeWeight(0.6);
        newPane.setDividerLocation(0.1);
        frame.add(newPane);
        frame.setSize(800, 550);

        try {
            this.name = name;
            p = new Client(new ClientMessenger(socket, this));
            p.setName(name);

            String response = p.getNameResponse();

            if (!response.equals("OK")) {
                Common.errorMessage(frame, response);
                frame.dispose();
                new MainMenu();
                return;
            }

            frame.setVisible(true);

            // UI won't display without thread.
            Thread t = new Thread(() -> {
                start();
            });

            t.start();
        } catch (IOException e) {
            disconnect(e);
        }
    }
    
    public void addChat(String who, String contents) {
        String s = chat.getText();
        chat.setText(String.format("%s%s: %s\n", s, who, contents));
    }

    @Override
    protected Question getNextQuestion() {
        try {
            if (leaderboard != null) {
                return null;
            }
            
            question.question = p.getQuestion();
            question.setOptions(p.getOptions());
            timeLimit = p.getTimeStamp();
        } catch (IOException e) {
            disconnect(e);
        }

        return question;
    }

    @Override
    protected void onAnswering(int id, ActionEvent e) {
        try {
            p.writeAns(id);
            long timestamp = e.getWhen();
            p.writeTimeStamp(timestamp);
            receiveData();
        } catch (IOException ex) {
            disconnect(ex);
        }
    }

    @Override
    protected void onWindowClosing(WindowEvent e) {
        try {
            p.close();
        } catch (IOException ex) {
            disconnect(ex);
        }
    }

    @Override
    protected void onTimeExceed() {
        try {
            p.writeAns(-1);
            p.writeTimeStamp(0);
            receiveData();
        } catch (IOException e) {
            disconnect(e);
        }
    }

    @Override
    protected int getAnswer() {
        return correctAnswer;
    }

    @Override
    protected void onRoundEnd() {
        countDownTimebar(4000);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected int getScore() {
        return score;
    }

    @Override
    protected int getRank() {
        return rank;
    }

    @Override
    protected void showLeaderboard() {
        try {
            p.close();
        } catch (IOException ex) {

        }

        Leaderboard.Player me = null;

        for (Leaderboard.Player player : leaderboard) {
            if (player.name.equals(name) && player.score == score) {
                me = player;
                break;
            }
        }

        if (me == null) {
            System.out.println("I am not in the leaderboard");
            return;
        }

        new Leaderboard(leaderboard, me) {
            @Override
            protected void onContinue() {
                MainMenu menu = new MainMenu();
                new LoginDialog(menu.getFrame(), new MultiplayerLoginHandler(menu.getFrame()));
            }
        };
    }

    @Override
    protected int getTimeLimit() {
        return timeLimit;
    }

    private void receiveData() throws IOException {
        boolean isEnd = p.checkEnd();

        correctAnswer = p.getAnswer();
        score = p.getScore();
        System.out.printf("分數為%d", score);
        rank = p.getRank();
        System.out.printf("名次為%d", rank);

        if (isEnd) {
            leaderboard = p.leaderborad();
            System.out.printf("sus");
        }
    }

    private void disconnect(IOException e) {
        if (!p.isClosed()) {
            Common.errorMessage(frame, "Disconnected");
            try {
                p.close();
            } catch (IOException ex) {

            }
        }

        frame.dispose();
        new MainMenu();
    }
}
