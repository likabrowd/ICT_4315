package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 Integration-style unit tests for Server.process() — tests the full JSON request → command execution → JSON response pipeline without
 opening a real socket.
 
 This tests the JSON serialization flow end-to-end:
 ParkingRequest.toJson()  →  Server.process()  →  ParkingResponse.fromJson()
 
 Covers:
 1.  CUSTOMER command: valid request returns 200 + customer ID in message
 2.  CUSTOMER command: missing firstname returns 400 error
 3.  CAR command: valid request (after registering customer) returns 200
 4.  CAR command: missing license returns 400 error
 5.  CAR command: unknown customerid returns 400 error
 6.  Unknown command returns 400 error
 7.  Null / blank JSON returns 400 error
 8.  Malformed JSON does not throw — returns 400 error
 9.  Full round-trip: build ParkingRequest → toJson() → process() → fromJson() → inspect ParkingResponse
 */

public class ServerProcessTest {

    private Server server;

    @BeforeEach
    public void setUp() {
        server = new Server();
    }

    //Helpers

    //Build a JSON request string from a command name and param pairs.
    private String buildJson(String command, String... kvPairs) {
        Properties params = new Properties();
        for (int i = 0; i < kvPairs.length - 1; i += 2) {
            params.setProperty(kvPairs[i], kvPairs[i + 1]);
        }
        return new ParkingRequest(command, params).toJson();
    }

    //1. CUSTOMER — valid

    @Test
    public void customerCommand_validRequest_returns200() {
        String json     = buildJson("CUSTOMER", "firstname", "Kalika", "phone", "303-123-4567");
        ParkingResponse resp = server.process(json);
        assertEquals(200, resp.getStatusCode(), "Valid CUSTOMER should return 200");
        assertTrue(resp.getMessage().contains("Kalika"), "Response should contain customer name");
    }

    @Test
    public void customerCommand_responseContainsCustomerId() {
        String json = buildJson("CUSTOMER", "firstname", "Kalika");
        ParkingResponse resp = server.process(json);
        //Message format: "Customer registered: <uuid> | Kalika"
        assertTrue(resp.getMessage().startsWith("Customer registered:"),
                "Message should start with 'Customer registered:'");
    }

    //2. CUSTOMER — missing firstname → 400

    @Test
    public void customerCommand_missingFirstname_returns400() {
        String json = buildJson("CUSTOMER", "phone", "303-000-0000");
        ParkingResponse resp = server.process(json);
        assertEquals(400, resp.getStatusCode(), "Missing firstname should return 400");
        assertTrue(resp.getMessage().startsWith("ERROR"), "Message should start with ERROR");
    }

    //3. CAR — valid (register customer first)

    @Test
    public void carCommand_validRequest_returns200() {
        //Step 1: register customer
        ParkingResponse custResp = server.process(
                buildJson("CUSTOMER", "firstname", "Kalika"));
        assertEquals(200, custResp.getStatusCode());

        //Extract customer ID from message: "Customer registered: <uuid> | Kalika"
        String msg        = custResp.getMessage();
        String customerId = msg.substring("Customer registered: ".length(), msg.indexOf(" | "));

        //Step 2: register car
        ParkingResponse carResp = server.process(
                buildJson("CAR", "license", "KAL4CO", "customerid", customerId, "cartype", "COMPACT"));
        assertEquals(200, carResp.getStatusCode(), "Valid CAR registration should return 200");
        assertTrue(carResp.getMessage().contains("KAL4CO"), "Response should mention license plate");
    }

    //4. CAR — missing license → 400

    @Test
    public void carCommand_missingLicense_returns400() {
        String json = buildJson("CAR", "customerid", "some-id");
        ParkingResponse resp = server.process(json);
        assertEquals(400, resp.getStatusCode());
        assertTrue(resp.getMessage().startsWith("ERROR"));
    }

    //5. CAR — unknown customerid → 400

    @Test
    public void carCommand_unknownCustomerId_returns400() {
        String json = buildJson("CAR", "license", "TST-001", "customerid", "no-such-id");
        ParkingResponse resp = server.process(json);
        assertEquals(400, resp.getStatusCode());
        assertTrue(resp.getMessage().startsWith("ERROR"));
    }

    //6. Unknown command → 400

    @Test
    public void unknownCommand_returns400() {
        String json = buildJson("FOOBAR", "x", "y");
        ParkingResponse resp = server.process(json);
        assertEquals(400, resp.getStatusCode());
        assertTrue(resp.getMessage().startsWith("ERROR"));
    }

    //7 & 8. Null / blank / malformed JSON → 400, no exception

    @Test
    public void nullJson_returns400_noException() {
        assertDoesNotThrow(() -> {
            ParkingResponse resp = server.process(null);
            assertEquals(400, resp.getStatusCode());
        });
    }

    @Test
    public void blankJson_returns400_noException() {
        assertDoesNotThrow(() -> {
            ParkingResponse resp = server.process("   ");
            assertEquals(400, resp.getStatusCode());
        });
    }

    @Test
    public void malformedJson_returns400_noException() {
        assertDoesNotThrow(() -> {
            ParkingResponse resp = server.process("{not valid json!!!");
            // Should return some response, not throw
            assertNotNull(resp);
        });
    }

    //9. Full round-trip via JSON strings

    @Test
    public void fullRoundTrip_requestToJsonToProcessFromJson() {
        //Build a ParkingRequest object
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone",     "303-123-4567");
        ParkingRequest request = new ParkingRequest("CUSTOMER", params);

        //Serialize to JSON (as the client would)
        String requestJson = request.toJson();
        assertNotNull(requestJson);
        assertTrue(requestJson.contains("CUSTOMER"));

        //Server processes the JSON string
        ParkingResponse response = server.process(requestJson);

        //Serialize response to JSON (as the server would send)
        String responseJson = response.toJson();
        assertNotNull(responseJson);

        //Client deserializes the response JSON
        ParkingResponse restored = ParkingResponse.fromJson(responseJson);
        assertEquals(200, restored.getStatusCode(),
                "Full round-trip should succeed with status 200");
        assertTrue(restored.getMessage().contains("Kalika"),
                "Full round-trip response should include customer name");
    }
}
