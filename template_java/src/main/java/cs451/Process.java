package cs451;

import cs451.broadcast.FIFOBroadcast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Process implements Observer {

    private final int id;

    private final int nbMessagesToBroadcast;

    private final int totalNbMessagesInQueue;

    private final String output;

    private final ConcurrentLinkedQueue<String> logs;

    private final FIFOBroadcast broadcast;

    public Process(int id, int port, int nbMessagesToBroadcast, List<Host> hosts, String output) {
        this.id = id;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.totalNbMessagesInQueue = nbMessagesToBroadcast * hosts.size(); // add +1 when using BEB
        this.output = output;
        this.logs = new ConcurrentLinkedQueue<>();
        this.broadcast = new FIFOBroadcast(this, hosts, port, id);
    }

    public void startBroadcasting() {
        broadcast.start();
        for (int i = 1; i < nbMessagesToBroadcast + 1; i++) {
            var message = new Message(i, id, id);

            broadcast.broadcast(message);

            // Logs broadcast message
            logs.add(String.format("b %d\n", message.getSeqNb()));
        }

        while (logs.size() < totalNbMessagesInQueue) {
        }
    }

    public void stopNetworkPacketProcessing() {
        broadcast.stop();
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

    @Override
    public void deliver(Message message) {
        logs.add(String.format("d %d %d\n", message.getSenderNb(), message.getSeqNb()));
    }
}
