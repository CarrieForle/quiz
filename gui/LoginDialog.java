package gui;

import javax.swing.*;

import utils.Common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.util.Random;

// Does not dispose after login.
// It is up to the client to decide when to dispose it.
public class LoginDialog extends JDialog {
    private static final float fontSize = 16f;
    private LoginHandler handler;
    private JTextField nameField = new JTextField();
    private JTextField serverField = new JTextField();

    public static void main(String[] args) {
        LoginDialog l = new LoginDialog(null, new LoginHandler() {
            @Override
            public void login(LoginDialog l, InetSocketAddress address, String name) {

            }
        });

        l.setVisible(true);
    }

    public static class Info {
        public String name;
        public String address;

        public Info(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public Info() {

        }
    }

    public static Info get(Window parent, LoginHandler handler, Info info) {
        LoginDialog dialog = new LoginDialog(parent, handler, info);
        info = dialog.getInfo();
        dialog.dispose();

        return info;
    }

    public static Info get(Window parent, LoginHandler handler) {
        LoginDialog dialog = new LoginDialog(parent, handler);
        Info info = dialog.getInfo();
        dialog.dispose();

        return info;
    }

    public LoginDialog(Window parent, LoginHandler handler) {
        this(parent, handler, null);
    }

    public LoginDialog(Window parent, LoginHandler handler, Info info) {
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
        JButton submitButton = new JButton("Login");
        submitButtonPanel.add(submitButton);
        add(submitButtonPanel, BorderLayout.SOUTH);

        final LoginDialog self = this;

        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText();
                String serverAddress = serverField.getText();

                // Validate inputs
                String reason = validateUsername(playerName);

                if (reason != null) {
                    Common.errorMessage(self, reason);
                    return;
                }

                try {
                    InetSocketAddress address = validateAddress(serverAddress);
                    handler.login(self, address, playerName);
                } catch (IllegalArgumentException ex) {
                    Common.errorMessage(self, ex.getMessage());
                }
            }
        };

        nameField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
        nameField.getActionMap().put("login", loginAction);
        serverField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
        serverField.getActionMap().put("login", loginAction);
        
        submitButton.addActionListener(loginAction);

        String[] names = {
            "Patrick", "Michael", "Rick Astley", "大谷翔平", "李白", "Lebron James", "劉在石", "春日影", "YOUR MOM"
        };

        this.nameField.setText(names[new Random().nextInt(names.length)]);
        this.serverField.setText("127.0.0.1");

        if (info != null) {
            this.nameField.setText(info.name);
            this.serverField.setText(info.address);
        }

        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    public void setAddress(String socketAddress) {
        this.serverField.setText(socketAddress);
    }

    public void setName(String name) {
        this.nameField.setText(name);
    }

    private Info getInfo() {
        String name = this.nameField.getText();
        String address = this.serverField.getText();
        
        return new Info(name, address);
    }

    private static String validateUsername(String username) {
        final String name = username.trim();

        if (name == null) {
            return "Username must not be null";
        }

        if (name.isBlank()) {
            return "Username must not be blank";
        }

        if ("$#\0".chars().anyMatch(x -> name.indexOf(x) != -1)) {
            return "User name must not contain illegal character";
        }

        if (username.length() > 16) {
            return "Username must not exceed 16 letters";
        }

        return null;
    }

    private static InetSocketAddress validateAddress(String socketAddress) {
        String address = socketAddress;
        int portDelimiter = socketAddress.lastIndexOf(":");
        int port = 0;

        if (portDelimiter == -1) {
            port = 12345;
        } else {
            address = socketAddress.substring(0, portDelimiter);

            try {
                port = Integer.parseInt(socketAddress.substring(portDelimiter + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number");
            }
        }
        
        return new InetSocketAddress(address, port);
    }
}
