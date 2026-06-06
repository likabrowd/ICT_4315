package parking;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Multithreaded JSON parking server (updated for Assignment 9 :))
 
  <h3>Threading model</h3>
  A fixed-size thread pool ({@value #POOL_SIZE} threads) created with  {@link Executors#newFixedThreadPool} is used instead of spinning up a
  raw {@code new Thread()} per request.  Each accepted connection is wrapped in a {@link ClientHandler} Runnable and submitted to the pool.
 
 <p>Benefits over per-request threads:
 <ul>
 * <li>Thread creation overhead is paid once at startup, not per request.</li>
 *   <li>The pool size caps concurrent threads, preventing resource exhaustion under heavy load.</li>
 *   <li>The {@link ExecutorService} lifecycle methods make graceful shutdownstraightforward.</li>
 </ul>
 
  <h3>Shared state &amp; thread safety</h3>
    The single {@link ParkingService} (and the {@link ParkingOffice} it wraps)is shared across all handler threads.  Mutable collections inside
   {@code ParkingOffice} are wrapped with {@code Collections.synchronizedList} so that concurrent registrations do not corrupt the lists.
 
  <h3>Protocol (unchanged from Assignment 8)</h3>
  <pre>
    Client → Server : one line of JSON  (ParkingRequest)
    Server → Client : one line of JSON  (ParkingResponse)
  </pre>
 */

public class Server {

    private static final int PORT      = 5001;
    private static final int POOL_SIZE = 10;  // max concurrent client handlers

    private final ParkingService         service;
    private final ExecutorService        threadPool;
    private final AtomicInteger          requestCounter = new AtomicInteger(0);

    //Construction 

    public Server() {
        Address       officeAddr = new Address(
                "2199 S. University Blvd.", "Denver", "CO", "80208");
        ParkingOffice office     = new ParkingOffice("DU Parking", officeAddr);
        this.service    = new ParkingService(office);
        this.threadPool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    //Server lifecycle 

    /**
     Starts the server and blocks indefinitely, submitting each incoming connection to the thread pool for concurrent handling.
     */

    public void start() throws Exception {
        System.out.println("=== DU Parking Multithreaded JSON Server ===");
        System.out.println("[Server] Started at  : " + Instant.now());
        System.out.println("[Server] Port        : " + PORT);
        System.out.println("[Server] Thread pool : " + POOL_SIZE + " worker threads");
        System.out.println("[Server] Protocol    : single-line JSON request/response");
        System.out.println("[Server] Commands    : CUSTOMER, CAR");
        System.out.println("[Server] Press Ctrl+C to stop.");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown,
                "shutdown-hook"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept();
                int reqNum = requestCounter.incrementAndGet();
                System.out.println("[Server] Accepted connection #" + reqNum
                        + " from " + client.getInetAddress()
                        + ":" + client.getPort());

                //Hand the socket to a pool thread — main thread returns immediately to accept the next connection.
                threadPool.submit(new ClientHandler(client, service));
            }
        }
    }

    //Graceful shutdown: stop accepting new work, wait for in-flight handlers.
    private void shutdown() {
        System.out.println("[Server] Shutting down — waiting for in-flight requests...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("[Server] Shutdown complete. Total requests served: "
                + requestCounter.get());
    }

    //process() — kept for unit-test compatibility.

    /**
     Delegates to a temporary {@link ClientHandler} instance so that {@link ServerProcessTest} continues to work without a live socket.
     */
    public ParkingResponse process(String requestJson) {
        return new ClientHandler(null, service).process(requestJson);
    }

    //Entry point

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
