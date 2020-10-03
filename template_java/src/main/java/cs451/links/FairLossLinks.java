package cs451.links;

import cs451.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class FairLossLinks implements Observer {

    private final Observer observer;
    private final UDPReceiver receiver;
    private final ExecutorService threadPool;

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    void send(Message message, Host host) {
        UDPSender udpSender = new UDPSender(host.getIp(), host.getPort(), message);
        threadPool.execute(udpSender);
    }

    void start() {
        receiver.start();
    }

    void stop() {
        threadPool.shutdownNow();
        receiver.stopReceiving();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
