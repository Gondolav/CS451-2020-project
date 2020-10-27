package cs451.links;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class StubbornLinks implements Observer, Links {

    private static final long TIMEOUT = 200;

    private final Observer observer;
    private final FairLossLinks fairLoss;

    private final Map<Pair<Host, Integer>, Message> sent;
    private final Timer timer;

    private final Map<Byte, Host> senderNbToHosts;

    private final byte senderNb;

    private final ReentrantLock lock = new ReentrantLock();

    StubbornLinks(Observer observer, int port, Map<Byte, Host> senderNbToHosts, byte senderNb) {
        this.observer = observer;
        this.fairLoss = new FairLossLinks(this, port);
        this.sent = new HashMap<>();

        this.timer = new Timer();

        this.senderNbToHosts = new HashMap<>(senderNbToHosts);

        this.senderNb = senderNb;
    }

    @Override
    public void send(Message message, Host host) {
        if (!message.isAck()) {
            lock.lock();
            var pair = new Pair<>(host, message.getSeqNb());
            sent.put(pair, message);
            lock.unlock();
        }
        fairLoss.send(message, host);
    }

    @Override
    public void start() {
        fairLoss.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lock.lock();
                for (var entry : sent.entrySet()) {
                    fairLoss.send(entry.getValue(), entry.getKey().first);
                }
                lock.unlock();
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
            lock.lock();
            sent.remove(new Pair<>(senderHost, message.getSeqNb()));
            lock.unlock();
        } else {
            send(new Message(message.getSeqNb(), senderNb, message.getOriginalSenderNb(), true), senderHost);
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
