package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class FIFOBroadcast implements Observer {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private int lsn; // sequence number for broadcasting
    private final Set<Message> pending;
    private final AtomicIntegerArray next;
    private final int senderNb;

    public FIFOBroadcast(Observer observer, List<Host> hosts, int port, int senderNb) {
        this.observer = observer;
        this.urb = new UniformReliableBroadcast(this, hosts, port, senderNb);
        this.lsn = 1;
        this.pending = ConcurrentHashMap.newKeySet();

        int[] nextTmp = new int[hosts.size() + 1];
        Arrays.fill(nextTmp, 2);
        this.next = new AtomicIntegerArray(nextTmp);

        this.senderNb = senderNb;

    }

    public void broadcast(Message message) {
        ++lsn;
        urb.broadcast(new Message(lsn, senderNb, message.getOriginalSenderNb()));
    }

    public void start() {
        urb.start();
    }

    public void stop() {
        urb.stop();
    }

    @Override
    public void deliver(Message message) {
        pending.add(message);

        for (var msg : pending) {
            if (msg.getSeqNb() == next.get(msg.getSenderNb())) {
                next.incrementAndGet(msg.getSenderNb());
                pending.remove(msg);
                observer.deliver(message);
            }
        }
    }
}
