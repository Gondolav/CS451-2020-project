package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.Links;
import cs451.links.PerfectLinks;

import java.util.ArrayList;
import java.util.List;

public class BestEffortBroadcast implements Observer, Broadcast {

    private final Observer observer;
    private final List<Host> hosts;
    private final PerfectLinks perfectLinks;

    public BestEffortBroadcast(Observer observer, List<Host> hosts, int port) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.perfectLinks = new PerfectLinks(this, port);
    }

    @Override
    public void broadcast(Message message) {
        for (var host : hosts) {
            perfectLinks.send(message, host);
        }
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
