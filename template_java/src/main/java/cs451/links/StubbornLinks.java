package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class StubbornLinks implements Observer, Links {

    private static final int ONE_SECOND = 1000;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Host, Set<Message>> sent;
    private final Timer timer;

    private final Map<Integer, Host> senderNbToHosts;

    private final int senderNb;

    StubbornLinks(Observer observer, int port, Map<Integer, Host> senderNbToHosts, int senderNb) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>();

        this.timer = new Timer();

        this.senderNbToHosts = new HashMap<>(senderNbToHosts);

        this.senderNb = senderNb;
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
        }, ONE_SECOND, ONE_SECOND);
    }

    @Override
    public void stop() {
        timer.cancel();
        fairLoss.stop();
    }

    @Override
    public void deliver(Message message) {
        var senderHost = senderNbToHosts.get(message.getSenderNb());

        if (message.isAck()) {
            sent.get(senderHost).removeIf(m -> m.getSeqNb() == message.getSeqNb());
        } else {
            send(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb(), true), senderHost);
            observer.deliver(message);
        }
    }
}
