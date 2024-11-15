package gui;

import javax.swing.*;

public class Score extends JLabel {
    private int score = 0;
    private int ranking = -1;

    public Score() {
        setHorizontalAlignment(SwingConstants.CENTER);
        updateScoreText();
    }

    public void setScore(int score) {
        this.score = score;
        updateScoreText();
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
        updateScoreText();
    }

    public int getRanking() {
        return this.ranking;
    }

    public int getScore() {
        return this.score;
    }

    private void updateScoreText() {
        this.setText(String.format("Score: %d   Ranking: %d", this.score, this.ranking));
    }
}
