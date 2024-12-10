package utils;

public class Question {
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
}

class QuestionWithAnswer extends Question {
    public int answer = 0;

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

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