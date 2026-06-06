package parking;

/**
 Represents a response sent from the server back to the client.
 
 Serialization format: JSON (hand-rolled, no external libraries required).
 
 Status codes follow HTTP conventions:
    200 = success
    400 = bad request / unknown command / validation error
    500 = unexpected server error
 
  JSON wire format:
    {"statusCode":200,"message":"Customer registered: abc-123 | Kalika"}
 
  This class replaces the old line-by-line server response where the server sent:
    Customer registered: abc-123\n
    end\n
 
  The new JSON approach returns a single structured line, making it easy for clients to distinguish success from error and extract the message programmatically.
 */

public class ParkingResponse {

    private final int    statusCode;
    private final String message;

    //Constructor + getters

    public ParkingResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message    = (message != null) ? message : "";
    }

    public int    getStatusCode() { return statusCode; }
    public String getMessage()    { return message; }

    //Serialization: ParkingResponse → JSON string

    /**
     Converts this ParkingResponse into a single-line JSON string suitable for transmission over a socket.
     
     Example output:
       {"statusCode":200,"message":"Customer registered: abc-123 | Kalika"}
     
      @return JSON string representation
     */

    public String toJson() {
        return "{\"statusCode\":" + statusCode
                + ",\"message\":\"" + escape(message) + "\"}";
    }

    //Deserialization: JSON string → ParkingResponse

    /**
     Parses a JSON string into a ParkingResponse.
     
     Returns a 400-status error response for null, blank, or malformed input.
     
     @param json the JSON string to parse
     @return a ParkingResponse constructed from the JSON
     */

    public static ParkingResponse fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ParkingResponse(400, "Empty response");
        }
        json = json.trim();

        int    statusCode = extractIntField(json, "statusCode");
        String message    = extractStringField(json, "message");
        return new ParkingResponse(statusCode, message);
    }

    //Minimal hand-rolled JSON helpers (no external dependency)

    private static int extractIntField(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 400;
        start += search.length();
        int end = start;
        while (end < json.length()
                && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 400;
        }
    }

    private static String extractStringField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();

        //Walk forward, handling escaped quotes
        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                sb.append(json.charAt(i + 1));
                i += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    //toString

    @Override
    public String toString() {
        return "ParkingResponse[statusCode=" + statusCode + ", message=" + message + "]";
    }
}
