package cs451;

import cs451.broadcast.Broadcast;
import cs451.broadcast.FIFOBroadcast;
import cs451.broadcast.LCBroadcast;
import cs451.utils.Message;
import cs451.utils.Observer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Process implements Observer {

    private final byte id;

    private final int nbMessagesToBroadcast;

    private final String output;

    private final ConcurrentLinkedQueue<String> logs;

    private final Broadcast broadcast;

    public Process(int id, int port, int nbMessagesToBroadcast, List<Host> hosts, String output) {
        this.id = (byte) id;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.output = output;
        this.logs = new ConcurrentLinkedQueue<>();

        Map<Byte, Host> senderNbToHosts = new HashMap<>();
        for (var host : hosts) {
            senderNbToHosts.put((byte) host.getId(), host);
        }

        this.broadcast = new FIFOBroadcast(this, hosts, port, senderNbToHosts, this.id);
    }

    public Process(int id, int port, int nbMessagesToBroadcast, List<Host> hosts, String output, Set<Byte> locality) {
        this.id = (byte) id;
        this.nbMessagesToBroadcast = nbMessagesToBroadcast;
        this.output = output;
        this.logs = new ConcurrentLinkedQueue<>();

        Map<Byte, Host> senderNbToHosts = new HashMap<>();
        for (var host : hosts) {
            senderNbToHosts.put((byte) host.getId(), host);
        }

        this.broadcast = new LCBroadcast(this, hosts, port, senderNbToHosts, this.id, new HashSet<>(locality), (byte) (this.id - 1));
    }

    public void startBroadcasting() {
        broadcast.start();

        for (int i = 1; i < nbMessagesToBroadcast + 1; i++) {
            var message = new Message(i, id, id, false, null);

            broadcast.broadcast(message);

            // Logs broadcast message
            logs.add(String.format("b %d\n", message.getSeqNb()));
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
        logs.add(String.format("d %d %d\n", message.getOriginalSenderNb(), message.getSeqNb()));
    }
}
