package utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import quiz.Server;
import utils.exceptions.*;

public class QuestionSet {
    public static void main(String[] args) {
        try {
            QuestionSet qs = Server.loadQuestions(Path.of("quiz_questions/lol.quiz"));

            for (QuestionWithAnswer q : qs.questions) {
                System.out.println(q.question);

                for (int i = 0; i < 4; i++) {
                    System.out.println(q.getOption(i));
                }

                System.out.println(q.answer);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String name;
    protected List<QuestionWithAnswer> questions = new ArrayList<>();

    public List<QuestionWithAnswer> getQuestions() {
        return this.questions;
    }

    public QuestionWithAnswer get(int i) {
        return this.questions.get(i);
    }

    public int size() {
        return this.questions.size();
    }

    public QuestionSet() {
        
    }

    public QuestionSet(String s) throws CorruptedQuestionsException {
        StringBuilder contents = new StringBuilder(s);

        this.name = popUntilNewLine(contents);

        while (contents.length() > 0) {
            String read = "";

            if (!(read = popUntilNewLine(contents)).isEmpty()) {
                throw new CorruptedQuestionsException(String.format("Expected newline. Found `%s`", read));
            }

            if (!(read = popUntil(contents, "$$")).isEmpty()) {
                throw new CorruptedQuestionsException(String.format("Expected token `$$`. Found `%s`", contents.substring(0, 2)));
            }

            QuestionWithAnswer question = new QuestionWithAnswer();
            question.question = popUntil(contents, "::::");
            read = popUntilNewLine(contents);

            try {
                question.answer = Integer.parseInt(read);
            } catch (NumberFormatException e) {
                throw new CorruptedQuestionsException(String.format("Expected integer. Found `%s`", read));
            }

            for (int i = 0; i < 4; i++) {
                question.setOptions(i, popUntil(contents, ":::"));
            }

            if (!(read = popUntilNewLine(contents)).isEmpty()) {
                throw new CorruptedQuestionsException(String.format("Expected newline. Found `%s`", read));
            }

            this.questions.add(question);
        }
    }

    private static String popUntil(StringBuilder sb, String delimiter) throws CorruptedQuestionsException {
        try {
            int delimiter_pos = sb.indexOf(delimiter);
            String res = sb.substring(0, delimiter_pos);
            sb.delete(0, delimiter_pos + delimiter.length());
    
            return res;
        } catch (StringIndexOutOfBoundsException e) {
            throw new CorruptedQuestionsException(String.format("Unexpected end of file while locating %s", delimiter));
        }
    }

    private static String popUntilNewLine(StringBuilder sb) throws CorruptedQuestionsException {
        try {
            int delimiter_pos = sb.indexOf("\n");
            String res = sb.substring(0, delimiter_pos);
            sb.delete(0, delimiter_pos + 1);

            if (res.endsWith("\r")) {
                res = res.substring(0, res.length() - 1);
            }

            return res;
        } catch (StringIndexOutOfBoundsException e) {
            throw new CorruptedQuestionsException("Unexpected end of file while locating newline");
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        res.append(String.format("%s\n", this.name));

        for (QuestionWithAnswer question : this.questions) {
            res.append(String.format("\n%s\n", question.toString()));
        }

        return res.toString();
    }
}