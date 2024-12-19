package quiz;

import gui.*;
import gui.Leaderboard.Player;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client implements AutoCloseable {
    private Socket socket;

    public Client(Socket socket){
        this.socket = socket;
    }

    public static void main(String[] args) {
        new MainMenu();
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
    
    public boolean checkEnd() throws IOException {
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
    
    @SuppressWarnings("unchecked")
    public ArrayList<Leaderboard.Player> leaderborad() throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
            
            return (ArrayList<Player>) ois.readObject();
        } catch (ClassNotFoundException e) {
            // Impossible
            return null;
        }
    }

    public long getTimeStamp() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        long timestamp = in.readLong();
        return timestamp;
    }

    public void leaveEarly() throws IOException {
        this.socket.getOutputStream().write(-1);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
    
    @Override
    public void close() throws IOException {
        socket.close();
    }

    public String getNameResponse() throws IOException {
        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        String response = in.readUTF();
        return response;
    }
}