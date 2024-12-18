package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import quiz.Client;

import java.io.*;
import java.net.*;
import java.util.TimerTask;
import java.util.Timer;

public class MultiplayerClient {
    private JFrame frame;
    private TimerTask tt;
    private Timer t = new Timer();
    private Timer timer;

    public static void main(String[] args) {
        try {
            String address = "0.0.0.0";
            String name = "Bob";
            new MultiplayerClient(new Socket(address, 12345), name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MultiplayerClient(Socket socket, String name) throws IOException {
        try {
            frame = new JFrame("刷題趣！");
            frame.setSize(600, 550);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setIconImage(Resource.icon.getImage());
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("w");
                    frame.dispose();

                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }

                    new MainMenu();
                }
            });

            Client p = new Client(socket);
            p.setName(name);

            JPanel questionPanel = new JPanel(new BorderLayout());
            JPanel infoPanel = new JPanel(new BorderLayout(50, 10));

            JTextArea questionArea = getJTextArea();
            questionArea.setText("Waiting for a question...");
            questionPanel.add(new JScrollPane(questionArea), BorderLayout.CENTER);

            JProgressBar timebar = new JProgressBar();
            timebar.setValue(100);
            infoPanel.add(timebar, BorderLayout.CENTER);
            JLabel scoreLabel = new JLabel("Score: 0");
            infoPanel.add(scoreLabel, BorderLayout.WEST);
            JLabel timeLabel = new JLabel("Time: ", JLabel.CENTER);
            infoPanel.add(timeLabel, BorderLayout.SOUTH);
            JLabel rankLabel = new JLabel("Rank: 1");
            infoPanel.add(rankLabel, BorderLayout.EAST);

            questionPanel.add(infoPanel, BorderLayout.SOUTH);
            
            JPanel buttonPanel = new JPanel();
            JTextArea[] optionAreas = new JTextArea[4];
            JButton[] answerButtons = new JButton[4];
            buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

            for (int i = 0; i < 4; i++) {
                JPanel panel = new JPanel(new BorderLayout(0, 10));
                optionAreas[i] = getJTextArea();
                answerButtons[i] = new JButton(String.valueOf((char)(65 + i)));
                panel.add(new JScrollPane(optionAreas[i]), BorderLayout.CENTER);
                panel.add(answerButtons[i], BorderLayout.SOUTH);
                buttonPanel.add(panel);

                final int id = i;
                answerButtons[i].addActionListener(e -> {
                    try {
                        timer.cancel();
                        if (tt.cancel()) {
                            timer.cancel();
                            p.writeAns(id);
                            long timestamp = e.getWhen();
                            p.writeTimeStamp(timestamp);
                        }
                        if (p.CheckEnd()) {
                            System.out.printf("sus");
                        }

                        int score = p.getScore();
                        System.out.printf("分數為%d", score);
                        scoreLabel.setText("Score: " + score);
                        int rank = p.getRank();
                        System.out.printf("名次為%d", rank);
                        rankLabel.setText("Rank: " + rank);
                        String question = p.getQuestion();
                        String[] options = p.getOptions();
                        long clientTimestamp = p.getTimeStamp();
                        tt = sendInvalidAnswerInSecond(socket);
                        t.schedule(tt, clientTimestamp);
                        questionArea.setText(question);

                        for (int j = 0; j < 4; j++) {
                            optionAreas[j].setText(options[j]);
                        }
                        timer = new Timer();
                        timer.scheduleAtFixedRate(new TimerTask() {
                            int timeLeft = 10;

                            @Override
                            public void run() {
                                timeLabel.setText(String.valueOf(timeLeft));
                                timeLeft--;
                                if (timeLeft <= 0) {
                                    timer.cancel();
                                }
                            }
                        }, 0, 1000);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, questionPanel, buttonPanel);
            pane.setResizeWeight(0.5);
            pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            frame.add(pane);

            new Thread(() -> {
                try {
                    String question = p.getQuestion();
                    String[] options = p.getOptions();
                    long clientTimestamp = p.getTimeStamp();
                    tt = sendInvalidAnswerInSecond(socket);
                    t.schedule(tt, clientTimestamp);
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        int timeLeft = 10;

                        @Override
                        public void run() {
                            timeLabel.setText(String.valueOf(timeLeft));
                            timeLeft--;
                            if (timeLeft <= 0) {
                                timer.cancel();
                            }
                        }
                    }, 0, 1000);
                    questionArea.setText(question);

                    for (int j = 0; j < 4; j++) {
                        optionAreas[j].setText(options[j]);
                    }
                } catch (IOException e) {

                }
            }).start();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException e) {
            frame.dispose();

            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }

            throw e;
        }
    }

    private static TimerTask sendInvalidAnswerInSecond(Socket socket) {
        return new TimerTask() {
            public void run() {
                try {
                    System.out.println("task");
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeInt(-1);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private JTextArea getJTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setFocusable(false);
        area.setEditable(false);

        return area;
    }
}