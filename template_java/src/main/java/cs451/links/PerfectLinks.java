package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerfectLinks implements Observer, Links {

    private final Observer observer;
    private final StubbornLinks stubborn;
    private final Set<Message> delivered;

    public PerfectLinks(Observer observer, int port, Map<Integer, Host> senderNbToHosts, int senderNb) {
        this.observer = observer;
        this.stubborn = new StubbornLinks(this, port, senderNbToHosts, senderNb);
        this.delivered = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void send(Message message, Host host) {
        stubborn.send(message, host);
    }

    @Override
    public void start() {
        stubborn.start();
    }

    @Override
    public void stop() {
        stubborn.stop();
    }

    @Override
    public void deliver(Message message) {
        if (!delivered.contains(message)) {
            delivered.add(message);
            observer.deliver(message);
        }
    }
}
