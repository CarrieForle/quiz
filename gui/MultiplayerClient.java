package gui;

import javax.swing.*;

import quiz.Client;
import utils.OpenMenuOnClosing;

import java.io.*;
import java.net.*;
import java.util.TimerTask;
import java.util.Timer;

public class MultiplayerClient {
    private JFrame frame;
    private TimerTask tt;
    private Timer t = new Timer();

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
            Client p = new Client(socket);
            p.setName(name);
            frame = new JFrame("刷題趣！");
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(null);
            frame.addWindowListener(new OpenMenuOnClosing(frame));

            JLabel questionLabel = new JLabel("Waiting for a question...");
            questionLabel.setBounds(50, 50, 500, 30);
            frame.add(questionLabel);
            JLabel scoreLabel = new JLabel("Score : ");
            scoreLabel.setBounds(0,300,100,50);
            frame.add(scoreLabel);
            JLabel rankLabel = new JLabel("Rank : ");
            rankLabel.setBounds(0,320,100,50);
            frame.add(rankLabel);
            JButton[] answerButtons = new JButton[4];
            for (int i = 0; i < 4; i++) {
                answerButtons[i] = new JButton();
                int x = (i % 2 == 0) ? 100 : 300;
                int y = 100 + (i / 2) * 60;
                answerButtons[i].setBounds(x, y, 150, 40);
                final int id = i;
                answerButtons[i].addActionListener(e -> {
                    try {
                        if (tt.cancel()) {
                            p.writeAns(id);
                            long timestamp = e.getWhen();
                            p.writeTimeStamp(timestamp);
                        }

                        if (p.CheckEnd()) {
                            System.out.printf("sus");
                        }
            
                        int score = p.getScore();
                        System.out.printf("分數為%d", score);
                        scoreLabel.setText("Score : " + score);
                        int rank = p.getRank();
                        System.out.printf("名次為%d", rank);
                        rankLabel.setText("Rank : " + rank);

                        String question = p.getQuestion();
                        String[] options = p.getOptions();
                        long clientTimestamp = p.getTimeStamp();
                        tt = sendInvalidAnswerInSecond(socket);
                        t.schedule(tt, clientTimestamp);
                        questionLabel.setText(question);

                        for (int j = 0; j < 4; j++) {
                            answerButtons[j].setText(options[j]);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                frame.add(answerButtons[i]);
            }
            frame.setVisible(true);
            String question = p.getQuestion();
            String[] options = p.getOptions();
            long clientTimestamp = p.getTimeStamp();
            tt = sendInvalidAnswerInSecond(socket);
            t.schedule(tt, clientTimestamp);

            questionLabel.setText(question);

            for (int j = 0; j < 4; j++) {
                answerButtons[j].setText(options[j]);
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException e) {
            frame.dispose();
            throw e;
        }
    }

    private static TimerTask sendInvalidAnswerInSecond(Socket socket){
        return new TimerTask() {
            public void run() {
                try{
                    System.out.println("task");
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeInt(-1);
                    out.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };
    }
}