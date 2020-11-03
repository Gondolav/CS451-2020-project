package cs451.broadcast;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class FIFOBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private final Map<MessageID, Message> pending;
    private final AtomicIntegerArray next;
    private final byte senderNb;
    private final AtomicInteger lsn; // sequence number for broadcasting

    public FIFOBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.urb = new UniformReliableBroadcast(this, hosts, port, senderNbToHosts, senderNb);
        this.lsn = new AtomicInteger(1);
        this.pending = new ConcurrentHashMap<>();

        int[] nextTmp = new int[hosts.size() + 1];
        Arrays.fill(nextTmp, 1);
        this.next = new AtomicIntegerArray(nextTmp);

        this.senderNb = senderNb;
    }

    @Override
    public void broadcast(Message message) {
        urb.broadcast(new Message(lsn.getAndIncrement(), senderNb, message.getOriginalSenderNb(), message.isAck()));
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
        var messageID = new MessageID(message.getOriginalSenderNb(), message.getSeqNb());

        if (messageID.messageNb >= next.get(messageID.senderNb)) {
            pending.put(messageID, message);

            var iterator = pending.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var msg = entry.getValue();
                var originalSenderNb = msg.getOriginalSenderNb();
                if (msg.getSeqNb() == next.get(originalSenderNb)) {
                    observer.deliver(msg);
                    next.incrementAndGet(originalSenderNb);
                    iterator.remove();
                }
            }
        }
    }
}
