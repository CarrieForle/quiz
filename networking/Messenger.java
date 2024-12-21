package networking;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Messenger implements AutoCloseable {
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected Socket socket;
    private Queue<String> buffer = new ConcurrentLinkedQueue<>();

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

    // Only use it as last resort as it almost certainly will read longer than timeFrame

    public void readIncoming() throws IOException {
        String n = this.dis.readUTF();

        if (n.equals("y")) {
            Command c = extract(this.dis.readUTF());
            this.onCommand(c.command, c.args);
        } else {
            buffer.add(n);
        }
    }

    private static String produceCommand(String command, String[] args) {
        if (args.length == 0) {
            return command;
        }

        StringBuilder sb = new StringBuilder(command + " ");

        for (String arg : args) {
            sb.append(arg + "\0");
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public void writeCommand(String command, String[] args) throws IOException {
        this.dos.writeUTF("y");
        this.dos.writeUTF(produceCommand(command, args));
    }

    public void writeCommand(String command) throws IOException {
        this.writeCommand(command, new String[0]);
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

    private boolean buffer_is(String s) {
        String inc = this.buffer.peek();
        if (inc != null && inc.equals(s)) {
            this.buffer.poll();

            return true;
        }

        return false;
    }
    
    public Object readObject() throws IOException, ClassNotFoundException {
        if (this.buffer_is("o")) {
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
        if (this.buffer_is("s")) {
            return this.dis.readUTF();
        } else if (this.rn()) {
            return this.readUTF();
        } else {
            return this.dis.readUTF();
        }
    }

    public long readLong() throws IOException {
        if (this.buffer_is("l")) {
            return this.dis.readLong();
        } else if (this.rn()) {
            return this.readLong();
        } else {
            return this.dis.readLong();
        }
    }
    
    public int readInt() throws IOException {
        if (this.buffer_is("i")) {
            return this.dis.readInt();
        } else if (this.rn()) {
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

    public void onCommand(String command, String[] args) throws IOException {
        System.out.format("New Command %s\n", command);
    }

    public void onCommand(String command) throws IOException {
        this.onCommand(command, new String[0]);
    }

    private boolean rn() throws IOException {
        String n = this.dis.readUTF();

        if (n.equals("y")) {
            Command c = this.extract(this.dis.readUTF());
            
            this.onCommand(c.command, c.args);
            return true;
        } else {
            return false;
        }
    }

    private Command extract(String s) {
        Command c = new Command();

        String[] tokens = s.split(" ");
        c.command = tokens[0];

        if (tokens.length >= 2) {
            c.args = tokens[1].split("\0");
        } else {
            c.args = new String[0];
        }

        return c;
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

class Command {
    String command;
    String[] args;
}