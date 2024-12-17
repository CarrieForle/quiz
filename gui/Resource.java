package gui;

import javax.swing.ImageIcon;

public class Resource {
    public static final ImageIcon podium;
    public static final ImageIcon iconSmall;
    public static final ImageIcon iconMid;

    static {
        ImageIcon tmp = new ImageIcon(Resource.class.getResource("/assets/podium.png"));
        podium = new ImageIcon(tmp.getImage().getScaledInstance(250, 105, java.awt.Image.SCALE_SMOOTH));
        iconSmall = new ImageIcon(Resource.class.getResource("/assets/icon-small.png"));
        iconMid = new ImageIcon(Resource.class.getResource("/assets/icon-mid.png"));
    }
}
