package parking;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

//Tests ParkingRequest construction, toString, toJson, and fromJson round-trip.

public class ParkingRequestTest {

    @Test
    public void testConstructorAndGetters() {
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone", "303-123-4567");

        ParkingRequest req = new ParkingRequest("CUSTOMER", params);

        assertEquals("CUSTOMER", req.getCommandName());
        assertEquals("Kalika", req.getParams().getProperty("firstname"));
        assertEquals("303-123-4567", req.getParams().getProperty("phone"));
    }

    @Test
    public void testToStringContainsCommand() {
        ParkingRequest req = new ParkingRequest("CAR", new Properties());
        String s = req.toString();
        assertNotNull(s);
        assertTrue(s.contains("CAR"), "toString should contain the command name");
    }

    @Test
    public void testToJsonContainsCommandName() {
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");

        ParkingRequest req = new ParkingRequest("CUSTOMER", params);
        String json = req.toJson();

        assertNotNull(json);
        assertTrue(json.contains("\"commandName\""), "JSON should have commandName key");
        assertTrue(json.contains("CUSTOMER"),        "JSON should have CUSTOMER value");
        assertTrue(json.contains("Kalika"),          "JSON should have param value");
    }

    @Test
    public void testRoundTrip() {
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone", "303-123-4567");

        ParkingRequest original = new ParkingRequest("CUSTOMER", params);
        String json = original.toJson();

        ParkingRequest restored = ParkingRequest.fromJson(json);
        assertEquals("CUSTOMER",     restored.getCommandName());
        assertEquals("Kalika",       restored.getParams().getProperty("firstname"));
        assertEquals("303-123-4567", restored.getParams().getProperty("phone"));
    }

    @Test
    public void testFromJsonWithEmptyParams() {
        ParkingRequest req = new ParkingRequest("CUSTOMER", new Properties());
        String json = req.toJson();
        ParkingRequest restored = ParkingRequest.fromJson(json);
        assertEquals("CUSTOMER", restored.getCommandName());
        assertNotNull(restored.getParams());
    }

    @Test
    public void testFromJsonNullReturnsEmptyRequest() {
        ParkingRequest req = ParkingRequest.fromJson(null);
        assertNotNull(req);
        assertEquals("", req.getCommandName());
    }
}
