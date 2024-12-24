package gui;

import javax.swing.*;
import javax.swing.border.Border;

import utils.Common;
import utils.Question;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;

public abstract class AnswerFrame {
    private Timer timer = new Timer();
    private TimerTask countdownTask = new TimerTask() {
        @Override
        public void run() {

        }
    };

    private int countdown = 0;
    private int questionNum = 1;
    private int correctAnswerNum = 0;
    protected JFrame frame;
    protected JLabel scoreLabel = new JLabel("Score: 0");
    protected JLabel timeLabel = new JLabel("0.0", JLabel.CENTER);
    protected JLabel rankLabel = new JLabel("Rank: 1");
    protected JTextArea questionArea = getJTextArea();
    protected JScrollPane questionScrollArea = new JScrollPane(questionArea);
    private JProgressBar timebar = new JProgressBar(0, 100);
    protected JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    protected JTextArea[] optionAreas = new JTextArea[4];
    protected JButton[] answerButtons = new JButton[4];
    protected JScrollPane[] optionScrollAreas = new JScrollPane[4];
    private int score = 0;

    public AnswerFrame() {
        frame = new JFrame("刷題趣！") {
            @Override
            public void dispose() {
                super.dispose();
                timer.cancel();
            }
        };

        frame.setSize(600, 550);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Resource.icon.getImage());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing(e);
                frame.dispose();
                new MainMenu();
            }
        });

        questionArea.setText("Wait for the question");
        timebar.setValue(100);

        JPanel questionPanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new BorderLayout(50, 0));
        questionPanel.add(questionScrollArea, BorderLayout.CENTER);

        infoPanel.add(timebar, BorderLayout.CENTER);
        infoPanel.add(scoreLabel, BorderLayout.WEST);
        infoPanel.add(timeLabel, BorderLayout.SOUTH);
        infoPanel.add(rankLabel, BorderLayout.EAST);
        questionPanel.add(infoPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        for (int i = 0; i < 4; i++) {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            optionAreas[i] = getJTextArea();
            answerButtons[i] = new JButton(String.valueOf((char) (65 + i)));
            answerButtons[i].setEnabled(false);
            optionScrollAreas[i] = new JScrollPane(optionAreas[i]);
            optionScrollAreas[i].setBorder(getNormalBorder());

            panel.add(optionScrollAreas[i], BorderLayout.CENTER);
            panel.add(answerButtons[i], BorderLayout.SOUTH);
            buttonPanel.add(panel);

            final int id = i;
            answerButtons[i].addActionListener(e -> {
                optionScrollAreas[id].setBorder(getHighlightBorder(new Color(238, 208, 82)));
                countdownTask.cancel();

                for (JButton button : answerButtons) {
                    button.setEnabled(false);
                }

                Thread t = new Thread(() -> {
                    onAnswering(id, e);
                    endRound(id);
                });

                t.start();
            });
        }

        mainPane.add(questionPanel);
        mainPane.add(buttonPanel);
        mainPane.setResizeWeight(0.5);
        mainPane.setDividerLocation(0.5);
        mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.add(mainPane);
        frame.setLocationRelativeTo(null);
    }
    
    final public void start() {
        updateFields(getNextQuestion());
        updateTitle();
        countDown(getTimeLimit());

        for (JButton button : answerButtons) {
            button.setEnabled(true);
        }
    }

    private JTextArea getJTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setFocusable(false);
        area.setEditable(false);

        return area;
    }

    private Border getHighlightBorder(Color color) {
        Border inner = BorderFactory.createLineBorder(color, 5);
        Border outer = BorderFactory.createEmptyBorder(3, 3, 3, 3);

        return BorderFactory.createCompoundBorder(outer, inner);
    }

    private Border getNormalBorder() {
        Border inner = BorderFactory.createLineBorder(Color.BLACK, 1);
        Border outer = BorderFactory.createEmptyBorder(7, 7, 7, 7);

        return BorderFactory.createCompoundBorder(outer, inner);
    }

    private void endRound(int id) {
        int correct_answer = getAnswer();
        updateTitle();

        for (int j = 0; j < 4; j++) {
            Color color;

            if (j == correct_answer) {
                color = Color.GREEN;
            } else {
                color = Color.RED;
            }

            optionScrollAreas[j].setBorder(getHighlightBorder(color));
        }

        int old_score = score;
        updateInfo();
        int scoreOffset = score - old_score;

        if (id == correct_answer) {
            correctAnswerNum++;
            timeLabel.setText(String.format("Correct +%d", scoreOffset));
            timeLabel.setForeground(new Color(68, 194, 61));
        } else {
            // Time exceeded
            if (id == -1) {
                timeLabel.setText("Time exceeded");
            } else {
                timeLabel.setText("Wrong");
            }

            timeLabel.setForeground(new Color(156, 34, 43));
        }

        onRoundEnd();

        for (JScrollPane area : optionScrollAreas) {
            area.setBorder(getNormalBorder());
        }

        timeLabel.setForeground(Color.BLACK);

        Question question = getNextQuestion();

        for (JButton button : answerButtons) {
            button.setEnabled(true);
        }

        if (question == null) {
            frame.dispose();
            showLeaderboard();

            Thread t = new Thread(() -> {
                try {
                    saveHistory();
                } catch (IOException e) {
                    Common.errorMessage(frame, "Failed to save this game into history", e);
                }
            });

            t.start();
        } else {
            updateFields(question);
            questionNum++;
            updateTitle();
            countDown(getTimeLimit());
        }
    }

    private void updateInfo() {
        score = getScore();
        scoreLabel.setText("Score: " + score);
        rankLabel.setText("Rank: " + getRank());
    }

    private void updateFields(Question question) {
        questionArea.setText(question.question);
        questionScrollArea.getVerticalScrollBar().setValue(0);

        for (int i = 0; i < 4; i++) {
            optionAreas[i].setText(question.getOption(i));
            optionScrollAreas[i].getVerticalScrollBar().setValue(0);
        }
    }
    
    private void countDown(int t) {
        countdownTask.cancel();
        countdown = t;

        countdownTask = new TimerTask() {
            int timeLeft = t;

            @Override
            public void run() {
                timeLabel.setText(String.format("%.1f", timeLeft * 0.001));
                timebar.setValue(timeLeft * 100 / t);

                if (timeLeft <= 0) {
                    cancel();

                    Thread t = new Thread(() -> {
                        for (JButton button : answerButtons) {
                            button.setEnabled(false);
                        }

                        onTimeExceed();
                        endRound(-1);
                    });

                    t.start();
                }

                timeLeft -= 100;
                countdown = timeLeft;
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 100);
    }

    private void updateTitle() {
        frame.setTitle(String.format("刷題趣！ (%d/%d) %s", questionNum, getQuestionCount(), getQuizName()));
    }

    protected void cancelCountDown(String s, int progress) {
        countdownTask.cancel();
        timeLabel.setText(s);
        timebar.setValue(progress);
    }

    protected int getTimebarTime() {
        return countdown;
    }

    protected void countDownTimebar(int fromTime, boolean onLabel) {
        countdownTask.cancel();
        countdown = fromTime;

        countdownTask = new TimerTask() {
            int timeLeft = fromTime;

            @Override
            public void run() {
                timebar.setValue(timeLeft * 100 / fromTime);

                if (onLabel) {
                    timeLabel.setText(String.format("%.1f", timeLeft * 0.001));
                }

                if (timeLeft <= 0) {
                    cancel();
                }

                timeLeft -= 100;
                countdown = timeLeft;
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 100);
    }

    protected int getCorrectAnswerCount() {
        return correctAnswerNum;
    }

    protected void onWindowClosing(WindowEvent e) {

    }
    
    protected void onTimeExceed() {

    }

    protected abstract String getQuizName();
    protected abstract void saveHistory() throws IOException;
    protected abstract int getQuestionCount();
    protected abstract int getAnswer();
    protected abstract void onRoundEnd();
    protected abstract int getTimeLimit();
    protected abstract void showLeaderboard();
    protected abstract int getScore();
    protected abstract int getRank();
    protected abstract Question getNextQuestion();
    protected abstract void onAnswering(int i, ActionEvent e);
}