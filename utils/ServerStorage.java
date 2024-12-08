package utils;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerStorage {
    // 用於生成遞增的檔案名稱
    private final AtomicInteger fileCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        String host = "26.58.144.237";
        int port = 12345;

        System.out.println("Server is starting on host: " + host + ", port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ServerStorage ss = new ServerStorage();

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

    public void saveClientDataToFile(Socket clientSocket) {
        try (DataInputStream reader = new DataInputStream(clientSocket.getInputStream())) {
            // 檔案名稱，例如 1.txt、2.txt
            String fileName = fileCounter.getAndIncrement() + ".txt";
            String contents = reader.readUTF();

            // 寫入檔案
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(contents, 0, contents.length());
            }

            System.out.println("Data saved to file: " + fileName);

        } catch (IOException e) {
            System.out.println("Error saving data to file: " + e.getMessage());
        }
    }
}
