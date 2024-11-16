package networking;

import java.io.*;
import utils.QuizAnswerResponse;
import node.Client;
import java.util.List;

public class ServerTransmission {
    public static void main(String[] args) {
        ByteArrayOutputStream test = new ByteArrayOutputStream();

        try {
            transmitQuestion(test, "Q1+1等於幾？a2\nA4\nA-5\nA2\nA19\n");

            byte[] res = test.toByteArray();

            for (byte b : res) {
                System.out.format("%x ", b);
            }

            System.out.println();

            String s = new String(res);

            System.out.println(s);
        } catch (IOException ex) {

        }

        QuizAnswerResponse res = extractAnswer("1 2 3");
        System.out.format("from who = %d\n", res.from_id);
        System.out.format("for how long = %d\n", res.remaining_time);
        System.out.format("choose which one = %d\n", res.choice_id);
    }
    
    public static void transmitQuestion(OutputStream writer, String question_message) throws IOException {
        DataOutputStream out = new DataOutputStream(writer);

        out.writeUTF(question_message);
    }

    public static QuizAnswerResponse extractAnswer(String response) {
        QuizAnswerResponse res = new QuizAnswerResponse();

        String[] segments = response.split(" ");

        res.from_id = Integer.parseInt(segments[0]);
        res.remaining_time = Integer.parseInt(segments[1]);
        res.choice_id = Integer.parseInt(segments[2]);

        return res;
    }

    private static List<Integer> calculateRanking(List<Client> clients) {
        clients.
    }
}