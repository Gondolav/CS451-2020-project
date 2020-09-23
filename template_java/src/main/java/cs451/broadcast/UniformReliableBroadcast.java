package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class UniformReliableBroadcast implements Observer {

    private final Observer observer;
    private final List<Host> hosts;
    private final BestEffortBroadcast beb;
    private final Set<Message> delivered;
    private final Map<Message, Set<Integer>> pending;
    private final Map<Message, Set<Integer>> ack;
    private final int senderNb;

    UniformReliableBroadcast(Observer observer, List<Host> hosts, int port, int senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.beb = new BestEffortBroadcast(this, hosts, port);
        this.delivered = ConcurrentHashMap.newKeySet();
        this.pending = new ConcurrentHashMap<>();
        this.ack = new ConcurrentHashMap<>();
        this.senderNb = senderNb;
    }

    private boolean canDeliver(Message message) {
        return 2 * ack.get(message).size() > (hosts.size() + 1);
    }

    void broadcast(Message message) {
        pending.computeIfAbsent(message, m -> ConcurrentHashMap.newKeySet());
        pending.get(message).add(message.getSenderNb()); // the senderNb will be our own process' id
        beb.broadcast(message);
    }

    void start() {
        beb.start();
    }

    void stop() {
        beb.stop();
    }

    @Override
    public void deliver(Message message) {
        ack.computeIfAbsent(message, m -> ConcurrentHashMap.newKeySet());
        ack.get(message).add(message.getSenderNb());

        if (!pending.containsKey(message) || !pending.get(message).contains(message.getOriginalSenderNb())) {
            pending.computeIfAbsent(message, m -> ConcurrentHashMap.newKeySet());
            pending.get(message).add(message.getOriginalSenderNb());
            beb.broadcast(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb()));
        }

        if (pending.containsKey(message) && pending.get(message).contains(message.getOriginalSenderNb()) && canDeliver(message) && !delivered.contains(message)) {
            delivered.add(message);
            observer.deliver(message);
        }
    }
}
