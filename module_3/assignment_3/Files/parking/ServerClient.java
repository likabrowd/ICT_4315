package parking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

//Command-line client for the parking server.

 /* Usage:
  java -cp out parking.ServerClient CUSTOMER firstname=Kalika phone=303-123-4567
  java -cp out parking.ServerClient CAR license=KAL4CO customerid=<id-from-above> cartype=COMPACT

  The first argument is the command name.
  Subsequent arguments are key=value pairs that become the request parameters.
 */


public class ServerClient {

    private static final String HOST = "localhost";
    private static final int    PORT = 5001;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ServerClient <COMMAND> [key=value ...]");
            System.out.println("  Example: ServerClient CUSTOMER firstname=Kalika");
            System.out.println("  Example: ServerClient CAR license=KAL4CO customerid=<id>");
            return;
        }

        //First arg = command name
        String commandName = args[0].toUpperCase();

        //Remaining args = key=value pairs
        Properties params = new Properties();
        for (int i = 1; i < args.length; i++) {
            String[] parts = args[i].split("=", 2);
            if (parts.length == 2) {
                params.setProperty(parts[0].toLowerCase(), parts[1]);
            } else {
                System.out.println("[Client] Warning: ignoring malformed argument: " + args[i]);
            }
        }

        //Build and serialize the request!
        ParkingRequest request = new ParkingRequest(commandName, params);
        String requestJson = request.toJson();
        System.out.println("[Client] Sending:  " + requestJson);

        //Open socket, send request, read response.
        try (
            Socket socket     = new Socket(HOST, PORT);
            PrintWriter out   = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(requestJson);
            String responseJson = in.readLine();
            System.out.println("[Client] Received: " + responseJson);

            ParkingResponse response = ParkingResponse.fromJson(responseJson);
            System.out.println("[Client] Status:   " + response.getStatusCode());
            System.out.println("[Client] Message:  " + response.getMessage());
        }
    }
}
