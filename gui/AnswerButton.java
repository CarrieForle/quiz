package gui;

import javax.swing.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AnswerButton extends JButton implements ActionListener {
    private int identifier;

    AnswerButton(int id) {
        this.identifier = id;
        this.addActionListener(this);
        setText(String.format("Answer %d", this.identifier));
    }

    public int getId() {
        return this.identifier;
    }

    public void actionPerformed(ActionEvent e) {
        long timestamp = e.getWhen();

        try (FileWriter writer = new FileWriter(String.format("Answer_%d.txt", this.getId()))) {
            writer.write(String.format("Timestamp: %d\nLocal Datetime: %s", timestamp, LocalDateTime.now()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}