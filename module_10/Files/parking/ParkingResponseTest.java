package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingResponse — JSON serialization protocol.
 
 Covers:
  1. Constructor and getters
  2. toString() contains status code
  3. toJson() structure (statusCode + message keys)
  4. Round-trip for 200 OK response
  5. Round-trip for 400 error response
  6. fromJson(null) returns 400 error response safely
  7. fromJson("") returns 400 error response safely
  8. Message with special characters survives round-trip
  9. Null message defaults to empty string safely
 */
public class ParkingResponseTest {

    //1. Constructor and getters

    @Test
    public void testConstructorAndGetters() {
        ParkingResponse resp = new ParkingResponse(200, "Customer registered: abc | Kalika");
        assertEquals(200, resp.getStatusCode());
        assertEquals("Customer registered: abc | Kalika", resp.getMessage());
    }

    //2. toString

    @Test
    public void testToStringContainsStatusCode() {
        ParkingResponse resp = new ParkingResponse(400, "ERROR: bad input");
        assertTrue(resp.toString().contains("400"), "toString should include status code");
    }

    //3. toJson structure

    @Test
    public void testToJsonContainsStatusCodeKey() {
        ParkingResponse resp = new ParkingResponse(200, "OK");
        assertTrue(resp.toJson().contains("\"statusCode\""), "JSON must contain 'statusCode' key");
    }

    @Test
    public void testToJsonContainsMessageKey() {
        ParkingResponse resp = new ParkingResponse(200, "OK");
        assertTrue(resp.toJson().contains("\"message\""), "JSON must contain 'message' key");
    }

    @Test
    public void testToJsonContainsStatusValue() {
        ParkingResponse resp = new ParkingResponse(200, "OK");
        assertTrue(resp.toJson().contains("200"), "JSON must include status value 200");
    }

    @Test
    public void testToJsonContainsMessageValue() {
        ParkingResponse resp = new ParkingResponse(200, "OK");
        assertTrue(resp.toJson().contains("OK"), "JSON must include message value 'OK'");
    }

    //4. Round-trip: 200 success

    @Test
    public void testRoundTrip200() {
        ParkingResponse original = new ParkingResponse(200, "Customer registered: abc-123 | Kalika");
        String          json     = original.toJson();
        ParkingResponse restored = ParkingResponse.fromJson(json);

        assertEquals(200, restored.getStatusCode());
        assertEquals("Customer registered: abc-123 | Kalika", restored.getMessage());
    }

    //5. Round-trip: 400 error

    @Test
    public void testRoundTrip400() {
        ParkingResponse original = new ParkingResponse(400, "ERROR: license is required");
        ParkingResponse restored = ParkingResponse.fromJson(original.toJson());

        assertEquals(400, restored.getStatusCode());
        assertEquals("ERROR: license is required", restored.getMessage());
    }

    //6 & 7. Null / blank safety

    @Test
    public void testFromJsonNullReturns400() {
        ParkingResponse resp = ParkingResponse.fromJson(null);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void testFromJsonBlankReturns400() {
        ParkingResponse resp = ParkingResponse.fromJson("");
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCode());
    }

    //8. Message with special chars survives round-trip

    @Test
    public void testMessageWithPipeCharSurvivesRoundTrip() {
        // Typical server message contains " | " separators
        ParkingResponse resp = new ParkingResponse(200, "Car registered: KAL4CO | permit=xyz | owner=Kalika");
        ParkingResponse restored = ParkingResponse.fromJson(resp.toJson());
        assertEquals("Car registered: KAL4CO | permit=xyz | owner=Kalika", restored.getMessage());
    }

    //9. Null message defaults to empty string

    @Test
    public void testNullMessageDefaultsToEmpty() {
        ParkingResponse resp = new ParkingResponse(200, null);
        assertEquals("", resp.getMessage());
        assertNotNull(resp.toJson());
    }
}
