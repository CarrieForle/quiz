package quiz;

import gui.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "26.198.51.130";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    public Client(Socket socket){
        this.socket = socket;
    }

    public static void main(String[] args) {
        MainMenu menu = new MainMenu();
    }
    public void connectToServer(){
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            Client p = new Client(socket);
            // p.setName("Kuei");
            // p.getQuestion();
            // p.writeAns(0);
            // p.writeTimeStamp();

            if (p.CheckEnd()) {
                System.out.printf("sus");
            }

            int score = p.getScore();
            System.out.printf("分數為%d", score);
            int rank = p.getRank();
            System.out.printf("分數為%d", score);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getQuestion() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        String question = in.readUTF();

        return question;
    }
    public String[] getOptions() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        
        String[] options = new String[4];

        for (int i = 0; i < options.length; i++) {
            options[i] = in.readUTF();
            System.out.println(options[i]);
        }

        return options;
    }
    public void setName(String name) throws IOException {
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
        out.writeUTF(name);
        out.flush();
    }
    
    public void writeAns(int ans) throws IOException {
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
        out.writeInt(ans);
        out.flush();
    }

    public void writeTimeStamp() throws IOException {
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
        out.writeLong(1731822526000L);
        out.flush();
    }
    
    public boolean CheckEnd() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        boolean check = in.readBoolean();
        if(check){
            return true;
        }else{
            return false;
        }
    }

    public int getScore() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        int score = in.readInt();
        return score;
    }
    
    public int getRank() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        int rank = in.readInt();
        return rank;
    }
    
    private void uploadToServer(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());

            String contents = new String(fileInputStream.readAllBytes());
            dataOutputStream.writeUTF(contents);
        }
    }

    private void sendInvalidAnswerInSecond(long time) {
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            void run() {
                // code
            }
        }, time);
    }
}