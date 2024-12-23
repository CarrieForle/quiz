package utils;

import java.util.List;

import utils.exceptions.CorruptedHistoryException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryStorage {
    private static final Path DIRECTORY = Path.of("quiz_history");

    public static void save(HistoryGame game) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String time = now.format(DateTimeFormatter.ofPattern("HH_mm_ss"));
        String datetime = date + "T" + time;
        String fileName = String.format("%s-%s.quih", datetime, game.quiz.name);

        Path savePath;
        
        try {
            savePath = DIRECTORY.resolve(fileName);
        } catch (InvalidPathException e) {
            savePath = DIRECTORY.resolve(datetime + ".quih");
        }

        game.export(savePath);
    }

    public static List<Path> list() throws IOException {
        if (!DIRECTORY.toFile().exists()) {
            Files.createDirectory(DIRECTORY);
        }

        return Files.list(DIRECTORY).toList();
    }

    public static HistoryGame load(Path path) throws IOException, CorruptedHistoryException {
        return new HistoryGame(Files.readString(path, StandardCharsets.UTF_8));
    }
}
