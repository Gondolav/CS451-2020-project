package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class StubbornLinks implements Observer {

    private static final int ONE_SECOND = 1000;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Host, Message> sent;
    private final Timer timer;

    public StubbornLinks(Observer observer, int port) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>();

        // Implements the timer feature in StubbornLinks
        this.timer = new Timer();
    }

    public void send(Message message, Host host) {
        fairLoss.send(message, host);
        sent.put(host, message);
        System.out.println("Stubborn send: " + message);
    }

    public void start() {
        fairLoss.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (var entry : sent.entrySet()) {
                    System.out.println("Stubborn send in loop: " + entry.getValue());
                    fairLoss.send(entry.getValue(), entry.getKey());
                }
            }
        }, ONE_SECOND, ONE_SECOND);
    }

    public void stop() {
        timer.cancel();
        fairLoss.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
        System.out.println("Stubborn deliver: " + message);
    }
}
