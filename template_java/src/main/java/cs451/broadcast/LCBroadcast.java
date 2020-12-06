package cs451.broadcast;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class LCBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private final int[] vClock;
    private final Map<Byte, Set<Byte>> locality;
    private int sendSeqNb;
    private final Set<Message> pending;
    private final byte rank;

    private final ReentrantLock lock = new ReentrantLock();

    public LCBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb, Map<Byte, Set<Byte>> locality, byte rank) {
        this.observer = observer;
        this.urb = new UniformReliableBroadcast(this, hosts, port, senderNbToHosts, senderNb);
        this.vClock = new int[senderNbToHosts.size()];
        Arrays.fill(vClock, 0);
        this.pending = new HashSet<>();
        this.locality = new HashMap<>(locality);
        this.sendSeqNb = 0;
        this.rank = rank;
    }

    @Override
    public void broadcast(Message message) {
        lock.lock();
        int[] w = vClock.clone();
        w[rank] = sendSeqNb;
        sendSeqNb++;
        lock.unlock();

        urb.broadcast(new Message(message.getSeqNb(), message.getSenderNb(), message.getOriginalSenderNb(), message.isAck(), w));
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

        pending.add(message);

        boolean loopAgain = true;
        while (loopAgain) {
            loopAgain = false;

            var iterator = pending.iterator();
            while (iterator.hasNext()) {
                var msg = iterator.next();
                var originalSenderNb = msg.getOriginalSenderNb();
                if (smallerOrEqual(msg.getVectorClock(), vClock, locality.get(originalSenderNb))) {
                    vClock[originalSenderNb - 1]++;
                    // If we delivered some message, we have to loop again to deliver potentially more
                    loopAgain = true;
                    observer.deliver(msg);
                    iterator.remove();
                }
            }
        }

        lock.unlock();
    }

    private boolean smallerOrEqual(int[] vc1, int[] vc2, Set<Byte> dependencies) {
        int[] deps = dependencies.stream().mapToInt(b -> b - 1).toArray();
        for (int i : deps) {
            if (vc1[i] > vc2[i]) return false;
        }

        return true;
    }
}
