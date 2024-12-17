package gui;

import javax.swing.*;
import java.awt.*;

public class AnswerFrame {
    private JFrame frame;

    public static void main(String[] args) {
        AnswerFrame f = new AnswerFrame();
    }

    public AnswerFrame() {
        frame = new JFrame("刷題趣！");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);

        JTextArea questionArea = new JTextArea("便利商店因週年慶而提供折扣優惠，只要消費滿99元就可從紙盒中隨機抽一球來決定該筆消費的折扣數（每顆球被抽到的機率相等）。店家已在盒中放了9顆球，其中寫著6折和7折的各有1顆、9折2顆、95折5顆。令隨機變數X代表消費100元的顧客在折扣後需要付的金額（元），哪個程式片段可以求得X的期望值是多少？\n" + //
        "已宣告二維陣列array2D儲存(折扣數,球數)。 \n" + //
        "double[,] array2D = new double[4,2]{{0.6,1},{0.7,1},{0.9,2},{0.95,5}};");
        
        questionArea.setLineWrap(true);
        questionArea.setEditable(false);
        questionArea.setOpaque(false);
        questionArea.setFocusable(false);
        questionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(questionArea);
        scrollPane.setOpaque(false);
        scrollPane.setBounds(50, 50, 500, 50);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame.add(scrollPane);

        JLabel timeLabel = new JLabel("Time: ");
        timeLabel.setBounds(0, 280, 100, 50);
        frame.add(timeLabel);

        JLabel scoreLabel = new JLabel("Score: ");
        scoreLabel.setBounds(0,300,100,50);
        frame.add(scoreLabel);

        JLabel rankLabel = new JLabel("Rank: ");
        rankLabel.setBounds(0,320,100,50);
        frame.add(rankLabel);

        JButton[] answerButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JButton();
            int x = (i % 2 == 0) ? 100 : 300;
            int y = 200 + (i / 2) * 60;
            answerButtons[i].setBounds(x, y, 150, 40);
            frame.add(answerButtons[i]);
        }

        answerButtons[0].setText("double totalBalls = 0;\n" + //
            "double x = 0;\n" + //
            "for (int i = 0; i < array2D.Length / array2D.Rank; i++)\n" + //
            "{\n" + //
            "totalBalls = totalBalls + array2D[i, 1];\n" + //
            "}\n" + //
            "for (int i = 0; i < array2D.Length / array2D.Rank; i++)\n" + //
            "{\n" + //
            "if(totalBalls != 0)\n" + //
            "x = x + array2D[i, 0] * array2D[i, 1] / totalBalls;\n" + //
            "}\n" + //
            "label1.Text =x.ToString(\"0.000\");");

        answerButtons[1].setText("double totalBalls = 0;\n" + //
            "double x = 0;\n" + //
            "for (int i = 0; i < array2D.Length; i++)\n" + //
            "{\n" + //
            "totalBalls = totalBalls + array2D[i, 1];\n" + //
            "}\n" + //
            "for (int i = 0; i < array2D.Length; i++)\n" + //
            "{\n" + //
            "x = x + array2D[i, 0] * array2D[i, 1] / totalBalls;\n" + //
            "}");
        
        answerButtons[2].setText("int i = 0;\n" + //
            "int k = 0;\n" + //
            "int totalBalls = 0;\n" + //
            "double x = 0;\n" + //
            "foreach (int j in array2D)\n" + //
            "{\n" + //
            "i++;\n" + //
            "if (i % 2 == 0)\n" + //
            "totalBalls = totalBalls + j;\n" + //
            "}\n" + //
            "i = 0;\n" + //
            "foreach (int j in array2D)\n" + //
            "{\n" + //
            "i++;\n" + //
            "if (i % 2 == 1)\n" + //
            "k = j;\n" + //
            "else\n" + //
            "{\n" + //
            "x = x + k * j / totalBalls;\n" + //
            "}\n" + //
            "}");

        answerButtons[3].setText("double totalBalls = 0;\n" + //
            "totalBalls = array2D[1, 2] + array2D[2, 2] + array2D[3, 2] + array2D[4, 2];\n" + //
            "double x = 0;\n" + //
            "x = x + array2D[1, 1] * array2D[1, 2] / totalBalls;\n" + //
            "x = x + array2D[2, 1] * array2D[2, 2] / totalBalls;\n" + //
            "x = x + array2D[3, 1] * array2D[3, 2] / totalBalls;\n" + //
            "x = x + array2D[4, 1] * array2D[4, 2] / totalBalls;");
    
        frame.setLocationRelativeTo(frame.getParent());
        frame.setVisible(true);
    }
}