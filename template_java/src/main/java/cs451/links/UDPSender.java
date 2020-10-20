package cs451.links;

import cs451.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

class UDPSender implements Runnable {
    private final DatagramSocket socket;

    private InetAddress ip;
    private final int port;
    private final Message message;

    UDPSender(String ip, int port, Message message, DatagramSocket socket) {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
        this.message = message;
        this.socket = socket;
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
