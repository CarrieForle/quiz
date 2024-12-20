package gui;

import java.awt.event.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import networking.ClientMessenger;
import quiz.Client;
import utils.Common;
import utils.Question;

public class MultiplayerClient extends AnswerFrame {
    private Question question = new Question();
    private Client p;
    private String name;
    private int score = 0;
    private int rank = 1;
    private List<Leaderboard.Player> leaderboard;
    private boolean is_in_game = false;

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
        try {
            this.name = name;
            p = new Client(new ClientMessenger(socket));
            p.setName(name);

            String response = p.getNameResponse();

            if (!response.equals("OK")) {
                Common.errorMessage(getFrame(), response);
                getFrame().dispose();
                new MainMenu();
                return;
            }

            setVisible(true);

            // UI won't display without thread.
            Thread t = new Thread(() -> {
                start();
                this.is_in_game = true;
            });

            t.start();
        } catch (IOException e) {
            disconnect(e);
        }
    }

    @Override
    protected Question getNextQuestion() {
        try {
            if (leaderboard != null) {
                return null;
            }
            
            question.question = p.getQuestion();
            question.setOptions(p.getOptions());
            long timeLimit = p.getTimeStamp();
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

    private void receiveData() throws IOException {
        boolean isEnd = p.checkEnd();

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
            Common.errorMessage(getFrame(), "Disconnected");
            try {
                p.close();
            } catch (IOException ex) {

            }
        }

        getFrame().dispose();
        new MainMenu();
    }
}
