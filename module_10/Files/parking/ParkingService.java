package parking;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 Central dispatcher for parking commands.
 
 DI update for Assignment 10: constructor is annotated with @Inject so that Guice can supply the ParkingOffice dependency automatically.
 No changes to logic from previous parts :) 
 */

public class ParkingService {

    private final ParkingOffice office;
    private final Map<String, Command> commands = new HashMap<>();

    @Inject
    public ParkingService(ParkingOffice office) {
        this.office = office;
        commands.put("CUSTOMER", new RegisterCustomerCommand(office));
        commands.put("CAR",      new RegisterCarCommand(office));
    }

    public String performCommand(String commandName, Properties params) {
        if (commandName == null || commandName.isBlank()) {
            return "ERROR: No command specified";
        }
        Command cmd = commands.get(commandName.toUpperCase());
        if (cmd == null) {
            return "ERROR: Unknown command: " + commandName;
        }
        return cmd.execute(params);
    }

    public ParkingOffice getOffice() {
        return office;
    }
}
