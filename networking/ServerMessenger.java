package networking;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class ServerMessenger extends Messenger {
    List<? extends Messenger> messengers = Collections.emptyList();

    public ServerMessenger(Socket s) throws IOException {
        super(s);
    }

    public void updateMessenger(List<? extends Messenger> messengers) {
        synchronized (this.messengers) {
            this.messengers = messengers;
        }
    }

    private void executeBroadcast(String command, String[] args) throws IOException {
        synchronized (this.messengers) {
            for (Messenger m : this.messengers) {
                m.onCommand("transmit_message", args);
            }
        }
    }

    @Override
    public void onCommand(String command, String[] args) throws IOException {
        System.out.format("Server got command: %s\n", command);

        if (command.equals("message")) {
            this.executeBroadcast("transmit_message", args);
        } else if (command.equals("transmit_message")) {
            this.writeCommand("message", args);
        }
    }

    public void readIncomingFor(Duration timeFrame) throws IOException {
        Instant startTime = Instant.now();
        int oldTimeout = this.socket.getSoTimeout();
        
        try {
            this.socket.setSoTimeout(1050);
            Duration duration = Duration.ZERO;

            while (duration.compareTo(timeFrame) < 0) {
                try {
                    this.readIncoming();
                } catch (SocketTimeoutException e) {
                    System.out.println("Time out");
                }

                duration = Duration.between(startTime, Instant.now());
                System.out.println(duration.getSeconds());
            }
        } finally {
            this.socket.setSoTimeout(oldTimeout);
        }
    }
}
