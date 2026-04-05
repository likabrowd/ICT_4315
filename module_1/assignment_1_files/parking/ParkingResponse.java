package parking;

// Represents a response sent from the server back to the client.


 /* statusCode 200 = success, 400 = bad request / error.
 
  JSON format:
  {"statusCode":200,"message":"Customer registered: abc-123 | Kalika"}
 */


public class ParkingResponse {

    private final int    statusCode;
    private final String message;

    public ParkingResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message    = (message != null) ? message : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    // Serialization: ParkingResponse → JSON string

    public String toJson() {
        return "{\"statusCode\":" + statusCode
                + ",\"message\":\"" + escape(message) + "\"}";
    }


    // Deserialization: JSON string → ParkingResponse
    public static ParkingResponse fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ParkingResponse(400, "Empty response");
        }
        json = json.trim();

        int statusCode = extractIntField(json, "statusCode");
        String message = extractStringField(json, "message");
        return new ParkingResponse(statusCode, message);
    }


    // Minimal JSON helpers!
    private static int extractIntField(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 400;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
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
        
        // walk forward handling escaped quotes
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

    @Override
    public String toString() {
        return "ParkingResponse[statusCode=" + statusCode + ", message=" + message + "]";
    }
}
