package cs451.links;

import cs451.Host;
import cs451.Message;

public interface Links {

    void send(Message message, Host host);

    void start();

    void stop();
}
