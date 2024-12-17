package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class LoginDialog extends JDialog {
    private static final Font FONT = new Font("Arial", Font.PLAIN, 16);
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

    public LoginDialog(JFrame parent, LoginHandler handler) {
        super(parent, "刷題趣！", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(400, 220);
        setLayout(new BorderLayout());
        setIconImage(Resource.iconSmall.getImage());

        this.handler = handler;

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        
        // Name label and field
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(FONT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(nameLabel, c);

        this.nameField.setFont(FONT);
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
        serverLabel.setFont(FONT);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 10, 10, 10);

        fieldPanel.add(serverLabel, c);

        this.serverField.setFont(FONT);
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
            if (playerName == null || playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (serverAddress == null || serverAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Server address is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            handler.login(this, serverAddress, playerName);
        });

        // TODO remove auto fields
        String[] names = {
            "Bob", "Jimmy", "Patrick", "Tom", "Leo", "Michael"
        };

        this.nameField.setText(names[new Random().nextInt(names.length)]);
        this.serverField.setText("127.0.0.1");

        setLocationRelativeTo(getParent());
    }

    public void setName(String name) {
        this.nameField.setText(name);
    }

    public void setAddress(String address) {
        this.serverField.setText(address);
    }
}