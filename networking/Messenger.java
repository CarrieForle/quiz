package networking;

import java.io.*;
import java.net.*;
import java.util.*;

public class Messenger implements AutoCloseable {
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected Socket socket;
    private String buffer;

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(54432);
            Socket sa = new Socket("127.0.0.1", 54432) ;
            Socket sb = server.accept();

            Messenger a = new Messenger(sa);
            Messenger b = new Messenger(sb);
            
            a.writeUTF("LOL");
            b.readIncoming();
            System.out.println(b.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Messenger(Socket socket) throws IOException {
        this.socket = socket;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public void writeObject(Object o) throws IOException {
        this.dos.writeUTF("o");

        ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());

        oos.writeObject(o);
    }

    public void readIncoming() throws IOException {
        String n = this.dis.readUTF();

        if (n.equals("y")) {
            this.onCommand(this.dis.readUTF());
        } else {
            buffer = n;
        }
    }

    public void writeCommand(String s) throws IOException {
        this.dos.writeUTF("y");
        this.dos.writeUTF(s);
    }

    public void writeUTF(String s) throws IOException {
        this.dos.writeUTF("s");
        this.dos.writeUTF(s);
    }

    public void writeLong(long l) throws IOException {
        this.dos.writeUTF("l");
        this.dos.writeLong(l);
    }

    public void writeInt(int i) throws IOException {
        this.dos.writeUTF("i");
        this.dos.writeInt(i);
    }

    public void writeBoolean(boolean b) throws IOException {
        this.dos.writeUTF("b");
        this.dos.writeBoolean(b);
    }
    
    public Object readObject() throws IOException, ClassNotFoundException {
        if (this.buffer.equals("o")) {
            buffer = "";

            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());

            return ois.readObject();
        } else if (this.rn()) {
            return this.readObject();
        } else {
            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());

            return ois.readObject();
        }
    }

    public String readUTF() throws IOException {
        if (this.buffer.equals("s")) {
            buffer = "";
            return this.dis.readUTF();
        } else if (this.rn()) {
            return this.readUTF();
        } else {
            return this.dis.readUTF();
        }
    }

    public long readLong() throws IOException {
        if (this.buffer.equals("l")) {
            buffer = "";
            return this.dis.readLong();
        } else if (this.rn()) {
            return this.readLong();
        } else {
            return this.dis.readLong();
        }
    }
    
    public int readInt() throws IOException {
        if (this.rn()) {
            return this.readInt();
        } else {
            return this.dis.readInt();
        }
    }

    public boolean readBoolean() throws IOException {
        if (this.rn()) {
            return this.readBoolean();
        } else {
            return this.dis.readBoolean();
        }
    }

    protected void onCommand(String s) throws IOException {
        System.out.format("New Command %s\n", s);
    }

    private boolean rn() throws IOException {
        String n = this.dis.readUTF();

        if (n.equals("y")) {
            this.onCommand(this.dis.readUTF());
            return true;
        } else {
            return false;
        }
    }

    public void flush() throws IOException {
        this.dos.flush();
    }

    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    public boolean isClosed() {
        return this.socket.isClosed();
    }
}
