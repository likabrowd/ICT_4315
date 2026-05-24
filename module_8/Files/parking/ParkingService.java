package parking;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ParkingService {

    private final ParkingOffice office;
    private final Map<String, Command> commands = new HashMap<>();

    public ParkingService(ParkingOffice office) {
        this.office = office;
        commands.put("CUSTOMER", new RegisterCustomerCommand(office));
        commands.put("CAR", new RegisterCarCommand(office));
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