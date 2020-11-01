package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class UniformReliableBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final List<Host> hosts;
    private final BestEffortBroadcast beb;
    private final Set<MessageID> delivered;
    private final Map<MessageID, Message> pending;
    private final Map<MessageID, Set<Byte>> ack;
    private final byte senderNb;
    private final ReentrantLock lock = new ReentrantLock();

    UniformReliableBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.beb = new BestEffortBroadcast(this, hosts, port, senderNbToHosts, senderNb);
        this.delivered = new HashSet<>();
        this.pending = new HashMap<>();
        this.ack = new HashMap<>();
        this.senderNb = senderNb;
    }

    private boolean canDeliver(MessageID messageID) {
        return 2 * ack.getOrDefault(messageID, new HashSet<>()).size() > hosts.size();
    }

    @Override
    public void broadcast(Message message) {
        lock.lock();
        var toSend = new Message(message.getSeqNb(), senderNb, senderNb, message.isAck(), message.isUpperLimit());
        pending.put(new MessageID(senderNb, message.getSeqNb()), toSend);
        lock.unlock();

        beb.broadcast(toSend);
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
        var receivedMessageID = new MessageID(message.getOriginalSenderNb(), message.getSeqNb());

        lock.lock();

        ack.computeIfAbsent(receivedMessageID, m -> new HashSet<>());
        ack.get(receivedMessageID).add(message.getSenderNb());

        if (!pending.containsKey(receivedMessageID)) {
            pending.put(receivedMessageID, message);
            beb.broadcast(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb(), message.isAck(), message.isUpperLimit()));
        }

        for (var entry : pending.entrySet()) {
            var messageID = entry.getKey();
            if (canDeliver(messageID) && !delivered.contains(messageID)) {
                delivered.add(messageID);
                observer.deliver(entry.getValue());
            }
        }

        lock.unlock();
    }
}
