package utils;

public class QuestionWithAnswer extends Question {
    public int answer = 0;

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
