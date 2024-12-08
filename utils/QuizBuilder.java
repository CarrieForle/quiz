package utils;

import java.util.ArrayList;
import java.util.List;

public class QuizBuilder {
    class PartialQuestionWithAnswer {
        public Integer answer;
        public String question;
        protected String[] options = new String[4];

        public void setOptions(int i, String s) {
            this.options[i] = s;
        }

        public void setOptions(String[] s) {
            for (int i = 0; i < this.options.length; i++) {
                this.options[i] = s[i];
            }
        }

        public String getOption(int i) {
            return options[i];
        }

        public int getOptionLength() {
            return this.options.length;
        }

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

            if (this.answer == null || this.answer < 0 || this.answer >= this.options.length) {
                res.add("answer");
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
        
        public String toString() {
            StringBuilder res = new StringBuilder();

            res.append(str_or_unit(this.question));
            res.append("::::");
            res.append(str_or_unit(this.answer.toString()));
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

    private List<PartialQuestionWithAnswer> questions = new ArrayList<>();
    private String name;

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
    
    public QuizBuilder(String name) {
        this.name = name;
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
}