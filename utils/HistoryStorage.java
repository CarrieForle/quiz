package utils;

import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.exceptions.CorruptedHistoryException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HistoryStorage {
    private static final Path DIRECTORY = Path.of("quiz_history");

    public static void save(Path path, HistoryGame game) throws IOException {
        checkAndCreateDirectory();

        if (!path.toString().endsWith(".quih")) {
            path = Path.of(path.toString() + ".quih");
        }

        game.export(path);
    }

    public static void save(HistoryGame game) throws IOException {
        checkAndCreateDirectory();
        game.export(getSavePath(game));
    }
    
    public static Path getSavePath(HistoryGame game) {
        LocalDateTime when = LocalDateTime.ofInstant(game.metadata.when, ZoneId.systemDefault());
        String date = when.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String time = when.format(DateTimeFormatter.ofPattern("HH_mm_ss"));
        String datetime = date + "T" + time;
        String fileName = String.format("%s-%s.quih", datetime, game.quiz.name);

        Path savePath;

        try {
            savePath = DIRECTORY.resolve(fileName);
        } catch (InvalidPathException e) {
            savePath = DIRECTORY.resolve(datetime + ".quih");
        }

        return savePath;
    }

    public static List<Path> list() throws IOException {
        checkAndCreateDirectory();
        return Files.list(DIRECTORY).toList();
    }

    public static HistoryGame load(Path path) throws IOException, CorruptedHistoryException {
        return new HistoryGame(Files.readString(path, StandardCharsets.UTF_8));
    }

    public static JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz History File (.quih)", "quih"));

        return fileChooser;
    }

    private static void checkAndCreateDirectory() throws IOException {
        if (!Files.exists(DIRECTORY)) {
            Files.createDirectory(DIRECTORY);
        }
    }
}
