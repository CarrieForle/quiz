package utils;

import java.awt.Component;

import javax.swing.JOptionPane;

public class Common {
    public static void errorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void errorMessage(Component parent, String prefix, Exception e) {
        errorMessage(parent, String.format("%s: %s", prefix, e.getMessage()));
    }

    public static void connectionFailedMessage(Component parent, Exception e) {
        errorMessage(parent, "Failed to connect to server", e);
    }
}