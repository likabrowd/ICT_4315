package parking;

import java.util.Properties;

//Command interface for the parking system. Each command encapsulates a single operation (e.g. register customer, register car).

public interface Command {
    // Execute this command with the given parameters.
    //@param params key-value pairs parsed from the request
    //@return a result message string
    String execute(Properties params);
}
