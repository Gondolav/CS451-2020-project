package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

public class UDPSender implements Runnable {
    private DatagramSocket socket;

    private InetAddress ip;
    private int port;
    private Message message;

    public UDPSender(String ip, int port, Message message) {
        try {
            this.ip = InetAddress.getByName(ip);
            this.port = port;
            this.message = message;
            socket = new DatagramSocket();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (var byteOutputStream = new ByteArrayOutputStream();
             var outputStream = new ObjectOutputStream(byteOutputStream)) {
            outputStream.writeObject(message);
            byte[] data = byteOutputStream.toByteArray();
            var packet = new DatagramPacket(data, data.length, ip, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
