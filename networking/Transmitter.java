package networking;

import java.io.*;
import java.net.Socket;
import java.time.Duration;

import utils.QuizAnswerResponse;
import java.util.ArrayList;

import gui.Leaderboard;
import utils.Question;

public class Transmitter {
    private final Socket socket;

    public Transmitter(Socket socket) {
        this.socket = socket;
    }
    
    public void sendQuestion(Question question, Duration duration) throws IOException {
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());

        out.writeUTF(question.question);

        for (int i = 0; i < question.getOptionLength(); i++) {
            out.writeUTF(question.getOption(i));
        }

        out.writeLong(duration.toMillis());
    }

    public QuizAnswerResponse getAnswer() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        QuizAnswerResponse res = new QuizAnswerResponse();

        res.choice_id = in.readInt();
        res.send_timestamp = in.readLong();

        return res;
    }
    
    public void sendRoundResult(boolean is_over, int score, int ranking) throws IOException {
        DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());

        dos.writeBoolean(is_over);
        dos.writeInt(score);
        dos.writeInt(ranking);
    }

    public void sendLeaderboard(ArrayList<Leaderboard.Player> players) throws IOException {
        ObjectOutputStream dos = new ObjectOutputStream(this.socket.getOutputStream());
        dos.writeObject(players);
    }
}