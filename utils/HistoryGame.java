package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import gui.HistoryBoard;
import quiz.Server;
import utils.exceptions.CorruptedHistoryException;
import utils.exceptions.CorruptedQuestionsException;

public class HistoryGame {
    public static void main(String[] args) {
        try {
            QuestionSet quiz = Server.loadQuestions(Path.of("quiz_questions/lol.quiz"));

            List<Play> data = List.of(
                new Play(5000, 1, 300),
                new Play(3400, 1, 0),
                new Play(2300, 0, 498)
            );

            HistoryGame game = new HistoryGame(quiz, data, new Metadata(Instant.now(), "Lebron James", "127.0.0.1"));
            game.export(Path.of("lol.quih"));
            new HistoryBoard(null, new HistoryGame(Files.readString(Path.of("lol.quih"), StandardCharsets.UTF_8)));
            System.out.println("lol");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Snapshot {
        public final QuestionWithAnswer question;
        public final Play play;

        private Snapshot(QuestionWithAnswer question, Play play) {
            this.question = question;
            this.play = play;
        }

        @Override
        public String toString() {
            return String.format("%s\n%s", playToString(play), this.question.toString());
        }
    }

    public static class Play {
        public final int timeSpent;
        public final int choiceId;
        public final int scoreOffset;

        public Play(int timeSpent,int choiceId, int scoreOffset){
            this.timeSpent = timeSpent;
            this.choiceId = choiceId;
            this.scoreOffset = scoreOffset;
        }
        @Override
        public String toString() {
            return String.format("%d,%d,%d", this.timeSpent, this.choiceId, this.scoreOffset);
        }
    }

    public static class Metadata {
        public Instant when;
        public String name;
        public String address;

        public Metadata(Instant when, String name, String address) {
            this.when = when;
            this.name = name;
            this.address = address;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("when=%d\n", when.toEpochMilli()));
            sb.append(String.format("name=%s\n", name));
            sb.append(String.format("address=%s\n", address));

            return sb.toString();
        }
    }

    public final Metadata metadata;
    public final QuestionSet quiz;
    public final List<Play> plays;
    
    public HistoryGame(QuestionSet quiz, List<Play> plays, Metadata metadata) {
        this.quiz = quiz;
        this.plays = plays;
        this.metadata = metadata;
    }

    public HistoryGame(String s) throws CorruptedHistoryException {
        StringBuilder sb = new StringBuilder(s);
        List<String> keys = new ArrayList<>(3);
        Instant when = null;
        String name = null;
        String address = null;
        
        keys.add("when");
        keys.add("name");
        keys.add("address");

        while (!keys.isEmpty()) {
            String key = popUntil(sb, "=");

            if (!keys.remove(key)) {
                throw new CorruptedHistoryException(String.format("Invalid metadata %s", key));
            }

            String value = popUntilNewLine(sb);

            if (key.equals("when")) {
                when = Instant.ofEpochSecond(Long.parseLong(value));
            } else if (key.equals("name")) {
                name = value;
            } else if (key.equals("address")) {
                address = value;
            }
        }

        this.metadata = new Metadata(when, name, address);

        String token = "_";
        List<Play> plays = new ArrayList<>();

        while (!(token = popUntilNewLine(sb)).isEmpty()) {
            if (token.equals("null")) {
                plays.add(null);
                continue;
            }

            String[] tokens = token.split(",");

            if (tokens.length != 3) {
                throw new CorruptedHistoryException(String.format("Invalid play data: %s", token));
            }
            
            try {
                int timeSpent = Integer.parseInt(tokens[0]);
                int choiceId = Integer.parseInt(tokens[1]);
                int scoreOffset = Integer.parseInt(tokens[2]);

                plays.add(new Play(timeSpent, choiceId, scoreOffset));
            } catch (NumberFormatException e) {
                throw new CorruptedHistoryException(String.format("Invalid play data: %s", token));
            }
        }

        this.plays = plays;
        
        try {
            this.quiz = new QuestionSet(sb.toString());
        } catch (CorruptedQuestionsException e) {
            throw new CorruptedHistoryException(String.format("Failed to parse quiz data: %s", e.getMessage()));
        }
    }

    private static String popUntil(StringBuilder sb, String delimiter) throws CorruptedHistoryException {
        try {
            int delimiter_pos = sb.indexOf(delimiter);
            String res = sb.substring(0, delimiter_pos);
            sb.delete(0, delimiter_pos + delimiter.length());
    
            return res;
        } catch (StringIndexOutOfBoundsException e) {
            throw new CorruptedHistoryException(String.format("Unexpected end of file while locating %s", delimiter));
        }
    }

    private static String popUntilNewLine(StringBuilder sb) throws CorruptedHistoryException {
        try {
            int delimiter_pos = sb.indexOf("\n");
            String res = sb.substring(0, delimiter_pos);
            sb.delete(0, delimiter_pos + 1);

            if (res.endsWith("\r")) {
                res = res.substring(0, res.length() - 1);
            }

            return res;
        } catch (StringIndexOutOfBoundsException e) {
            throw new CorruptedHistoryException("Unexpected end of file while locating newline");
        }
    }
    
    public Snapshot get(int i) {
        return new Snapshot(this.quiz.get(i), this.plays.get(i));
    }

    public int indexOf(Snapshot snapshot) {
        for (int i = 0; i < this.quiz.size(); i++) {
            if (snapshot.question == this.quiz.get(i) && snapshot.play == this.plays.get(i)) {
                return i;
            }
        }

        return -1;
    }

    public void export(Path filePath) throws IOException {
        Files.writeString(filePath, this.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Play play : this.plays) {
            sb.append(playToString(play));
            sb.append("\n");
        }

        sb.append("\n");

        return metadata.toString() + sb.toString() + this.quiz.toString();
    }

    private static String playToString(Play play) {
        if (play == null) {
            return null;
        } else {
            return play.toString();
        }
    }
}