package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO
public class Process {

    private final int id;

    private final String ip;

    private final int port;

    private final int nbMessagesToBroadcast;

    private final List<Host> hosts;

    private final String output;

    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    // TODO some field representing a log of all events

    // TODO instantiate one receiver and multiple senders (one for each message)

    public Process(int id, String ip, int port, int nbMessagesToBroadcast, List<Host> hosts, String output) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.hosts = new ArrayList<>(hosts);
        this.output = output;
    }

    public void stopNetworkPacketProcessing() {
        // TODO receiver.stopReceiving()
    }

    public void writeOutput() {
        try (var outputStream = new FileOutputStream(output)) {
            logs.forEach(s -> {
                try {
                    outputStream.write(s.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logReceivedMessage(Message message) {
        logs.add(String.format("d %d %d", message.getSenderNb(), message.getSeqNb()));
    }

    public void logSentMessage(Message message) {
        logs.add(String.format("b %d", message.getSeqNb()));
    }
}
