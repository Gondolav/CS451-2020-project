package cs451.broadcast;

import cs451.Message;

public interface Broadcast {

    void broadcast(Message message);

    void start();

    void stop();
}
