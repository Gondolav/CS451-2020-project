package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class FairLossLinks implements Observer, Links {

    private final Observer observer;
    private final UDPReceiver receiver;
    private DatagramSocket socket;

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Message message, Host host) {
        try (var byteOutputStream = new ByteArrayOutputStream();
             var outputStream = new ObjectOutputStream(byteOutputStream)) {
            outputStream.writeObject(message);
            byte[] data = byteOutputStream.toByteArray();
            var packet = new DatagramPacket(data, data.length, InetAddress.getByName(host.getIp()), host.getPort());
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        receiver.start();
    }

    @Override
    public void stop() {
        receiver.stopReceiving();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
