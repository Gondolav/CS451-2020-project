package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

class StubbornLinks implements Observer {

    private static final int FIVE_SECONDS = 5000;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Host, Message> sent;
    private final Timer timer;

    StubbornLinks(Observer observer, int port) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>();

        // Implements the timer feature in StubbornLinks
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (var entry : sent.entrySet()) {
                    fairLoss.send(entry.getValue(), entry.getKey());
                }
            }
        }, 0, FIVE_SECONDS);
    }

    void send(Message message, Host host) {
        fairLoss.send(message, host);
        sent.put(host, message);
    }

    void stop() {
        timer.cancel();
        fairLoss.stop();
    }

    @Override
    public void notify(Message message) { // like deliver
        observer.notify(message);
    }
}
