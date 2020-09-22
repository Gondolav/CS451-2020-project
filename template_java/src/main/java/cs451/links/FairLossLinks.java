package cs451.links;

import cs451.*;

class FairLossLinks implements Observer {

    private final Observer observer;
    private final UDPReceiver receiver;

    FairLossLinks(Observer observer, int port) {
        this.observer = observer;
        this.receiver = new UDPReceiver(this, port);
        receiver.start();
    }

    void send(Message message, Host host) {
        UDPSender udpSender = new UDPSender(host.getIp(), host.getPort(), message);
        udpSender.start();
    }

    void stop() {
        receiver.stopReceiving();
    }

    @Override
    public void notify(Message message) { // like deliver
        observer.notify(message);
    }
}
