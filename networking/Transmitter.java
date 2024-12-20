package networking;

import java.io.*;
import java.net.Socket;
import java.time.Duration;

import utils.QuizAnswerResponse;
import java.util.ArrayList;

import gui.Leaderboard;
import utils.Question;

public class Transmitter implements AutoCloseable {
    private final Messenger m;

    public Transmitter(Messenger m) {
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

    public void writeCommand(String s) throws IOException {
        this.m.writeCommand(s);
    }

    @Override
    public void close() throws IOException {
        this.m.close();
    }

    public Messenger getMessenger() {
        return this.m;
    }

    public Socket getSocket() {
        return this.m.getSocket();
    }
}