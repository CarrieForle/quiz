package gui;

import javax.swing.*;

import quiz.Client;

import java.io.*;
import java.net.*;

class MultiplayerClient {
    private Socket socket;
    private JFrame frame;
    private BufferedReader in;
    private PrintWriter out;

    public MultiplayerClient(Socket socket, String name) throws IOException {
        Client p = new Client(socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        p.setName(name);
        frame = new JFrame("Multiplayer Mode");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);

        JLabel questionLabel = new JLabel("Waiting for a question...");
        questionLabel.setBounds(50, 50, 500, 30);
        frame.add(questionLabel);
        JLabel scoreLabel = new JLabel("Score : ");
        scoreLabel.setBounds(0,300,50,50);
        frame.add(scoreLabel);
        JLabel rankLabel = new JLabel("Rank : ");
        rankLabel.setBounds(0,320,50,50);
        frame.add(rankLabel);
        JButton[] answerButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JButton();
            int x = (i % 2 == 0) ? 100 : 300;
            int y = 100 + (i / 2) * 60;
            answerButtons[i].setBounds(x, y, 150, 40);
            int finalI = i;
            answerButtons[i].addActionListener(e -> {
                try {
                    p.writeAns(finalI);
                    long timestamp = e.getWhen();
                    p.writeTimeStamp(timestamp);

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

        questionLabel.setText(question);

        for (int j = 0; j < 4; j++) {
            answerButtons[j].setText(options[j]);
        }
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void dispose() {
        frame.dispose();
    }
}