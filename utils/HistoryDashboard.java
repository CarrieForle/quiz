package utils;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.*;

import utils.exceptions.CorruptedHistoryException;
import gui.HistoryBoard;

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
        this.parent = parent;
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 500);
        setLocationRelativeTo(parent);
        this.list = new JList<>(this.listModel);
        this.listScrollPane = new JScrollPane(this.list);
        this.updateUI();

        JPanel buttonPanel = new JPanel();
        JButton reviewButton = new JButton("Review");
        JButton deleteButton = new JButton("Delete");
        JButton openButton = new JButton("Open");

        reviewButton.addActionListener(e -> {
            if (this.historyPaths.isEmpty()) {
                return;
            }

            try {
                HistoryGame game = HistoryStorage.load(this.historyPaths.get(this.list.getSelectedIndex()));

                dispose();
                new HistoryBoard((Window) getParent(), game);
            } catch (IOException ex) {
                Common.errorMessage(this, "Failed to open game history file", ex);
            } catch (CorruptedHistoryException ex) {
                Common.errorMessage(this, "This game history is corrupted", ex);
            }
        });

        deleteButton.addActionListener(e -> {
            if (this.historyPaths.isEmpty()) {
                return;
            }

            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure you want to delete it?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
                try {
                    Files.delete(this.historyPaths.get(this.list.getSelectedIndex()));
                } catch (IOException ex) {
                    Common.errorMessage(this, "Failed to delete file", ex);
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
                Common.errorMessage(this, "This game history is corrupted", ex);
            }
        });

        buttonPanel.add(reviewButton);
        buttonPanel.add(deleteButton);
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
