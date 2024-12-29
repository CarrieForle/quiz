package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.*;

import utils.Common;
import utils.HistoryGame;
import utils.HistoryStorage;
import utils.exceptions.CorruptedHistoryException;

public class HistoryDashboard extends JDialog {
    private static final JLabel NONE_LABEL1;
    private static final JLabel NONE_LABEL2;
    private static final JPanel NONE_PANEL;
    private JList<String> list;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JScrollPane listScrollPane;
    private Window parent;
    private List<Path> historyPaths;

    public static void main(String[] args) {
        try {
            new HistoryDashboard(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        NONE_LABEL1 = new JLabel("Wow. So empty.", JLabel.CENTER);
        NONE_LABEL1.setAlignmentX(0.5f);
        NONE_LABEL1.setAlignmentY(0.5f);
        NONE_LABEL2 = new JLabel("Start playing to see something here!", JLabel.CENTER);
        NONE_LABEL2.setAlignmentX(0.5f);
        NONE_LABEL2.setAlignmentY(0.5f);
        NONE_PANEL = new JPanel();
        NONE_PANEL.setLayout(new BoxLayout(NONE_PANEL, BoxLayout.Y_AXIS));
        NONE_PANEL.add(Box.createVerticalGlue());
        NONE_PANEL.add(NONE_LABEL1);
        NONE_PANEL.add(NONE_LABEL2);
        NONE_PANEL.add(Box.createVerticalGlue());
    }

    public HistoryDashboard(Window parent) throws IOException {
        super(parent, "複習趣！", Dialog.ModalityType.DOCUMENT_MODAL);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setIconImage(Resource.icon.getImage());
        
        this.parent = parent;
        this.list = new JList<>(this.listModel);
        this.listScrollPane = new JScrollPane(this.list);
        this.updateUI();

        JPanel buttonPanel = new JPanel();
        JButton reviewButton = new JButton("Review");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton openButton = new JButton("Open");

        final HistoryDashboard self = this;

        Action reviewAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (self.historyPaths.isEmpty()) {
                    return;
                }

                try {
                    HistoryGame game = HistoryStorage.load(self.historyPaths.get(list.getSelectedIndex()));

                    dispose();
                    new HistoryBoard((Window) getParent(), game);
                } catch (IOException ex) {
                    Common.errorMessage(self, "Failed to open game history file", ex);
                } catch (CorruptedHistoryException ex) {
                    Common.errorMessage(self, "Failed to parse game history file", ex);
                }
            }
        };

        Action deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (self.historyPaths.isEmpty()) {
                    return;
                }

                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(self, "Are you sure you want to delete it?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                    try {
                        Files.delete(self.historyPaths.get(self.list.getSelectedIndex()));
                    } catch (IOException ex) {
                        Common.errorMessage(self, "Failed to delete file", ex);
                    }

                    try {
                        self.updateUI();
                        repaint();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        };

        this.list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "review");
        this.list.getActionMap().put("review", reviewAction);

        this.list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        this.list.getActionMap().put("delete", deleteAction);

        reviewButton.addActionListener(reviewAction);
        deleteButton.addActionListener(deleteAction);

        clearButton.addActionListener(e -> {
            if (this.historyPaths.isEmpty()) {
                return;
            }

            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Do you want to clear your history? This cannot be undone", "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                while (true) {
                    try {
                        for (Path path : this.historyPaths) {
                            Files.delete(path);
                        }

                        JOptionPane.showMessageDialog(this, "History cleared successfully", "Success", JOptionPane.PLAIN_MESSAGE);
                        break;
                    } catch (IOException ex) {
                        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, "Failed to delete file. Do you wish to continue deletion?", "Deletion Failed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                            break;
                        }
                    }
                }

                try {
                    this.updateUI();
                    repaint();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        });

        openButton.addActionListener(e -> {
            JFileChooser fileChooser = HistoryStorage.getFileChooser();
            fileChooser.setCurrentDirectory(HistoryStorage.DIRECTORY.toFile());

            if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this)) {
                return;
            }

            try {
                HistoryGame game = HistoryStorage.load(fileChooser.getSelectedFile().toPath());

                dispose();
                new HistoryBoard(this.parent, game);
            } catch (IOException ex) {
                Common.errorMessage(this, "Failed to open game history file", ex);
            } catch (CorruptedHistoryException ex) {
                Common.errorMessage(this, "Failed to parse game history file", ex);
            }
        });

        buttonPanel.add(reviewButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(openButton);

        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }
    
    private void updateUI() throws IOException {
        this.historyPaths = HistoryStorage.list();

        if (this.historyPaths.isEmpty()) {
            this.getContentPane().remove(this.listScrollPane);
            add(NONE_PANEL, BorderLayout.CENTER);
        } else {
            this.listModel.clear();

            for (Path path : this.historyPaths) {
                listModel.addElement(path.getFileName().toString());
            }

            this.list.setSelectedIndex(0);

            this.getContentPane().remove(NONE_PANEL);
            add(this.listScrollPane, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }
}
