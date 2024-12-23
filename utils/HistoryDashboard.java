package utils;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.exceptions.CorruptedHistoryException;
import gui.HistoryBoard;

public class HistoryDashboard extends JDialog {
    private JList<String> list;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JScrollPane listScrollPane;
    private Window parent;
    private static final JLabel NONE_LABEL = new JLabel("Start playing to see something here!", JLabel.CENTER);
    private List<Path> historyPaths;
    private static final Path DIRECTORY = Path.of("quiz_history");

    public static void main(String[] args) {
        try {
            new HistoryDashboard(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                HistoryGame game = load(this.historyPaths.get(this.list.getSelectedIndex()));

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
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz History File (.quih)", "quih"));

            if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(this)) {
                return;
            }

            try {
                HistoryGame game = load(fileChooser.getSelectedFile().toPath());

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
        this.historyPaths = scanHistory();

        if (this.historyPaths.isEmpty()) {
            this.getContentPane().remove(this.listScrollPane);
            add(NONE_LABEL, BorderLayout.CENTER);
        } else {
            this.listModel.clear();

            for (Path path : this.historyPaths) {
                listModel.addElement(path.getFileName().toString());
            }

            this.list.setSelectedIndex(0);

            this.getContentPane().remove(NONE_LABEL);
            add(this.listScrollPane, BorderLayout.CENTER);
        }
    }

    private static List<Path> scanHistory() throws IOException {
        if (!DIRECTORY.toFile().exists()) {
            Files.createDirectory(DIRECTORY);
        }

        return Files.list(DIRECTORY).toList();
    }

    private static HistoryGame load(Path path) throws IOException, CorruptedHistoryException {
        return new HistoryGame(Files.readString(path, StandardCharsets.UTF_8));
    }
}
