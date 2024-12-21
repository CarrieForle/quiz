package quiz;

import gui.*;
import gui.Leaderboard.Player;
import networking.Messenger;

import java.io.*;
import java.util.ArrayList;

public class Client implements AutoCloseable {
    private Messenger m;

    public Client(Messenger m){
        this.m = m;
    }

    public static void main(String[] args) {
        new MainMenu();
    }
    
    public String getQuestion() throws IOException {
        String question = m.readUTF();

        return question;
    }
    public String[] getOptions() throws IOException {  
        String[] options = new String[4];

        for (int i = 0; i < options.length; i++) {
            options[i] = m.readUTF();
            System.out.println(options[i]);
        }

        return options;
    }

    public void setName(String name) throws IOException {
        DataOutputStream dos = new DataOutputStream(m.getSocket().getOutputStream());
        dos.writeUTF(name);
        dos.flush();
    }
    
    public void writeAns(int ans) throws IOException {
        m.writeInt(ans);
        m.flush();
    }

    public void writeTimeStamp(long a) throws IOException {
        m.writeLong(a);
        m.flush();
    }
    
    public boolean checkEnd() throws IOException {
        boolean check = m.readBoolean();
        return check;
    }

    public int getAnswer() throws IOException {
        int answer = m.readInt();
        return answer;
    }

    public int getScore() throws IOException {
        int score = m.readInt();
        return score;
    }
    
    public int getRank() throws IOException {
        int rank = m.readInt();
        return rank;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<Leaderboard.Player> leaderborad() throws IOException {
        try {
            return (ArrayList<Player>) m.readObject();
        } catch (ClassNotFoundException e) {
            // Impossible
            return null;
        }
    }

    public int getTimeStamp() throws IOException {
        int timestamp = m.readInt();
        return timestamp;
    }

    public void leaveEarly() throws IOException {
        this.m.getSocket().getOutputStream().write(-1);
    }

    public void message(String name, String contents) throws IOException {
        this.m.writeCommand("message", new String[] { name, contents });
    }

    public void readIncoming() throws IOException {
        this.m.readIncoming();
    }

    public boolean isClosed() {
        return this.m.isClosed();
    }
    
    @Override
    public void close() throws IOException {
        this.m.close();
    }

    public String getNameResponse() throws IOException {
        String response = this.m.readUTF();
        return response;
    }
}