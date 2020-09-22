package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerfectLinks implements Observer {

    private final Observer observer;
    private final StubbornLinks stubborn;
    private final Set<Message> delivered;

    public PerfectLinks(Observer observer, int port) {
        this.observer = observer;
        this.stubborn = new StubbornLinks(observer, port);
        this.delivered = ConcurrentHashMap.newKeySet();
    }

    public void send(Message message, Host host) {
        stubborn.send(message, host);
    }

    public void stop() {
        stubborn.stop();
    }

    @Override
    public void notify(Message message) { // like deliver
        if (!delivered.contains(message)) {
            delivered.add(message);
            observer.notify(message);
        }
    }
}
