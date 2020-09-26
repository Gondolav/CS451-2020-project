package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLinks;

import java.util.ArrayList;
import java.util.List;

class BestEffortBroadcast implements Observer {

    private final Observer observer;
    private final List<Host> hosts;
    private final PerfectLinks perfectLinks;

    BestEffortBroadcast(Observer observer, List<Host> hosts, int port) {
        this.observer = observer;
        this.hosts = new ArrayList<>(hosts);
        this.perfectLinks = new PerfectLinks(this, port);
    }

    void broadcast(Message message) {
        for (var host : hosts) {
            perfectLinks.send(message, host);
        }
    }

    void start() {
        perfectLinks.start();
    }

    void stop() {
        perfectLinks.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
