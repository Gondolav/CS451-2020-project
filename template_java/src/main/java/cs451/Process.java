package cs451;

import cs451.broadcast.FIFOBroadcast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Process implements Observer {

    private final int id;

    private final int nbMessagesToBroadcast;

    private final String output;

    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    private final FIFOBroadcast fifoBroadcast;

    public Process(int id, int port, int nbMessagesToBroadcast, List<Host> hosts, String output) {
        this.id = id;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.output = output;
        this.fifoBroadcast = new FIFOBroadcast(this, hosts, port, id);
    }

    public void startBroadcasting() {
        fifoBroadcast.start();
        for (int i = 1; i < nbMessagesToBroadcast + 1; i++) {
            var message = new Message(i, id, id);
            fifoBroadcast.broadcast(message);

            // Logs broadcast message
            logs.add(String.format("b %d\n", message.getSeqNb()));
        }
    }

    public void stopNetworkPacketProcessing() {
        fifoBroadcast.stop();
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
        logs.add(String.format("d %d %d\n", message.getOriginalSenderNb(), message.getSeqNb()));
    }
}
