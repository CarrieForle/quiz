package utils;

public class QuizAnswerResponse {
    public int choice_id;

    // In milliseconds (unused)
    // TODO remove it if it's replaced by Server timing
    public long send_timestamp;

    public boolean is_correct(int answer_id) {
        return this.choice_id == answer_id;
    }
}