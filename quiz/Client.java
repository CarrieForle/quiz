package quiz;

import gui.*;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Client {
    private static final String SERVER_ADDRESS = "26.198.51.130";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    public Client(Socket socket){
        this.socket = socket;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        ShootGUI gui = new ShootGUI();
        
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
                p.getScore();
                p.getRank();
            } else {
                p.getScore();
                p.getRank();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getQuestion(){
        try{
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            String question = (String) in.readUTF();
            return question;
        }catch (Exception e) {
            e.printStackTrace();
            return "Error";
            }
    }
    public String getOption1(){
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            
            String option1 = (String) in.readUTF();
            System.out.println(option1);
            return option1;

        }catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    public String getOption2(){
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            
            String option2 = (String) in.readUTF();
            System.out.println(option2);
            return option2;

        }catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    public String getOption3(){
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            
            String option3 = (String) in.readUTF();
            System.out.println(option3);
            return option3;

        }catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    public String getOption4(){
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            
            String option4 = (String) in.readUTF();
            System.out.println(option4);
            return option4;

        }catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    public void setName(String name) {
        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeUTF(name);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeAns(int ans){
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeInt(ans);
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeTimeStamp() {
        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeLong(1731822526000L);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean CheckEnd(){
        try{
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            boolean check = in.readBoolean();
            if(check){
                return true;
            }else{
                return false;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void getScore() {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            int score = in.readInt();
            System.out.printf("分數為%d", score);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getRank() {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            int rank = in.readInt();
            System.out.printf("排名為%d", rank);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void uploadToServer(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());

            String contents = new String(fileInputStream.readAllBytes());
            dataOutputStream.writeUTF(contents);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}