package utils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import gui.MainMenu;

public class OpenMenuOnClosing extends WindowAdapter {
    private JFrame frame;

    public OpenMenuOnClosing(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.frame.dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        new MainMenu();
    }
}