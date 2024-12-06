package networking;

import java.io.*;
import utils.QuizAnswerResponse;
import java.util.List;
import utils.Question;

public class ServerTransmission {
    public static void main(String[] args) {
        ByteArrayOutputStream test = new ByteArrayOutputStream();

        try {
            Question q = new Question();

            q.question = "1+1等於幾？";
            q.setOptions(new String[] {
                "2", "4", "-5", "19"
            });
            
            transmitQuestion(test, q);

            byte[] res = test.toByteArray();

            for (byte b : res) {
                System.out.format("%x ", b);
            }

            System.out.println();

            String s = new String(res);

            System.out.println(s);
        } catch (IOException ex) {

        }
    }

    public static String receiveName(InputStream reader) throws IOException {
        DataInputStream dis = new DataInputStream(reader);

        return dis.readUTF();
    }
    
    public static void transmitQuestion(OutputStream writer, Question question) throws IOException {
        DataOutputStream out = new DataOutputStream(writer);

        out.writeUTF(question.question);

        for (int i = 0; i < question.getOptionLength(); i++) {
            out.writeUTF(question.getOption(i));
        }
    }

    public static QuizAnswerResponse receiveAnswer(InputStream reader) throws IOException {
        DataInputStream in = new DataInputStream(reader);

        QuizAnswerResponse res = new QuizAnswerResponse();

        res.choice_id = in.readInt();
        res.remaining_time = in.readLong();

        return res;
    }
    
    public static void sendRoundResult(OutputStream writer, boolean is_over, int score, int ranking) throws IOException {
        DataOutputStream dos = new DataOutputStream(writer);

        dos.writeBoolean(is_over);
        dos.writeInt(score);
        dos.writeInt(ranking);
    }


    public static void transmitLeaderboard(OutputStream writer, int score, int ranking) throws IOException {
        DataOutputStream dos = new DataOutputStream(writer);
        dos.writeUTF(String.format("%d:%d", score, ranking));
    }
}