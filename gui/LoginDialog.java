package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class LoginDialog extends JDialog {
    private LoginHandler handler;

    public LoginDialog(JFrame parent, LoginHandler handler) {
        super(parent, "刷題趣！", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(400, 200);
        setLayout(null);
        setResizable(false);

        this.handler = handler;

        JLabel nameLabel = new JLabel("Name");
        nameLabel.setBounds(30, 30, 150, 30);
        this.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 30, 200, 30);
        this.add(nameField);

        //Server address input label and field
        JLabel serverLabel = new JLabel("Server Address");
        serverLabel.setBounds(30, 80, 150, 30);
        this.add(serverLabel);

        JTextField serverField = new JTextField();
        serverField.setBounds(150, 80, 200, 30);
        this.add(serverField);

        // Submit Button
        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 130, 100, 30);
        this.add(submitButton);

        submitButton.addActionListener(e -> {
            String playerName = nameField.getText();
            String serverAddress = serverField.getText();

            // Validate inputs
            if (playerName == null || playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            if (serverAddress == null || serverAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Server address is required.");
                return;
            }

            handler.login(serverAddress, playerName);
        });

        // TODO remove auto fields
        String[] names = {
            "Bob", "Jimmy", "Patrick", "Tom", "Leo", "Michael"
        };

        nameField.setText(names[new Random().nextInt(names.length)]);
        serverField.setText("127.0.0.1");

        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
