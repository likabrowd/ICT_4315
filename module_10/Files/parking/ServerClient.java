package parking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

/**
 Command-line JSON client for the DU Parking server.
 
  <h3>Protocol (Assignment 7 — JSON serialization)</h3>
  <p>Sends a single line of JSON (a {@link ParkingRequest}) to the server and reads back a single line of JSON (a {@link ParkingResponse}).</p>
 
  <p>This replaces the old line-by-line protocol where the client sent multiple lines terminated by "end".</p>
 
  <h3>Usage</h3>
  <pre>
    # Register a customer
    java -cp out parking.ServerClient CUSTOMER firstname=Kalika phone=303-123-4567
 
    # Register a car  (use the customerId returned from CUSTOMER)
    java -cp out parking.ServerClient CAR license=KAL4CO customerid=&lt;id&gt; cartype=COMPACT
  </pre>
 
  <p>The first argument is the command name (case-insensitive).
  Subsequent arguments are {@code key=value} pairs that become request parameters.
  Keys are lowercased automatically.</p>
 */

public class ServerClient {

    private static final String HOST = "localhost";
    private static final int    PORT = 5001;

    public static void main(String[] args) throws Exception {

        //Parse command-line arguments
        if (args.length < 1) {
            printUsage();
            return;
        }

        String     commandName = args[0].toUpperCase();
        Properties params      = new Properties();

        for (int i = 1; i < args.length; i++) {
            String[] parts = args[i].split("=", 2);
            if (parts.length == 2) {
                params.setProperty(parts[0].toLowerCase(), parts[1]);
            } else {
                System.out.println("[Client] Warning: ignoring malformed argument: " + args[i]);
            }
        }

        //Serialize request → JSON
        ParkingRequest request     = new ParkingRequest(commandName, params);
        String         requestJson = request.toJson();

        System.out.println("[Client] Command:   " + commandName);
        System.out.println("[Client] Params:    " + params);
        System.out.println("[Client] Sending:   " + requestJson);
        System.out.println("[Client] Connecting to " + HOST + ":" + PORT + " ...");

        //Send JSON over socket, read JSON response
        try (
            Socket         socket = new Socket(HOST, PORT);
            PrintWriter    out    = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in     = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(requestJson);                      // send one JSON line
            String responseJson = in.readLine();           // read one JSON line

            System.out.println("[Client] Received:  " + responseJson);

            //Deserialize response ← JSON
            ParkingResponse response = ParkingResponse.fromJson(responseJson);
            System.out.println("[Client] Status:    " + response.getStatusCode()
                    + (response.getStatusCode() == 200 ? " (OK)" : " (ERROR)"));
            System.out.println("[Client] Message:   " + response.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("DU Parking JSON Client");
        System.out.println();
        System.out.println("Usage:  java -cp out parking.ServerClient <COMMAND> [key=value ...]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  CUSTOMER  firstname=<name> [lastname=<name>] [phone=<phone>]");
        System.out.println("            [street=<street>] [city=<city>] [state=<state>] [zip=<zip>]");
        System.out.println();
        System.out.println("  CAR       license=<plate> customerid=<id> [cartype=COMPACT|SEDAN|SUV|TRUCK]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -cp out parking.ServerClient CUSTOMER firstname=Kalika phone=303-123-4567");
        System.out.println("  java -cp out parking.ServerClient CAR license=KAL4CO customerid=<uuid> cartype=COMPACT");
    }
}
