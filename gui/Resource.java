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
        ImageIcon tmp = readImage("/assets/podium.png");
        podium = new ImageIcon(tmp.getImage().getScaledInstance(250, 105,
java.awt.Image.SCALE_SMOOTH));
        icon = readImage("/assets/icon-mid.png");
        github = readImage("/assets/github.png");
        leftArrow = readImage("/assets/left.png");
        rightArrow = readImage("/assets/right.png");
        questionMark = readImage("/assets/question-mark.png");
    }
    
    private static ImageIcon readImage(String path) {
        try {
            InputStream stream = Resource.class.getResourceAsStream(path);

            return new ImageIcon(stream.readAllBytes());
        } catch (Exception ex) {
            return new ImageIcon(path);
        }
    }
}
