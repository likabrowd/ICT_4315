package parking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

/**
 JSON-based parking server.
 
 <h3>Protocol (Assignment 7 — JSON serialization)</h3>
 <pre>
 Client → Server : one line of JSON representing a ParkingRequest
 Server → Client : one line of JSON representing a ParkingResponse
 </pre>
 
 <h4>Example request JSON:</h4>
 <pre>
    {"commandName":"CUSTOMER","params":{"firstname":"Kalika","phone":"303-123-4567"}}
  </pre>
 
  <h4>Example response JSON:</h4>
  <pre>
    {"statusCode":200,"message":"Customer registered: abc-123 | Kalika"}
  </pre>
 
  <p>Each client connection is handled on its own thread so the server can
  serve multiple simultaneous clients without blocking.</p>
 
  <p>The old line-by-line protocol (command\nparam=value\n…\nend\n) has been
  replaced entirely by this single-line JSON exchange.</p>
 */

public class Server {

    private static final int PORT = 5001;

    private final ParkingService service;

    //Construction

    public Server() {
        Address      officeAddr = new Address("2199 S. University Blvd.", "Denver", "CO", "80208");
        ParkingOffice office    = new ParkingOffice("DU Parking", officeAddr);
        this.service = new ParkingService(office);
    }

    //Server lifecycle

    /**
     Starts the server and blocks indefinitely, accepting client connections.
     Each connection is handled on a dedicated thread.
     */

    public void start() throws Exception {
        System.out.println("[Server] DU Parking JSON Server");
        System.out.println("[Server] Started at " + Instant.now());
        System.out.println("[Server] Listening on port " + PORT + " ...");
        System.out.println("[Server] Protocol: single-line JSON request/response");
        System.out.println("[Server] Commands: CUSTOMER, CAR");
        System.out.println();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                //Handle each client on its own thread — non-blocking for the server loop
                Thread clientThread = new Thread(() -> handleClient(client),
                        "client-" + client.getPort());
                clientThread.setDaemon(true);
                clientThread.start();
            }
        }
    }

    //Per-client handling

    /**
     Reads a single JSON line from the client, processes it, and writes back a single JSON response line.
     */
    private void handleClient(Socket client) {
        String clientAddr = client.getInetAddress() + ":" + client.getPort();
        System.out.println("[Server] Client connected: " + clientAddr);

        try (
            BufferedReader in  = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter    out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String requestJson = in.readLine();
            System.out.println("[Server] Received:  " + requestJson);

            ParkingResponse response     = process(requestJson);
            String          responseJson = response.toJson();
            System.out.println("[Server] Responding: " + responseJson);

            out.println(responseJson);

        } catch (Exception e) {
            System.err.println("[Server] Error with client " + clientAddr + ": " + e.getMessage());
        } finally {
            System.out.println("[Server] Client disconnected: " + clientAddr);
        }
    }

    //Request processing

    /**
     Parses a JSON request string, executes the command, and returns a JSON-serialisable response.
     
     This method is package-visible for unit testing without a live socket.
     
     @param requestJson raw JSON string from the client
     @return a ParkingResponse ready to be serialised back
     */

    public ParkingResponse process(String requestJson) {
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
            return new ParkingResponse(500, "ERROR: Internal server error — " + e.getMessage());
        }
    }

    //Entry point

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
