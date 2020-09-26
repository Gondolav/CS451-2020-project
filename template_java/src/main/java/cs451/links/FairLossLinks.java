package cs451.links;

import cs451.*;

public class FairLossLinks implements Observer {

    private final Observer observer;
    private final UDPReceiver receiver;
    // TODO we could implement a thread pool made of a certain number of senders to avoid creating too many senders

    public FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
    }

    public void send(Message message, Host host) {
        UDPSender udpSender = new UDPSender(host.getIp(), host.getPort(), message);
        udpSender.start();
        System.out.println("FairLoss send: " + message);
    }

    public void start() {
        receiver.start();
    }

    public void stop() {
        receiver.stopReceiving();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
        System.out.println("FairLoss deliver: " + message);
    }
}
