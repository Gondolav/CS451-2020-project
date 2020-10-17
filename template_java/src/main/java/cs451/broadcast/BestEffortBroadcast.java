package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class BestEffortBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final List<Host> hosts;
    private final PerfectLinks perfectLinks;
    private final int senderNb;

    BestEffortBroadcast(Observer observer, List<Host> hosts, int port, Map<Integer, Host> senderNbToHosts, int senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.perfectLinks = new PerfectLinks(this, port, senderNbToHosts, senderNb);
        this.senderNb = senderNb;
    }

    @Override
    public void broadcast(Message message) {
        for (var host : hosts) {
            if (host.getId() != senderNb) {
                perfectLinks.send(message, host);
            }
        }
        deliver(message);
    }

    @Override
    public void start() {
        perfectLinks.start();
    }

    @Override
    public void stop() {
        perfectLinks.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
