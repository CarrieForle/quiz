import gui.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {
    private static final String SERVER_ADDRESS = "192.168.0.141";
    private static final int SERVER_PORT = 12345;
    public static void main(String[] args) {
        
        ClientAnswerFrame gui = new ClientAnswerFrame();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
             
            setName("Kuei");
            String read = (String) in.readUTF();
            String[] temp = read.split("a");
            String[] Option = temp[1].split("A");
            System.out.println(temp[0]);
            for(String c : Option){
               System.out.println(c);
            }
            // writeAns(0);
            // writeTimeStamp();

            // if(CheckEnd()){
            //     System.out.printf("sus");
            // }else{
            //     getScore();
            //     getRank();
            // }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void setName(String name){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(name);
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeAns(int ans){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(ans);
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeTimeStamp(){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeLong(1731822526000L);
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean CheckEnd(){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataInputStream in = new DataInputStream(socket.getInputStream());
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
    public static void getScore(){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int score = in.readInt(); 
            System.out.printf("分數為%d",score);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void getRank(){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)){
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int rank = in.readInt(); 
            System.out.printf("排名為%d",rank);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}