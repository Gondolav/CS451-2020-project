package cs451.links;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public final class PerfectLinks implements Observer, Links {

    private final Observer observer;
    private final StubbornLinks stubborn;
    private final Set<Message> delivered;
    private final ReentrantLock lock = new ReentrantLock();

    public PerfectLinks(Observer observer, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.stubborn = new StubbornLinks(this, port, senderNbToHosts, senderNb);
        this.delivered = new HashSet<>();
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
        lock.lock();
        if (!delivered.contains(message)) {
            delivered.add(message);
            observer.deliver(message);
        }
        lock.unlock();
    }
}
