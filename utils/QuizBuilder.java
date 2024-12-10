package utils;

import java.util.ArrayList;
import java.util.List;

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

            res.append(str_or_unit(this.question));
            res.append("::::");
            res.append(str_or_unit(String.valueOf(this.answer)));
            res.append("\n");

            for (String option : this.options) {
                res.append(str_or_unit(option));
                res.append(":::");
            }

            return res.toString();
        }

        private static String str_or_unit(String s) {
            if (s == null) {
                return String.valueOf((char) 31);
            } else {
                return s;
            }
        }
    }
    
    // Create a new QuizBuilder with 1 empty question
    public static QuizBuilder init(String s) {
        QuizBuilder res = new QuizBuilder(s);
        res.append_new();

        return res;
    }

    private List<PartialQuestionWithAnswer> questions = new ArrayList<>();
    private String name;

    public QuizBuilder(String name) {
        this.name = name;
    }
    
    // Call this method when the quiz might be incomplete
    public String toPartialQuizString() {
        StringBuilder res = new StringBuilder();

        // 31 is unit separater
        res.append(String.format("%c%s\n\n", (char) 31, this.name));

        for (PartialQuestionWithAnswer question : this.questions) {
            res.append(String.format("%s\n\n", question.toString()));
        }

        return res.toString();
    }

    // Call this method when the quiz is complete
    public String toQuizString() {
        StringBuilder res = new StringBuilder();

        // 31 is unit separater
        res.append(String.format("%s\n", this.name));

        for (PartialQuestionWithAnswer question : this.questions) {
            res.append(String.format("%s\n", question.toQuestionWithAnswer().toString()));
        }

        return res.toString();
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