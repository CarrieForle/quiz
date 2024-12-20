package gui;

import javax.swing.*;

import utils.Question;

import java.awt.*;
import java.awt.event.*;

import java.util.TimerTask;
import java.util.Timer;

public abstract class AnswerFrame {
    private JFrame frame;
    private Timer timer = new Timer();
    private TimerTask countdownTask;
    private JLabel scoreLabel = new JLabel("Score: 0");
    private JLabel timeLabel = new JLabel("", JLabel.CENTER);
    private JLabel rankLabel = new JLabel("Rank: 1");
    private JTextArea questionArea = getJTextArea();
    private JScrollPane questionScrollArea = new JScrollPane(questionArea);
    private JProgressBar timebar = new JProgressBar(0, 100);
    private JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JTextArea[] optionAreas = new JTextArea[4];
    private JButton[] answerButtons = new JButton[4];
    private JScrollPane[] optionScrollAreas = new JScrollPane[4];
    protected int score = 0;
    protected int ranking = 1;

    public AnswerFrame() {
        frame = new JFrame("刷題趣！");
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
            panel.add(optionScrollAreas[i], BorderLayout.CENTER);
            panel.add(answerButtons[i], BorderLayout.SOUTH);
            buttonPanel.add(panel);

            final int id = i;
            answerButtons[i].addActionListener(e -> {
                for (JButton button : answerButtons) {
                    button.setEnabled(false);
                }

                countdownTask.cancel();

                Thread t = new Thread(() -> {
                    onAnswering(id, e);
                    Question question = getNextQuestion();

                    for (JButton button : answerButtons) {
                        button.setEnabled(true);
                    }

                    if (question == null) {
                        frame.dispose();
                        showLeaderboard();
                    } else {
                        updateFields(question);
                        startCountDown();
                    }
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
        startCountDown();

        for (JButton button : answerButtons) {
            button.setEnabled(true);
        }
    }

    public void setVisible(boolean b) {
        frame.setVisible(b);
    }

    public void setSize(int width, int height) {
        frame.setSize(width, height);
    }

    public JFrame getFrame() {
        return frame;
    }

    public JSplitPane getMainPane() {
        return mainPane;
    }

    private JTextArea getJTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setFocusable(false);
        area.setEditable(false);

        return area;
    }

    private void updateFields(Question question) {
        scoreLabel.setText("Score: " + getScore());
        rankLabel.setText("Rank: " + getRank());

        questionArea.setText(question.question);
        questionScrollArea.getVerticalScrollBar().setValue(0);

        for (int i = 0; i < 4; i++) {
            optionAreas[i].setText(question.getOption(i));
            optionScrollAreas[i].getVerticalScrollBar().setValue(0);
        }
    }
    
    private void startCountDown() {
        countdownTask = new TimerTask() {
            int timeLeft = 100;

            @Override
            public void run() {
                timeLabel.setText(String.format("%.1f", timeLeft * 0.1));
                timebar.setValue(timeLeft);

                if (timeLeft <= 0) {
                    cancel();
                    onTimeExceed();

                    Question question = getNextQuestion();

                    if (question == null) {
                        showLeaderboard();
                        frame.dispose();
                    } else {
                        updateFields(question);
                        startCountDown();
                    }
                }

                timeLeft--;
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 100);
    }

    protected void onWindowClosing(WindowEvent e) {

    }
    
    protected void onTimeExceed() {

    }

    protected abstract void showLeaderboard();
    protected abstract int getScore();
    protected abstract int getRank();
    protected abstract Question getNextQuestion();
    protected abstract void onAnswering(int i, ActionEvent e);
}