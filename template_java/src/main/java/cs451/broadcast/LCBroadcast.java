package cs451.broadcast;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class LCBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private final int[] vClockSend;
    private final int[] vClockReceive;
    private final Set<Byte> locality;
    private int sendSeqNb;
    private final Map<Byte, Set<Message>> pending;
    private final byte rank;

    private final ReentrantLock sendLock = new ReentrantLock();
    private final ReentrantLock receiveLock = new ReentrantLock();

    public LCBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb, Set<Byte> locality, byte rank) {
        this.observer = observer;
        this.urb = new UniformReliableBroadcast(this, hosts, port, senderNbToHosts, senderNb);
        this.vClockSend = new int[senderNbToHosts.size()];
        this.vClockReceive = new int[senderNbToHosts.size()];
        this.pending = new HashMap<>();
        for (int i = 0; i < senderNbToHosts.size(); i++) {
            vClockSend[i] = 0;
            vClockReceive[i] = 0;
            pending.put((byte) (i + 1), new HashSet<>());
        }
        this.locality = new HashSet<>(locality);
        this.sendSeqNb = 0;
        this.rank = rank;
    }

    @Override
    public void broadcast(Message message) {
        sendLock.lock();
        int[] w = vClockSend.clone();
        w[rank] = sendSeqNb;
        sendSeqNb++;
        sendLock.unlock();

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
        receiveLock.lock();
        pending.computeIfAbsent(message.getOriginalSenderNb(), s -> new HashSet<>()).add(message);

        // For every process
        for (byte p = 1; p < vClockSend.length + 1; p++) {
            for (var msg : pending.get(p)) {
                byte originalSenderNb = msg.getOriginalSenderNb();
                if (smallerOrEqual(msg.getVectorClock(), vClockReceive)) {
                    vClockReceive[originalSenderNb - 1]++;
                    if (locality.contains(originalSenderNb)) {
                        sendLock.lock();
                        vClockReceive[originalSenderNb - 1]++;
                        sendLock.unlock();
                    }

                    observer.deliver(msg);
                    pending.remove(p);
                }
            }
        }

        receiveLock.unlock();
    }

    private boolean smallerOrEqual(int[] vc1, int[] vc2) {
        for (int i = 0; i < vc1.length; i++) {
            if (vc1[i] > vc2[i]) return false;
        }

        return true;
    }
}
