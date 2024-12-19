package gui;

import java.awt.event.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;

import quiz.Client;
import utils.Question;

public class MultiplayerClient extends AnswerFrame {
    private Question question = new Question();
    private Client p;
    private int score = 0;
    private int rank = 1;

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
        p = new Client(socket);
        p.setName(name);
        setVisible(true);
        start();
    }

    @Override
    protected Question getNextQuestion() {
        try {
            question.question = p.getQuestion();
            question.setOptions(p.getOptions());
            long timeLimit = p.getTimeStamp();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    protected void onWindowClosing(WindowEvent e) {
        try {
            p.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onTimeExceed() {
        try {
            p.writeAns(-1);
            p.writeTimeStamp(0);
            receiveData();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    private void receiveData() throws IOException {
        boolean isEnd = p.CheckEnd();

        score = p.getScore();
        System.out.printf("分數為%d", score);
        rank = p.getRank();
        System.out.printf("名次為%d", rank);

        if (isEnd) {
            p.Leaderborad();
            System.out.printf("sus");
        }
    }
}
