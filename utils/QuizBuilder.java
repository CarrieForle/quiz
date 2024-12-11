package utils;

import java.util.ArrayList;
import java.util.List;

import utils.exceptions.CorruptedQuestionsException;

public class QuizBuilder {
    static public class PartialQuestionWithAnswer extends QuestionWithAnswer implements Cloneable {
        // Get a list of string representation of incompleted fields separated by newline
        // Return empty list if every field is complete.
        public List<String> getIncompleteField() {
            List<String> res = new ArrayList<>();

            if (this.question == null || this.question.isEmpty()) {
                res.add("question");
            }

            for (int i = 0; i < this.options.length; i++) {
                if (this.options[i] == null || this.options[i].isEmpty()) {
                    res.add(String.valueOf(i));
                }
            }

            return res;
        }

        public boolean is_complete() {
            return this.getIncompleteField().isEmpty();
        }

        public QuestionWithAnswer toQuestionWithAnswer() {
            if (!this.is_complete()) {
                return null;
            }

            QuestionWithAnswer res = new QuestionWithAnswer();
            res.question = this.question;
            res.answer = this.answer;
            res.setOptions(this.options);

            return res;
        }

        @Override
        public PartialQuestionWithAnswer clone() {
            PartialQuestionWithAnswer copied = new PartialQuestionWithAnswer();

            // OK: Integer is immutable
            copied.answer = this.answer;
            copied.question = this.question;
            copied.options = this.options.clone();

            return copied;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();

            res.append("$$");
            res.append(this.question);
            res.append("::::");
            res.append(String.valueOf(this.answer));
            res.append("\n");

            for (String option : this.options) {
                res.append(option);
                res.append(":::");
            }

            return res.toString();
        }
    }    

    private List<PartialQuestionWithAnswer> questions = new ArrayList<>();
    private String name;

    private QuizBuilder() {

    }

    public QuizBuilder(String s) throws CorruptedQuestionsException {
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

            PartialQuestionWithAnswer question = new PartialQuestionWithAnswer();
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

    // Create a new QuizBuilder with 1 empty question
    public static QuizBuilder init(String name) {
        QuizBuilder res = new QuizBuilder();
        res.name = name;
        res.append_new();

        return res;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        res.append(String.format("%s\n", this.name));

        for (PartialQuestionWithAnswer question : this.questions) {
            res.append(String.format("\n%s\n", question.toString()));
        }

        return res.toString();
    }

    public QuestionSet toQuestionSet() {
        QuestionSet questionSet = new QuestionSet();

        questionSet.name = this.name;
        List<QuestionWithAnswer> questions = questionSet.getQuestions();

        for (PartialQuestionWithAnswer question : this.questions) {
            questions.add(question.toQuestionWithAnswer());
        }

        return questionSet;
    }

    public PartialQuestionWithAnswer insert_new(int i) {
        PartialQuestionWithAnswer q = new PartialQuestionWithAnswer();
        this.questions.add(i, q);

        return q;
    }

    public PartialQuestionWithAnswer append_new() {
        PartialQuestionWithAnswer q = new PartialQuestionWithAnswer();
        this.questions.add(q);

        return q;
    }

    public PartialQuestionWithAnswer insert_copy_of(PartialQuestionWithAnswer q, int i) {
        this.questions.add(i, q.clone());
        
        return q;
    }

    public boolean setName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        this.name = name;

        return true;
    }

    public String getName() {
        return this.name;
    }

    public PartialQuestionWithAnswer get(int i) {
        return this.questions.get(i);
    }

    public int size() {
        return this.questions.size();
    }

    public void remove(Object o) {
        this.questions.remove(o);
    }

    public int indexOf(Object o) {
        return this.questions.indexOf(o);
    }
}