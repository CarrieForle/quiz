package gui;

import javax.swing.*;
import javax.swing.border.Border;

import quiz.Server;
import utils.*;
import utils.HistoryGame.Metadata;
import utils.HistoryGame.Play;
import utils.exceptions.CorruptedHistoryException;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Path;

public class HistoryBoard extends JDialog {
    private HistoryGame game;
    private HistoryGame.Snapshot current;
    protected JLabel scoreLabel = new JLabel("Score: 0");
    protected JLabel timeLabel = new JLabel(" ", JLabel.CENTER);
    protected JLabel correctnessLabel = new JLabel("", JLabel.CENTER);
    protected JLabel rankLabel = new JLabel("Rank: 1");
    protected JTextArea questionArea = getJTextArea();
    protected JScrollPane questionScrollArea = new JScrollPane(questionArea);
    private JProgressBar timebar = new JProgressBar(0, 100);
    protected JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    protected JTextArea[] optionAreas = new JTextArea[4];
    protected JScrollPane[] optionScrollAreas = new JScrollPane[4];

    public static void main(String[] args) {
        try {
            HistoryGame game = HistoryStorage.load(Path.of("quiz_history/2024-12-24T00_17_03-程式設計與運算思維 Programming.quih"));
            new HistoryBoard(null, game);
        } catch (IOException e) {
            System.out.println(e);
        } catch (CorruptedHistoryException e) {
            System.out.println(e);
        }
    }

    public HistoryBoard(Window parent, HistoryGame game) {
        super(parent, "複習趣！", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(Resource.icon.getImage());
        setLayout(new BorderLayout());

        this.game = game;
        this.current = game.get(0);

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(game.quiz.name);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        this.questionArea.setText("Review Question");
        this.timebar.setValue(100);
        this.scoreLabel.setText("Score: " + game.metadata.score);
        this.rankLabel.setText("Rank: " + game.metadata.rank);

        JPanel questionPanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new BorderLayout(50, 0));
        questionPanel.add(this.questionScrollArea, BorderLayout.CENTER);
        JPanel playPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        playPanel.add(this.timeLabel);
        playPanel.add(this.correctnessLabel);

        infoPanel.add(this.timebar, BorderLayout.CENTER);
        infoPanel.add(this.scoreLabel, BorderLayout.WEST);
        infoPanel.add(playPanel, BorderLayout.SOUTH);
        infoPanel.add(this.rankLabel, BorderLayout.EAST);
        questionPanel.add(infoPanel, BorderLayout.SOUTH);

        JPanel answerButtonPanel = new JPanel();
        answerButtonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        for (int i = 0; i < 4; i++) {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            this.optionAreas[i] = getJTextArea();
            JButton answerButton = new JButton(String.valueOf((char) (65 + i)));
            answerButton.setEnabled(false);
            this.optionScrollAreas[i] = new JScrollPane(optionAreas[i]);

            panel.add(optionScrollAreas[i], BorderLayout.CENTER);
            panel.add(answerButton, BorderLayout.SOUTH);
            answerButtonPanel.add(panel);
        }

        this.mainPane.add(questionPanel);
        this.mainPane.add(answerButtonPanel);
        this.mainPane.setResizeWeight(0.5);
        this.mainPane.setDividerLocation(0.5);
        this.mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(mainPane);

        JPanel toolButtonPanel = new JPanel();
        JButton downloadQuizButton = new JButton("Download Quiz");
        toolButtonPanel.add(downloadQuizButton);
        JButton downloadHistoryButton = new JButton("Download History");
        toolButtonPanel.add(downloadHistoryButton);
        JButton dashboardButton = new JButton("Dashboard");
        toolButtonPanel.add(dashboardButton);
        JButton infoButton = new JButton("Info");
        toolButtonPanel.add(infoButton);

        JPanel navigatePanel = new JPanel();
        JScrollPane navigateScrollPane = new JScrollPane(navigatePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        navigateScrollPane.getHorizontalScrollBar().setUnitIncrement(15);

        for (int i = 0; i < game.quiz.size(); i++) {
            JButton button = new JButton(String.format("Q%d", i + 1));
            final int id = i;

            button.addActionListener(e -> {
                current = game.get(id);
                updateUI();
            });

            navigatePanel.add(button);
        }

        JSplitPane toolPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigateScrollPane, toolButtonPanel);
        toolPane.setDividerLocation(0.5f);
        toolPane.setResizeWeight(0.5f);

        add(toolPane, BorderLayout.SOUTH);
        this.updateUI();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JTextArea getJTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setFocusable(false);
        area.setEditable(false);

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                area.getCaret().setVisible(false);
            }
        });

        return area;
    }

    private Border getHighlightBorder(Color color) {
        Border inner = BorderFactory.createLineBorder(color, 5);
        Border outer = BorderFactory.createEmptyBorder(3, 3, 3, 3);

        return BorderFactory.createCompoundBorder(outer, inner);
    }

    private void updateUI() {
        setTitle(String.format("複習趣！ (%d/%d)", this.game.indexOf(this.current) + 1, this.game.quiz.size()));

        QuestionWithAnswer question = this.current.question;
        HistoryGame.Play play = this.current.play;

        this.questionArea.setText(question.question);

        for (int i = 0; i < 4; i++) {
            this.optionAreas[i].setText(this.current.question.getOption(i));

            Color color;

            if (i == question.answer) {
                color = Color.GREEN;
            } else {
                color = Color.RED;
            }

            this.optionScrollAreas[i].setBorder(this.getHighlightBorder(color));
        }

        if (play == null) {
            this.timeLabel.setText("You did not play this question");
            this.correctnessLabel.setText("");
            this.timebar.setValue(0);
            return;
        }

        if (play.isTimeExceed()) {
            this.timeLabel.setText("Time exceeded");
            this.timeLabel.setForeground(new Color(156, 34, 43));
            this.correctnessLabel.setText("");
            this.timebar.setValue(0);
            return;
        }

        this.timeLabel.setText(String.format("%.1fs", play.timeRemained * 0.001));

        if (play.choiceId == question.answer) {
            this.correctnessLabel.setText("Correct +" + this.current.play.scoreOffset);
            this.correctnessLabel.setForeground(new Color(68, 194, 61));
        } else {
            this.optionScrollAreas[play.choiceId].setBorder(this.getHighlightBorder(new Color(238, 208, 82)));
            this.correctnessLabel.setText("Wrong");
            this.correctnessLabel.setForeground(new Color(156, 34, 43));
        }

        this.timebar.setValue((int) (play.timeRemained * 0.01));
    }
}