package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ClientHandler.process() — verifies the extracted handler logic in isolation, without opening any real sockets.
 
 These mirror the ServerProcessTest suite but target ClientHandler directly,confirming that the refactoring did not break request processing.
 */

public class ClientHandlerTest {

    private ClientHandler handler;

    @BeforeEach
    public void setUp() {
        Address       addr    = new Address("2199 S. University Blvd.",
                                            "Denver", "CO", "80208");
        ParkingOffice office  = new ParkingOffice("DU Parking", addr);
        ParkingService service = new ParkingService(office);
        // null socket is fine — process() never uses it
        handler = new ClientHandler(null, service);
    }

    //Helper

    private String buildJson(String command, String... kvPairs) {
        Properties params = new Properties();
        for (int i = 0; i + 1 < kvPairs.length; i += 2) {
            params.setProperty(kvPairs[i], kvPairs[i + 1]);
        }
        return new ParkingRequest(command, params).toJson();
    }

    //CUSTOMER 

    @Test
    public void validCustomer_returns200() {
        ParkingResponse resp = handler.process(
                buildJson("CUSTOMER", "firstname", "Kalika", "phone", "303-123-4567"));
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getMessage().contains("Kalika"));
    }

    @Test
    public void missingFirstname_returns400() {
        ParkingResponse resp = handler.process(
                buildJson("CUSTOMER", "phone", "303-000-0000"));
        assertEquals(400, resp.getStatusCode());
        assertTrue(resp.getMessage().startsWith("ERROR"));
    }

    //CAR

    @Test
    public void validCar_returns200() {
        //Register customer first
        ParkingResponse custResp = handler.process(
                buildJson("CUSTOMER", "firstname", "Kalika"));
        assertEquals(200, custResp.getStatusCode());

        String msg        = custResp.getMessage();
        String customerId = msg.substring("Customer registered: ".length(),
                                          msg.indexOf(" | "));

        ParkingResponse carResp = handler.process(
                buildJson("CAR", "license", "KAL4CO",
                          "customerid", customerId, "cartype", "COMPACT"));
        assertEquals(200, carResp.getStatusCode());
        assertTrue(carResp.getMessage().contains("KAL4CO"));
    }

    @Test
    public void missingLicense_returns400() {
        ParkingResponse resp = handler.process(
                buildJson("CAR", "customerid", "some-id"));
        assertEquals(400, resp.getStatusCode());
    }

    //Edge cases

    @Test
    public void nullJson_returns400() {
        ParkingResponse resp = handler.process(null);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void blankJson_returns400() {
        ParkingResponse resp = handler.process("   ");
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void unknownCommand_returns400() {
        ParkingResponse resp = handler.process(buildJson("BOGUS", "x", "y"));
        assertEquals(400, resp.getStatusCode());
    }
}
