package gui;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import utils.HistoryGame;

public class HistoryInfoDialog extends JDialog {
    public HistoryInfoDialog(Window parent, HistoryGame game) {
        super(parent, "Info", Dialog.ModalityType.DOCUMENT_MODAL);
        setSize(300, 200);
        setIconImage(Resource.icon.getImage());

        HistoryGame.Metadata metadata = game.metadata;

        LocalDateTime when = LocalDateTime.ofInstant(metadata.when, ZoneId.systemDefault());
        String timeString = String.format(when.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        StringBuilder sb = new StringBuilder();

        sb.append("Complete datetime: ");
        sb.append(timeString);
        sb.append("\n");
        sb.append("Quiz: ");
        sb.append(game.quiz.name);
        sb.append("\n");
        
        if (metadata.isLocal()) {
            sb.append("Singleplayer\n");
        } else {
            sb.append("Multiplayer\n");
            sb.append("Name: ");
            sb.append(metadata.name);
            sb.append("\n");
            sb.append("Address: ");
            sb.append(metadata.address);
            sb.append("\n");
            sb.append("Rank: ");
            sb.append(metadata.rank);
            sb.append("\n");
        }

        sb.append("Score: ");
        sb.append(metadata.score);
        sb.append("\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        add(new JScrollPane(area));

        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
