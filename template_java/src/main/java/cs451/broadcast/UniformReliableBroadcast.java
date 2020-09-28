package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class UniformReliableBroadcast implements Observer {

    private final Observer observer;
    private final List<Host> hosts;
    private final BestEffortBroadcast beb;
    private final Set<Message> delivered;
    private final Set<Pair<Integer, Message>> pending;
    private final Map<Message, Set<Integer>> ack;
    private final int senderNb;

    UniformReliableBroadcast(Observer observer, List<Host> hosts, int port, int senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.beb = new BestEffortBroadcast(this, hosts, port);
        this.delivered = ConcurrentHashMap.newKeySet();
        this.pending = ConcurrentHashMap.newKeySet();
        this.ack = new ConcurrentHashMap<>();
        this.senderNb = senderNb;
    }

    private boolean canDeliver(Message message) {
        return 2 * ack.getOrDefault(message, ConcurrentHashMap.newKeySet()).size() > hosts.size();
    }

    void broadcast(Message message) {
        pending.add(new Pair<>(senderNb, message));
        ack.computeIfAbsent(message, m -> ConcurrentHashMap.newKeySet());
        ack.get(message).add(message.getSenderNb());
        ack.get(message).add(senderNb);
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
        ack.get(message).add(senderNb);

        var pair = new Pair<>(message.getOriginalSenderNb(), message);
        if (!pending.contains(pair)) {
            pending.add(pair);
            beb.broadcast(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb()));
        }

        for (var entry : pending) {
            var msg = entry.second;
            if (canDeliver(msg) && !delivered.contains(msg)) {
                delivered.add(msg);
                observer.deliver(msg);
            }
        }
    }

    private static class Pair<X, Y> {
        private final X first;
        private final Y second;

        private Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
