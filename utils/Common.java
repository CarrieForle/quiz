package utils;

import java.awt.Component;

import javax.swing.JOptionPane;

public class Common {
    public static void main(String[] args) {
        String[] test_cases = {
            "123",
            "",
            "What do you mean",
            "      \n\t",
            null,
            "1234567890abcdefg"
        };

        for (String username : test_cases) {
            System.out.println(validate_username(username));
        }
    }

    /* 
     * Validate the username.
     * 
     * It returns null if username is valid; otherwise it returns the reason it's not valid.
     */
    public static String validate_username(String username) {
        if (username == null) {
            return "Username must not be null";
        }

        if (username.isBlank()) {
            return "Username must not be blank or contain only whitespace";
        }

        if (username.startsWith("$")) {
            return "Username must not start with '$'";
        }

        username = username.trim();

        if (username.length() > 16) {
            return "Username must not exceed 16 letters";
        }

        return null;
    }

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