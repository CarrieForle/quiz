package networking;

import java.util.List;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class ServerStorage {
    // 用於生成遞增的檔案名稱
    private AtomicLong fileCounter;
    private final Path directory;

    public ServerStorage(Path directory) throws IOException {
        this.directory = directory;

        List<Path> paths = Files.list(directory)
            .filter(x -> !x.toFile().isDirectory()).toList();

        for (long i = 1; i < Long.MAX_VALUE; i++) {
            final long finali = i;
            if (!paths.stream().anyMatch(x -> x.endsWith(String.format("%d.quiz", finali)))) {
                fileCounter = new AtomicLong(i);
                break;
            }
        }
    }

    public static void main(String[] args) {
        String host = "26.58.144.237";
        int port = 12345;

        System.out.println("Server is starting on host: " + host + ", port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ServerStorage ss = new ServerStorage(Path.of(""));

            System.out.println("Server is running...");

            while (true) {
                // 等待 Client 連線
                System.out.println("Enter event loop.");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client: " + clientSocket.getInetAddress());

                // 接收 Client 傳送的文字並存成檔案
                ss.saveClientDataToFile(clientSocket);

                // 關閉 Client 連線
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error in server: " + e.getMessage());
        }
    }

    public File saveClientDataToFile(Socket clientSocket) throws IOException {
        try (DataInputStream reader = new DataInputStream(clientSocket.getInputStream())) {
            // 檔案名稱，例如 1.quiz、2.quiz
            File filePath = directory.resolve(fileCounter.getAndIncrement() + ".quiz").toFile();
            String contents = reader.readUTF();

            // 寫入檔案
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(contents, 0, contents.length());
            }

            return filePath;
        }
    }

    public void sendClientQuiz(Socket clientSocket) throws IOException {
        List<Path> filePaths = Files.list(directory).toList();
        
        String contents = Files.readString(filePaths.get(0), StandardCharsets.UTF_8);

        try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            out.writeUTF(contents);
        }
    }
}
