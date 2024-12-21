package gui;

import javax.swing.*;

import utils.Common;

import java.awt.*;
import java.util.Random;

public class LoginDialog extends JDialog {
    private static final int fontSize = 16;
    private LoginHandler handler;
    private JTextArea nameField = new JTextArea();
    private JTextArea serverField = new JTextArea();

    public static void main(String[] args) {
        LoginDialog l = new LoginDialog(null, new LoginHandler() {
            @Override
            public void login(LoginDialog l, String address, String name) {

            }
        });

        l.setVisible(true);
    }

    public LoginDialog(Window parent, LoginHandler handler) {
        super(parent, "Login", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(400, 220);
        setLayout(new BorderLayout());
        setIconImage(Resource.icon.getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.handler = handler;

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        
        // Name label and field
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(nameLabel.getFont().deriveFont(fontSize));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(nameLabel, c);

        this.nameField.setFont(nameField.getFont().deriveFont(fontSize));
        this.nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(this.nameField, c);
        
        // Server address input label and field
        JLabel serverLabel = new JLabel("Server Address");
        serverLabel.setFont(serverLabel.getFont().deriveFont(fontSize));

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(serverLabel, c);

        this.serverField.setFont(this.serverField.getFont().deriveFont(fontSize));
        this.serverField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(this.serverField, c);

        add(fieldPanel, BorderLayout.CENTER);
        
        // Submit Button
        JPanel submitButtonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        submitButtonPanel.add(submitButton);
        add(submitButtonPanel, BorderLayout.SOUTH);
        
        submitButton.addActionListener(e -> {
            String playerName = this.nameField.getText();
            String serverAddress = this.serverField.getText();

            // Validate inputs
            String reason = validate_username(playerName);
            if (reason != null) {
                Common.errorMessage(this, reason);
                return;
            }
            if (serverAddress == null || serverAddress.isEmpty()) {
                Common.errorMessage(this, "Server address is required.");
                return;
            }

            handler.login(this, serverAddress, playerName);
        });

        String[] names = {
            "Bob", "Jimmy", "Patrick", "Tom", "Leo", "Michael", "Rick Astley", "大谷翔平"
        };

        this.nameField.setText(names[new Random().nextInt(names.length)]);
        this.serverField.setText("127.0.0.1");

        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    public void setName(String name) {
        this.nameField.setText(name);
    }

    public void setAddress(String address) {
        this.serverField.setText(address);
    }

    private static String validate_username(String username) {
        username = username.trim();

        if (username == null) {
            return "Username must not be null";
        }

        if (username.isBlank()) {
            return "Username must not be blank";
        }

        if (username.contains("$#\0")) {
            return "User name must not contain illegal character";
        }

        if (username.length() > 16) {
            return "Username must not exceed 16 letters";
        }

        return null;
    }
}
