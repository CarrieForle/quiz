package networking;

import java.io.*;
import java.net.Socket;
import java.time.Duration;

import utils.QuizAnswerResponse;
import java.util.ArrayList;
import java.util.List;

import gui.Leaderboard;
import utils.Question;

public class Transmitter implements AutoCloseable {
    private final ServerMessenger m;

    public Transmitter(ServerMessenger m) {
        this.m = m;
    }
    
    public void sendQuestion(Question question, Duration duration) throws IOException {
        this.m.writeUTF(question.question);

        for (int i = 0; i < question.getOptionLength(); i++) {
            this.m.writeUTF(question.getOption(i));
        }

        this.m.writeLong(duration.toMillis());
    }

    public QuizAnswerResponse getAnswer() throws IOException {
        QuizAnswerResponse res = new QuizAnswerResponse();

        res.choice_id = this.m.readInt();
        res.send_timestamp = this.m.readLong();

        return res;
    }
    
    public void sendRoundResult(boolean is_over, int score, int ranking) throws IOException {
        this.m.writeBoolean(is_over);
        this.m.writeInt(score);
        this.m.writeInt(ranking);
    }

    public void sendLeaderboard(ArrayList<Leaderboard.Player> players) throws IOException {
        this.m.writeObject(players);
    }

    public void ping() throws IOException {
        this.m.writeCommand("ping");
    }

    @Override
    public void close() throws IOException {
        this.m.close();
    }

    public ServerMessenger getMessenger() {
        return this.m;
    }

    public void updateMessenger(List<? extends Messenger> m) {
        this.m.updateMessenger(m);
    }

    public Socket getSocket() {
        return this.m.getSocket();
    }
}