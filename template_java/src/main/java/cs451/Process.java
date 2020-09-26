package cs451;

import cs451.links.PerfectLinks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO
public class Process implements Observer {

    private final int id;

    private final String ip;

    private final int port;

    private final int nbMessagesToBroadcast;

    private List<Host> hosts;

    private final String output;

    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    private final PerfectLinks fifoBroadcast;

    public Process(int id, String ip, int port, int nbMessagesToBroadcast, List<Host> hosts, String output) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.hosts = new ArrayList<>(hosts);
        this.output = output;
        this.fifoBroadcast = new PerfectLinks(this, port);
//        this.fifoBroadcast = new FIFOBroadcast(this, hosts, port, id);
    }

    public void startBroadcasting() {
        fifoBroadcast.start();
        for (int i = 1; i < nbMessagesToBroadcast + 1; i++) {
            var message = new Message(i, id, id);
//            fifoBroadcast.broadcast(message);
            fifoBroadcast.send(message, hosts.get(1));

            // Logs broadcasted message
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
        System.out.println("Entering here");
        logs.add(String.format("d %d %d\n", message.getOriginalSenderNb(), message.getSeqNb()));
    }
}
