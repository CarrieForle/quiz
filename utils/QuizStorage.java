package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class QuizStorage {
    public static JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Quiz File (.quiz)", "quiz"));

        return fileChooser;
    }

    public static void save(Path path, QuestionSet quiz) throws IOException {
        if (!path.endsWith(".quiz")) {
            path = Path.of(path.toString() + ".quiz");
        }

        Files.writeString(path, quiz.toString(), StandardCharsets.UTF_8);
    }

    public static Path save(Path path, QuizBuilder quiz) throws IOException {
        if (!path.endsWith(".quiz")) {
            path = Path.of(path.toString() + ".quiz");
        }
        
        Files.writeString(path, quiz.toString(), StandardCharsets.UTF_8);

        return path;
    }
}
