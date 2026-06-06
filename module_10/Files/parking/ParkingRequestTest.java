package parking;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingRequest — JSON serialization protocol.
 
  Covers:
  1. Constructor and getters
  2. toString() contains command name
  3. toJson() produces valid JSON with commandName and params
  4. fromJson() round-trip: CUSTOMER command with multiple params
  5. fromJson() round-trip: CAR command with multiple params
  6. fromJson(null) returns empty request safely
  7. fromJson("") returns empty request safely
  8. toJson() with empty params produces valid JSON
  9. Special characters in values are escaped/unescaped correctly
 */

public class ParkingRequestTest {

    //1. Constructor and getters

    @Test
    public void testConstructorAndGetters() {
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone",     "303-123-4567");

        ParkingRequest req = new ParkingRequest("CUSTOMER", params);

        assertEquals("CUSTOMER",     req.getCommandName());
        assertEquals("Kalika",       req.getParams().getProperty("firstname"));
        assertEquals("303-123-4567", req.getParams().getProperty("phone"));
    }

    //2. toString

    @Test
    public void testToStringContainsCommandName() {
        ParkingRequest req = new ParkingRequest("CAR", new Properties());
        assertTrue(req.toString().contains("CAR"), "toString should include the command name");
    }

    //3. toJson — structure checks

    @Test
    public void testToJsonContainsCommandNameKey() {
        ParkingRequest req = new ParkingRequest("CUSTOMER", new Properties());
        String json = req.toJson();
        assertTrue(json.contains("\"commandName\""), "JSON must contain 'commandName' key");
    }

    @Test
    public void testToJsonContainsCommandNameValue() {
        ParkingRequest req = new ParkingRequest("CUSTOMER", new Properties());
        assertTrue(req.toJson().contains("CUSTOMER"), "JSON must contain command value 'CUSTOMER'");
    }

    @Test
    public void testToJsonContainsParamsKey() {
        Properties p = new Properties();
        p.setProperty("firstname", "Kalika");
        ParkingRequest req = new ParkingRequest("CUSTOMER", p);
        assertTrue(req.toJson().contains("\"params\""), "JSON must contain 'params' key");
        assertTrue(req.toJson().contains("Kalika"),     "JSON must include param value 'Kalika'");
    }

    @Test
    public void testToJsonWithEmptyParamsIsValid() {
        ParkingRequest req = new ParkingRequest("CUSTOMER", new Properties());
        String json = req.toJson();
        assertNotNull(json);
        assertTrue(json.startsWith("{"), "JSON must start with '{'");
        assertTrue(json.endsWith("}"),   "JSON must end with '}'");
        assertTrue(json.contains("\"params\":{}"), "Empty params should produce \"params\":{}");
    }

    //4. Round-trip: CUSTOMER command

    @Test
    public void testRoundTripCustomerCommand() {
        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone",     "303-123-4567");

        ParkingRequest original = new ParkingRequest("CUSTOMER", params);
        String json = original.toJson();

        ParkingRequest restored = ParkingRequest.fromJson(json);

        assertEquals("CUSTOMER",     restored.getCommandName());
        assertEquals("Kalika",       restored.getParams().getProperty("firstname"));
        assertEquals("303-123-4567", restored.getParams().getProperty("phone"));
    }

    //5. Round-trip: CAR command

    @Test
    public void testRoundTripCarCommand() {
        Properties params = new Properties();
        params.setProperty("license",    "KAL4CO");
        params.setProperty("customerid", "abc-123");
        params.setProperty("cartype",    "COMPACT");

        ParkingRequest original = new ParkingRequest("CAR", params);
        ParkingRequest restored = ParkingRequest.fromJson(original.toJson());

        assertEquals("CAR",     restored.getCommandName());
        assertEquals("KAL4CO",  restored.getParams().getProperty("license"));
        assertEquals("abc-123", restored.getParams().getProperty("customerid"));
        assertEquals("COMPACT", restored.getParams().getProperty("cartype"));
    }

    //6 & 7. Null / blank safety

    @Test
    public void testFromJsonNullReturnsEmptyRequest() {
        ParkingRequest req = ParkingRequest.fromJson(null);
        assertNotNull(req);
        assertEquals("", req.getCommandName());
    }

    @Test
    public void testFromJsonBlankReturnsEmptyRequest() {
        ParkingRequest req = ParkingRequest.fromJson("  ");
        assertNotNull(req);
        assertEquals("", req.getCommandName());
    }

    //8. Null constructor params default safely

    @Test
    public void testNullParamsDefaultToEmptyProperties() {
        ParkingRequest req = new ParkingRequest("CUSTOMER", null);
        assertNotNull(req.getParams());
        String json = req.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"params\":{}"));
    }

    //9. Special characters are handled

    @Test
    public void testSpecialCharactersInParamValues() {
        Properties params = new Properties();
        params.setProperty("name", "O'Brien");   // apostrophe — fine as-is
        ParkingRequest req = new ParkingRequest("CUSTOMER", params);
        ParkingRequest restored = ParkingRequest.fromJson(req.toJson());
        assertEquals("O'Brien", restored.getParams().getProperty("name"));
    }
}
