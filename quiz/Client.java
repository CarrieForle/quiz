package quiz;

import gui.*;
import gui.Leaderboard.Player;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;

public class Client implements AutoCloseable {
    private static final String SERVER_ADDRESS = "26.198.51.130";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private Timer t = new Timer();

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

    public void writeTimeStamp(long a) throws IOException {
        DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
        out.writeLong(a);
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
    public void Leaderborad(){
        try{
            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
            @SuppressWarnings("unchecked")
            ArrayList<Leaderboard.Player> Leaderboard = (ArrayList<Player>) ois.readObject();
            Player firstPlayer = Leaderboard.get(0);
            System.out.println(firstPlayer.name);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public long getTimeStamp() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        long timestamp = in.readLong();
        return timestamp;
    }
    public Timer getTimer() {
        return t;
    }
    
    @Override
    public void close() throws IOException {
        socket.close();
    }
}