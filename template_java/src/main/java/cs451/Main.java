package cs451;

import cs451.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static Process pr;

    private static void handleSignal() {
        // Immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        pr.stopNetworkPacketProcessing();

        // Write/flush output file if necessary
        System.out.println("Writing output.");
        pr.writeOutput();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::handleSignal));
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        for (Host host : parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()

        List<String> configLines = null;
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
            try (Stream<String> stream = Files.lines(Paths.get(parser.config()))) {
                configLines = stream.collect(Collectors.toUnmodifiableList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int nbMessagesToBroadcast = 0;
        HashMap<Byte, Set<Byte>> locality = new HashMap<>();
        if (configLines != null) {
            nbMessagesToBroadcast = Integer.parseInt(configLines.get(0));
            int size = configLines.size();
            if (size > 1) {
                for (String line : configLines.subList(1, size)) {
                    var split = line.split(" ");
                    byte process = Byte.parseByte(split[0]);
                    locality.put(process, Arrays.stream(split).map(Byte::valueOf).collect(Collectors.toUnmodifiableSet()));
                }
            }
        }

        // Go through each host, find the one associated to our id, create new process and new list of hosts
        for (var host : parser.hosts()) {
            if (host.getId() == parser.myId()) {
                if (!locality.isEmpty()) {
                    System.out.println(locality.toString());
                    // LCBroadcast
                    pr = new Process(host.getId(), host.getPort(), nbMessagesToBroadcast, parser.hosts(), parser.output(), locality);
                } else {
                    // FIFO
                    pr = new Process(host.getId(), host.getPort(), nbMessagesToBroadcast, parser.hosts(), parser.output());
                }
                break;
            }
        }

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");
        pr.startBroadcasting();

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
