package parking;

import java.util.Properties;

//Represents a request sent from the client to the server.
//Serializes to/from a simple JSON format without any external library.
 
 /* JSON format:
 * {
 *   "commandName": "CUSTOMER",
 *   "params": {
 *     "firstname": "Kalika",
 *     "phone": "303-123-4567"
 *   }
 * }
 */

public class ParkingRequest {

    private final String commandName;
    private final Properties params;

    public ParkingRequest(String commandName, Properties params) {
        this.commandName = commandName;
        this.params      = (params != null) ? params : new Properties();
    }

    public String getCommandName() {
        return commandName;
    }

    public Properties getParams() {
        return params;
    }

    //Serialization: ParkingRequest → JSON string

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"commandName\":\"").append(escape(commandName)).append("\",");
        sb.append("\"params\":{");

        boolean first = true;
        for (String key : params.stringPropertyNames()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escape(key)).append("\":");
            sb.append("\"").append(escape(params.getProperty(key))).append("\"");
            first = false;
        }

        sb.append("}}");
        return sb.toString();
    }

    //Deserialization: JSON string → ParkingRequest

    public static ParkingRequest fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ParkingRequest("", new Properties());
        }
        json = json.trim();

        String commandName = extractStringField(json, "commandName");
        Properties params  = extractParams(json);
        return new ParkingRequest(commandName, params);
    }

    //Minimal JSON helpers


    //Extracts a top-level string field value by key. 
    private static String extractStringField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return unescape(json.substring(start, end));
    }

    //Extracts the "params" object as a Properties map.
    private static Properties extractParams(String json) {
        Properties props = new Properties();
        int paramsStart = json.indexOf("\"params\":{");
        if (paramsStart < 0) return props;
        paramsStart += "\"params\":{".length();
        int paramsEnd = json.indexOf("}", paramsStart);
        if (paramsEnd < 0) return props;
        String paramsJson = json.substring(paramsStart, paramsEnd);

        //parse "key":"value" pairs
        
        int pos = 0;
        while (pos < paramsJson.length()) {
            int kStart = paramsJson.indexOf("\"", pos);
            if (kStart < 0) break;
            int kEnd = paramsJson.indexOf("\"", kStart + 1);
            if (kEnd < 0) break;
            String k = unescape(paramsJson.substring(kStart + 1, kEnd));

            int colon = paramsJson.indexOf(":", kEnd + 1);
            if (colon < 0) break;
            int vStart = paramsJson.indexOf("\"", colon + 1);
            if (vStart < 0) break;
            int vEnd = paramsJson.indexOf("\"", vStart + 1);
            if (vEnd < 0) break;
            String v = unescape(paramsJson.substring(vStart + 1, vEnd));

            props.setProperty(k, v);
            pos = vEnd + 1;
        }
        return props;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    @Override
    public String toString() {
        return "ParkingRequest[command=" + commandName + ", params=" + params + "]";
    }
}
