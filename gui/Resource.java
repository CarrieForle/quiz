package gui;

import javax.swing.ImageIcon;

public class Resource {
    public static final ImageIcon podium;
    public static final ImageIcon icon;
    public static final ImageIcon github;

    static {
        ImageIcon tmp = new ImageIcon(Resource.class.getResource("/assets/podium.png"));
        podium = new ImageIcon(tmp.getImage().getScaledInstance(250, 105, java.awt.Image.SCALE_SMOOTH));
        icon = new ImageIcon(Resource.class.getResource("/assets/icon-mid.png"));
        github = new ImageIcon(Resource.class.getResource("/assets/github.png"));
    }
}
