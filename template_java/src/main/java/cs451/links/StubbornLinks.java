package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

class StubbornLinks implements Observer, Links {

    private static final int HALF_SECOND = 500;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Host, Set<Message>> sent;
    private final Timer timer;

    StubbornLinks(Observer observer, int port) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>();

        // Implements the timer feature in StubbornLinks
        this.timer = new Timer();
    }

    @Override
    public void send(Message message, Host host) {
        fairLoss.send(message, host);
        sent.computeIfAbsent(host, h -> ConcurrentHashMap.newKeySet());
        sent.get(host).add(message);
    }

    @Override
    public void start() {
        fairLoss.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (var entry : sent.entrySet()) {
                    for (var msg : entry.getValue()) {
                        fairLoss.send(msg, entry.getKey());
                    }
                }
            }
        }, HALF_SECOND, HALF_SECOND);
    }

    @Override
    public void stop() {
        timer.cancel();
        fairLoss.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
