package cs451.links;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

final class FairLossLinks implements Observer, Links {

    private static final int NB_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    private final Observer observer;
    private final UDPReceiver receiver;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(NB_THREADS);
    private DatagramSocket[] senderSockets;

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
        try {
            senderSockets = new DatagramSocket[NB_THREADS];
            for (int i = 0; i < senderSockets.length; i++) {
                senderSockets[i] = new DatagramSocket();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Message message, Host host) {
        int socketSelector = ThreadLocalRandom.current().nextInt(senderSockets.length);
        UDPSender sender = new UDPSender(host.getIp(), host.getPort(), message, senderSockets[socketSelector]);
        threadPool.execute(sender);
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
