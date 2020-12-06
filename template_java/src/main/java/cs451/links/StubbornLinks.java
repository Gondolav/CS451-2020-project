package cs451.links;

import cs451.Host;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class StubbornLinks implements Observer, Links {

    private static final long TIMEOUT = 200L;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Pair<Host, Integer>, Message> sent;

    private final Timer timer;

    private final Map<Byte, Host> senderNbToHosts;

    private final byte senderNb;

    StubbornLinks(Observer observer, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new ConcurrentHashMap<>(1000);

        this.timer = new Timer();

        this.senderNbToHosts = new HashMap<>(senderNbToHosts);

        this.senderNb = senderNb;
    }

    @Override
    public void send(Message message, Host host) {
        if (!message.isAck()) {
            var pair = new Pair<>(host, message.getSeqNb());
            sent.put(pair, message);
        }
        fairLoss.send(message, host);
    }

    @Override
    public void start() {
        fairLoss.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                var toSend = new HashMap<>(sent);
                toSend.entrySet().stream().sorted(Comparator.comparing(o -> o.getKey().second))
                        .forEach(entry -> fairLoss.send(entry.getValue(), entry.getKey().first));
            }
        }, TIMEOUT, TIMEOUT);
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
            sent.remove(new Pair<>(senderHost, message.getSeqNb()));
        } else {
            send(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb(), true, message.getVectorClock()), senderHost);
            observer.deliver(message);
        }
    }

    private static class Pair<X, Y> {
        private final X first;
        private final Y second;

        private Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }
}
