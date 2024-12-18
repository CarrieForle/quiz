package utils;

import java.util.ArrayList;
import java.util.List;
import utils.exceptions.*;

public class QuestionSet {
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

        this.name = popUntil(contents, "\n");

        while (contents.length() > 0) {
            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
            }

            contents.delete(0, 1);

            if (!contents.substring(0, 2).equals("$$")) {
                throw new CorruptedQuestionsException(String.format("Expected token `$$`. Found `%s`", contents.substring(0, 2)));
            }

            contents.delete(0, 2);

            QuestionWithAnswer question = new QuestionWithAnswer();
            question.question = popUntil(contents, "::::");
            question.answer = Integer.parseInt(contents.substring(0, 1));
            contents.delete(0, 1);

            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
            }

            contents.delete(0, 1);

            for (int i = 0; i < 4; i++) {
                question.setOptions(i, popUntil(contents, ":::"));
            }

            if (!contents.substring(0, 1).equals("\n")) {
                throw new CorruptedQuestionsException(String.format("Expected token `\\n`. Found `%s`", contents.substring(0, 1)));
            }

            contents.delete(0, 1);

            this.questions.add(question);
        }
    }

    private static String popUntil(StringBuilder sb, String delimiter) {
        int delimiter_pos = sb.indexOf(delimiter);
        String res = sb.substring(0, delimiter_pos);
        sb.delete(0, delimiter_pos + delimiter.length());

        return res;
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