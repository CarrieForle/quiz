package networking;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerStorage {
    // 用於生成遞增的檔案名稱
    private final AtomicInteger fileCounter = new AtomicInteger(1);
    private final Path directory;

    public ServerStorage(Path directory) {
        this.directory = directory;
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
}
