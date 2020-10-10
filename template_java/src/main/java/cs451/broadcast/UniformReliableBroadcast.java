package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UniformReliableBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final List<Host> hosts;
    private final BestEffortBroadcast beb;
    private final Map<Message, AtomicInteger> delivered;
    private final Map<Message, AtomicInteger> pending;
    private final Map<Message, AtomicInteger> ack;
    private final int senderNb;

    public UniformReliableBroadcast(Observer observer, List<Host> hosts, int port, int senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.beb = new BestEffortBroadcast(this, hosts, port);
        this.delivered = new ConcurrentHashMap<>();
        this.pending = new ConcurrentHashMap<>();
        this.ack = new ConcurrentHashMap<>();
        this.senderNb = senderNb;
    }

    private boolean canDeliver(Message message) {
        return 2 * ack.getOrDefault(message, new AtomicInteger(0)).get() > hosts.size();
    }

    @Override
    public void broadcast(Message message) {
        pending.put(message, new AtomicInteger(1));
//        ack.computeIfAbsent(message, m -> ConcurrentHashMap.newKeySet());
//        ack.get(message).add(message.getSenderNb());
//        ack.get(message).add(senderNb);
        beb.broadcast(message);
    }

    @Override
    public void start() {
        beb.start();
    }

    @Override
    public void stop() {
        beb.stop();
    }

    @Override
    public void deliver(Message message) {
        ack.computeIfAbsent(message, m -> new AtomicInteger(0));
        ack.get(message).incrementAndGet();

        if (!pending.containsKey(message)) {
            pending.put(message, new AtomicInteger(1));
            beb.broadcast(message);
        }

        if (canDeliver(message) && !delivered.containsKey(message)) {
            delivered.put(message, new AtomicInteger(1));
            observer.deliver(message);
        }
    }
}
