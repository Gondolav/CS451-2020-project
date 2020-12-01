package cs451.links;

import cs451.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

final class UDPSender implements Runnable {
    private final DatagramSocket socket;
    private final int port;
    private final Message message;
    private InetAddress ip;

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
        byte[] data = message.toByteArray();
        var packet = new DatagramPacket(data, data.length, ip, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
