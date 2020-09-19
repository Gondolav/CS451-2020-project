package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPReceiver extends Thread {
    private final Process process;
    private DatagramSocket socket;
    private final byte[] buf = new byte[256];

    private final AtomicBoolean running = new AtomicBoolean(false);

    public UDPReceiver(Process process, int port) {
        this.process = process;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stopReceiving() {
        running.set(false);
        socket.close();
    }

    public void notifyProcess(Message message) {
        process.logReceivedMessage(message);
    }

    @Override
    public void run() {
        running.set(true);

        while (running.get()) {
            var packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (var inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()))) {
                Message message = (Message) inputStream.readObject();
                notifyProcess(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
