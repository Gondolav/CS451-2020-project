package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

class StubbornLinks implements Observer {

    private static final int ONE_MS = 1;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Host, Message> sent;
    private final Timer timer;

    StubbornLinks(Observer observer, int port) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>();

        // Implements the timer feature in StubbornLinks
        this.timer = new Timer();
    }

    void send(Message message, Host host) {
        fairLoss.send(message, host);
        sent.put(host, message);
    }

    void start() {
        fairLoss.start();
        while (true) {
            for (var entry : sent.entrySet()) {
                fairLoss.send(entry.getValue(), entry.getKey());
            }
        }
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                for (var entry : sent.entrySet()) {
//                    fairLoss.send(entry.getValue(), entry.getKey());
//                }
//            }
//        }, ONE_MS, ONE_MS);
    }

    void stop() {
        timer.cancel();
        fairLoss.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
