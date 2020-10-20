package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;

public class FIFOBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private final Map<MessageID, Message> pending;
    private final AtomicIntegerArray next;
    private final byte senderNb;
    private final ReentrantLock lock = new ReentrantLock();
    private int lsn; // sequence number for broadcasting

    public FIFOBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.urb = new UniformReliableBroadcast(this, hosts, port, senderNbToHosts, senderNb);
        this.lsn = 1;
        this.pending = new HashMap<>();

        int[] nextTmp = new int[hosts.size() + 1];
        Arrays.fill(nextTmp, 1);
        this.next = new AtomicIntegerArray(nextTmp);

        this.senderNb = senderNb;
    }

    @Override
    public void broadcast(Message message) {
        urb.broadcast(new Message(lsn++, senderNb, message.getOriginalSenderNb(), message.isAck()));
    }

    @Override
    public void start() {
        urb.start();
    }

    @Override
    public void stop() {
        urb.stop();
    }

    @Override
    public void deliver(Message message) {
        lock.lock();

        pending.put(new MessageID(message.getSenderNb(), message.getSeqNb()), message);

        var iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var senderNb = entry.getKey().senderNb;
            var msg = entry.getValue();
            if (msg.getSeqNb() == next.get(senderNb)) {
                next.incrementAndGet(senderNb);
                iterator.remove();
                observer.deliver(msg);
            }
        }

        lock.unlock();
    }
}
