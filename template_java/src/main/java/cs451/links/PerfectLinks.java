package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class PerfectLinks implements Observer, Links {

    private final Observer observer;
    private final StubbornLinks stubborn;
    private final Set<Message> delivered;
//    private final Map<Byte, Set<Integer>> delivered;
    private final ReentrantLock lock = new ReentrantLock();

    public PerfectLinks(Observer observer, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.stubborn = new StubbornLinks(this, port, senderNbToHosts, senderNb);
        this.delivered = new HashSet<>();
//        this.delivered = new HashMap<>();
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
//        delivered.computeIfAbsent(message.getOriginalSenderNb(), senderNb -> new HashSet<>());
//        if (!delivered.get(message.getOriginalSenderNb()).contains(message.getSeqNb())) {
//            System.out.println(message);
//            delivered.get(message.getOriginalSenderNb()).add(message.getSeqNb());
//            observer.deliver(message);
//        }
        if (!delivered.contains(message)) {
            delivered.add(message);
            observer.deliver(message);
        }
        lock.unlock();
    }
}
