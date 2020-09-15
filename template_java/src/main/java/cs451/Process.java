package cs451;

import java.util.ArrayList;
import java.util.List;

// TODO
public class Process {

    private final int id;

    private final String ip;

    private final int port;

    private final int nbMessagesToBroadcast;

    private final List<Host> hosts;

    // TODO some field representing a log of all events

    public Process(int id, String ip, int port, int nbMessagesToBroadcast, List<Host> hosts) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.hosts = new ArrayList<>(hosts);
    }

    public void stopNetworkPacketProcessing() {
        // TODO
    }

    public void writeOutput() {
        // TODO
    }
}
