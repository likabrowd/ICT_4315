package parking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 5001;
    private final ParkingService service;

    public Server() {
        Address officeAddr = new Address("2199 S. University Blvd.", "Denver", "CO", "80208");
        ParkingOffice office = new ParkingOffice("DU Parking", officeAddr);
        this.service = new ParkingService(office);
    }

    public void start() throws Exception {
        System.out.println("[Server] Listening on port " + PORT + " ...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("[Server] Client connected: " + client.getInetAddress());
                handleClient(client);
            }
        }
    }

    private void handleClient(Socket client) {
        try (
            BufferedReader in  = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter    out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String jsonLine = in.readLine();
            System.out.println("[Server] Received: " + jsonLine);

            ParkingResponse response = process(jsonLine);
            String responseJson = response.toJson();
            System.out.println("[Server] Sending:  " + responseJson);
            out.println(responseJson);

        } catch (Exception e) {
            System.err.println("[Server] Error handling client: " + e.getMessage());
        }
    }

    public ParkingResponse process(String requestJson) {
        try {
            ParkingRequest request = ParkingRequest.fromJson(requestJson);
            String result = service.performCommand(request.getCommandName(), request.getParams());

            //Determine status code.
            int code = result.startsWith("ERROR") ? 400 : 200;
            return new ParkingResponse(code, result);

        } catch (Exception e) {
            return new ParkingResponse(400, "ERROR: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}