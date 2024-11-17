package utils;

public class QuizAnswerResponse {
    public int choice_id;

    // In milliseconds
    public long remaining_time;

    public boolean is_correct(int answer_id) {
        return this.choice_id == answer_id;
    }
}