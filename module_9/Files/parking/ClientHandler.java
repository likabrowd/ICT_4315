package parking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

/**
 Runnable that handles a single client connection.
 
 Separating client-handling logic into its own class:
  - Keeps Server.java focused on accepting connections only.
  - Makes the handler independently testable.
  - Allows the same handler to be submitted to a ThreadPoolExecutor or run on a plain new Thread without any change to its logic.
 
 Each instance is constructed with the client Socket and the shared ParkingService, then run() reads one JSON request, processes it, and
 writes one JSON response before closing the socket.
 
 Thread-safety note: ParkingService / ParkingOffice are shared across all handler threads.  All mutations go through synchronized ArrayList
 wrappers or are otherwise isolated to construction time; no additional locking is required here.
 */

public class ClientHandler implements Runnable {

    private final Socket         socket;
    private final ParkingService service;

    public ClientHandler(Socket socket, ParkingService service) {
        this.socket  = socket;
        this.service = service;
    }

    @Override
    public void run() {
        String clientAddr = socket.getInetAddress() + ":" + socket.getPort();
        long   startMs    = System.currentTimeMillis();

        System.out.println("[Handler] Connected:  " + clientAddr
                + "  thread=" + Thread.currentThread().getName());

        try (
            BufferedReader in  = new BufferedReader(
                                     new InputStreamReader(socket.getInputStream()));
            PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String requestJson = in.readLine();
            System.out.println("[Handler] Received:   " + requestJson
                    + "  (" + clientAddr + ")");

            ParkingResponse response     = process(requestJson);
            String          responseJson = response.toJson();

            out.println(responseJson);
            System.out.println("[Handler] Responded:  " + responseJson
                    + "  (" + clientAddr + ")");

        } catch (Exception e) {
            System.err.println("[Handler] Error with " + clientAddr
                    + ": " + e.getMessage());
        } finally {
            long elapsedMs = System.currentTimeMillis() - startMs;
            System.out.println("[Handler] Disconnected: " + clientAddr
                    + "  handled in " + elapsedMs + " ms"
                    + "  thread=" + Thread.currentThread().getName());
            try { socket.close(); } catch (Exception ignored) { }
        }
    }

    /**
     Parses a JSON request string, executes the command via the shared ParkingService, and returns a ParkingResponse.
     
     Package-visible so Server (and tests) can call it directly without opening a socket.
     */
    
    ParkingResponse process(String requestJson) {
        if (requestJson == null || requestJson.isBlank()) {
            return new ParkingResponse(400, "ERROR: Empty request received");
        }
        try {
            ParkingRequest request = ParkingRequest.fromJson(requestJson);
            String result = service.performCommand(
                    request.getCommandName(), request.getParams());
            int code = result.startsWith("ERROR") ? 400 : 200;
            return new ParkingResponse(code, result);
        } catch (Exception e) {
            return new ParkingResponse(500,
                    "ERROR: Internal server error — " + e.getMessage());
        }
    }
}
