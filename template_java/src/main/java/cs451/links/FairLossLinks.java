package cs451.links;

import cs451.*;

class FairLossLinks implements Observer {

    private final Observer observer;
    private final UDPReceiver receiver;
    // TODO we could implement a thread pool made of a certain number of senders to avoid creating too many senders

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
    }

    void send(Message message, Host host) {
        UDPSender udpSender = new UDPSender(host.getIp(), host.getPort(), message);
        udpSender.start();
    }

    void start() {
        receiver.start();
    }

    void stop() {
        receiver.stopReceiving();
    }

    @Override
    public void deliver(Message message) { // like deliver
        observer.deliver(message);
    }
}
