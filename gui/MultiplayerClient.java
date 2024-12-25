package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import networking.ClientMessenger;
import quiz.Client;
import utils.*;
import utils.HistoryGame.Play;
import utils.HistoryGame.Metadata;

public class MultiplayerClient extends AnswerFrame {
    private Thread readIncoming = new Thread();
    private QuestionWithAnswer question = new QuestionWithAnswer();
    private QuestionSet quiz = new QuestionSet();
    private List<Play> plays = new ArrayList<Play>();
    private Play currentPlay = new Play();
    private JTextArea chat = new JTextArea("You joined the game\n");
    private JTextField inputField = new JTextField();
    private Client p;
    private String name;
    private int questionCount = 0;
    private int score = 0;
    private int rank = 1;
    private AtomicInteger myAnswer = new AtomicInteger(-1);
    private boolean isEnd = false;
    private Instant questionTimestamp = null;
    private int timeLimit = 10000;
    private List<Leaderboard.Player> leaderboard;
    private final InetSocketAddress address;

    public static void main(String[] args) {
        try {
            String address = "0.0.0.0";
            String name = "Bob";
            new MultiplayerClient(new Socket(address, 12345), name, new InetSocketAddress("127.0.0.1", 12345));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MultiplayerClient(Socket socket, String name, InetSocketAddress address) {
        this.address = address;
        chat.setEditable(false);
        chat.setLineWrap(true);

        chat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                chat.getCaret().setVisible(false);
            }
        });

        JScrollPane chatPane = new JScrollPane(chat);

        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inputPanel.add(inputField);

        AbstractAction sendTextAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText().trim();

                if (message.isEmpty()) {
                    return;
                }

                try {
                    p.message(name, inputField.getText());
                } catch (IOException ex) {
                    disconnect(ex);
                }

                inputField.setText("");
            }
        };

        inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        inputField.getActionMap().put("send", sendTextAction);

        JButton inputButton = new JButton("Send");
        inputButton.addActionListener(sendTextAction);

        inputPanel.add(inputButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        chatPanel.add(chatPane, BorderLayout.CENTER);
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
                return;
            }

            frame.setVisible(true);
            
            // UI won't display without thread.
            Thread t = new Thread(() -> {
                try {
                    quiz.name = p.getQuizName();
                    questionCount = p.getQuestionCount();
                    readQuestion();
                    start();
                } catch (IOException e) {
                    disconnect(e);
                }
            });

            t.start();
        } catch (IOException e) {
            disconnect(e);
        }
    }

    private void addChat(String contents) {
        String s = chat.getText();
        chat.setText(String.format("%s%s\n", s, contents));
    }
    
    public void addChat(String who, String contents) {
        this.addChat(String.format("%s: %s", who, contents));
    }

    public void joinChat(String who) {
        this.addChat(String.format("%s joined the game", who));
    }

    public void leaveChat(String who) {
        this.addChat(String.format("%s left the game", who));
    }

    public void startIn(int start_in) {
        this.addChat(String.format("Start in %d seconds", start_in / 1000));
        countDownTimebar(start_in, true);
    }

    public void notEnoughPlayer() {
        this.addChat("Wait for more players...");
        cancelCountDown("0.0", 100);
    }

    @Override
    protected Question getNextQuestion() {
        return question;
    }

    private void readQuestion() throws IOException {
        if (isEnd) {
            leaderboard = p.leaderborad();
            question = null;
            System.out.printf("sus");
            return;
        }

        question.question = p.getQuestion();
        question.setOptions(p.getOptions());
        timeLimit = p.getTimeStamp();
        questionTimestamp = Instant.now();

        readIncoming = new Thread(() -> {
            try {
                receiveData();
            } catch (IOException e) {
                disconnect(e);
            }
        });

        readIncoming.start();
    }

    @Override
    protected void onAnswering(int id, ActionEvent e) {
        try {
            p.writeAns(id);
            currentPlay.choiceId = id;
            myAnswer.set(id);

            long timestamp = e.getWhen();
            currentPlay.timeRemained = timeLimit - ((int) (timestamp - questionTimestamp.toEpochMilli()));
            p.writeTimeStamp(timestamp);
            readIncoming.join();
        } catch (IOException ex) {
            disconnect(ex);
        } catch (InterruptedException ex) {

        }
    }

    @Override
    protected void onWindowClosing(WindowEvent e) {
        try {
            p.close();

            if (plays.size() > 0) {
                saveHistory();
            }
        } catch (IOException ex) {
            disconnect(ex);
        }
    }

    @Override
    protected void onTimeExceed() {
        try {
            p.writeAns(-1);
            myAnswer.set(-1);
            p.writeTimeStamp(0);
            currentPlay = Play.TIME_EXCEEDED;
            readIncoming.join();
        } catch (IOException e) {
            disconnect(e);
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected int getAnswer() {
        return question.answer;
    }

    @Override
    protected void onRoundEnd() {
        quiz.getQuestions().add(question);
        question = new QuestionWithAnswer();
        plays.add(currentPlay);
        currentPlay = new Play();
        countDownTimebar(4000, false);
        
        try {
            readQuestion();
        } catch (IOException e) {
            disconnect(e);
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

        Leaderboard.CorrectPlayer me = null;
        me = new Leaderboard.CorrectPlayer(name, score, getCorrectAnswerCount(), questionCount);

        new Leaderboard(leaderboard, me) {
            @Override
            protected void onContinue() {
                MainMenu menu = new MainMenu();
                LoginDialog.get(menu.getFrame(), new MultiplayerLoginHandler(menu.getFrame()));
            }
        };
    }

    @Override
    protected int getTimeLimit() {
        return timeLimit;
    }

    @Override
    protected int getQuestionCount() {
        return questionCount;
    }

    @Override
    protected void saveHistory() throws IOException {
        HistoryStorage.save(new HistoryGame(quiz, plays, new Metadata(name, address.toString(), score, rank)));
    }

    @Override
    protected String getQuizName() {
        return quiz.name;
    }

    @Override
    protected void onWindowClosed(WindowEvent e) {
        
    }

    private void receiveData() throws IOException {
        isEnd = p.checkEnd();
        question.answer = p.getAnswer();

        int oldScore = score;
        score = p.getScore();
        currentPlay.scoreOffset = score - oldScore;
        System.out.printf("分數為%d", score);
        rank = p.getRank();
        System.out.printf("名次為%d", rank);
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
