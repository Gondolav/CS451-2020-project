package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

class FairLossLinks implements Observer, Links {

    private final Observer observer;
    private final UDPReceiver receiver;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
//        private DatagramSocket socket;
    private DatagramSocket[] senderSockets;

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
        try {
//            socket = new DatagramSocket();
            senderSockets = new DatagramSocket[]{new DatagramSocket(), new DatagramSocket(), new DatagramSocket(),
                    new DatagramSocket(), new DatagramSocket(), new DatagramSocket(), new DatagramSocket(),
                    new DatagramSocket(), new DatagramSocket(), new DatagramSocket()};
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Message message, Host host) {
        int socketSelector = ThreadLocalRandom.current().nextInt(0, senderSockets.length);
        UDPSender sender = new UDPSender(host.getIp(), host.getPort(), message, senderSockets[socketSelector]);
        threadPool.execute(sender);
//        try {
//            byte[] data = message.toByteArray();
//            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host.getIp()), host.getPort());
//            socket.send(packet);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
