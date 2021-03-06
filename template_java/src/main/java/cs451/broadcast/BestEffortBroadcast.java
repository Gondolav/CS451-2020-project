package cs451.broadcast;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;
import cs451.links.PerfectLinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class BestEffortBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final List<Host> hosts;
    private final PerfectLinks perfectLinks;

    BestEffortBroadcast(Observer observer, List<Host> hosts, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.perfectLinks = new PerfectLinks(this, port, senderNbToHosts, senderNb);
    }

    @Override
    public void broadcast(Message message) {
        hosts.parallelStream().forEach(host -> perfectLinks.send(message, host));
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
