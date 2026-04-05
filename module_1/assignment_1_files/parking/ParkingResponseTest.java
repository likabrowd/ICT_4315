package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests ParkingResponse construction, toString, toJson, and fromJson round-trip.

public class ParkingResponseTest {

    @Test
    public void testConstructorAndGetters() {
        ParkingResponse resp = new ParkingResponse(200, "Customer registered: abc | Kalika");
        assertEquals(200, resp.getStatusCode());
        assertEquals("Customer registered: abc | Kalika", resp.getMessage());
    }

    @Test
    public void testToStringContainsStatusCode() {
        ParkingResponse resp = new ParkingResponse(400, "ERROR: bad input");
        String s = resp.toString();
        assertNotNull(s);
        assertTrue(s.contains("400"), "toString should contain status code");
    }

    @Test
    public void testToJsonStructure() {
        ParkingResponse resp = new ParkingResponse(200, "OK");
        String json = resp.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"statusCode\""), "JSON should have statusCode key");
        assertTrue(json.contains("200"),            "JSON should have status value 200");
        assertTrue(json.contains("\"message\""),    "JSON should have message key");
        assertTrue(json.contains("OK"),             "JSON should have message value");
    }

    @Test
    public void testRoundTrip200() {
        ParkingResponse original = new ParkingResponse(200, "Customer registered: abc-123 | Kalika");
        String json = original.toJson();
        ParkingResponse restored = ParkingResponse.fromJson(json);

        assertEquals(200,        restored.getStatusCode());
        assertEquals("Customer registered: abc-123 | Kalika", restored.getMessage());
    }

    @Test
    public void testRoundTrip400() {
        ParkingResponse original = new ParkingResponse(400, "ERROR: license is required");
        String json = original.toJson();
        ParkingResponse restored = ParkingResponse.fromJson(json);

        assertEquals(400, restored.getStatusCode());
        assertEquals("ERROR: license is required", restored.getMessage());
    }

    @Test
    public void testFromJsonNullReturnsErrorResponse() {
        ParkingResponse resp = ParkingResponse.fromJson(null);
        assertNotNull(resp);
        assertEquals(400, resp.getStatusCode());
    }
}
