package parking.inject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import parking.ClientHandler;
import parking.ParkingResponse;
import parking.ParkingService;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Multithreaded JSON parking server — refactored to use Guice DI.
 
 Key DI change from Assignment 9:
 The original Server constructed its own ParkingService (and ParkingOffice) inside its constructor, hard-coding the dependency.  This version receives
 ParkingService through its constructor, annotated with @Inject.
 
 Benefits:
   - Tests can inject a mock or pre-configured ParkingService without subclassing or reflection tricks.
   - The class has no knowledge of how ParkingService is created; ParkingModule owns that decision.
   - @Singleton on the class ensures Guice creates exactly one instance.
 */

@Singleton
public class InjectedServer {

    private static final int PORT      = 5001;
    private static final int POOL_SIZE = 10;

    private final ParkingService  service;
    private final ExecutorService threadPool;
    private final AtomicInteger   requestCounter = new AtomicInteger(0);

    /**
     Constructor injection: Guice supplies the ParkingService.
     No "new ParkingService(new ParkingOffice(...))" buried in this class.
     
     @param service the shared ParkingService provided by ParkingModule
     */

    @Inject
    public InjectedServer(ParkingService service) {
        this.service    = service;
        this.threadPool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    /**Start the server and block, accepting connections on PORT. */
    public void start() throws Exception {
        System.out.println("=== DU Parking Server (Guice DI Edition) ===");
        System.out.println("[Server] Started at  : " + Instant.now());
        System.out.println("[Server] Port        : " + PORT);
        System.out.println("[Server] Thread pool : " + POOL_SIZE + " workers");
        System.out.println("[Server] DI framework: Google Guice 7.0.0");
        System.out.println("[Server] Press Ctrl+C to stop.");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(
                new Thread(this::shutdown, "shutdown-hook"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept();
                int reqNum = requestCounter.incrementAndGet();
                System.out.println("[Server] Accepted connection #" + reqNum
                        + " from " + client.getInetAddress()
                        + ":" + client.getPort());
                threadPool.submit(new ClientHandler(client, service));
            }
        }
    }

    /**Delegate to a temporary ClientHandler for unit-test convenience. */
    public ParkingResponse process(String requestJson) {
        return new ClientHandler(null, service).process(requestJson);
    }

    private void shutdown() {
        System.out.println("[Server] Shutting down...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("[Server] Done. Requests served: "
                + requestCounter.get());
    }
}
