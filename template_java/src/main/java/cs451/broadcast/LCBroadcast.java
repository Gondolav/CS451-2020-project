package cs451.broadcast;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.*;

public class LCBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final UniformReliableBroadcast urb;
    private final int[] vClockSend;
    private final int[] vClockReceive;
    private final Set<Byte> locality;
    private final int sendSeqNb;
    private final Map<Byte, Set<Message>> pending;

    public LCBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb, Set<Byte> locality) {
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
    }

    @Override
    public void broadcast(Message message) {

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

    }
}
