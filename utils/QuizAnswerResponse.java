package utils;

public class QuizAnswerResponse {
    public int from_id;
    public int choice_id;

    // In milliseconds
    public int remaining_time;

    public boolean is_correct(int answer_id) {
        return this.choice_id == answer_id;
    }
}