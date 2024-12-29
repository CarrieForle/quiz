package gui;

import java.io.InputStream;

import javax.swing.ImageIcon;

public class Resource {
    public static final ImageIcon podium;
    public static final ImageIcon icon;
    public static final ImageIcon github;
    public static final ImageIcon leftArrow;
    public static final ImageIcon rightArrow;
    public static final ImageIcon questionMark;

    static {
        ImageIcon tmp = new ImageIcon("assets/podium.png");
        podium = new ImageIcon(tmp.getImage().getScaledInstance(250, 105,
java.awt.Image.SCALE_SMOOTH));
        icon = new ImageIcon("assets/icon-mid.png");
        github = new ImageIcon("assets/github.png");
        leftArrow = new ImageIcon("assets/left.png");
        rightArrow = new ImageIcon("assets/right.png");
        questionMark = new ImageIcon("assets/question-mark.png");
    }
}
