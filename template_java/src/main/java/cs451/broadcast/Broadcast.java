package cs451.broadcast;

import cs451.utils.Message;

public interface Broadcast {

    void broadcast(Message message);

    void start();

    void stop();
}
