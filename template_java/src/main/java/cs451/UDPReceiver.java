package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPReceiver extends Thread {
    private final Observer observer;
    private DatagramSocket socket;
    private final byte[] buf = new byte[256];

    private final AtomicBoolean running = new AtomicBoolean(false);

    public UDPReceiver(Observer observer, int port) {
        this.observer = observer;
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
                observer.deliver(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
