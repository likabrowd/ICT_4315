package parking;

import java.util.Properties;

// Registers a car for an existing customer and issues a ParkingPermit.
// Expected params: license, customerid, cartype (optional, defaults to COMPACT).

public class RegisterCarCommand implements Command {

    private final ParkingOffice office;

    public RegisterCarCommand(ParkingOffice office) {
        this.office = office;
    }

    @Override
    public String execute(Properties params) {
        String license    = params.getProperty("license", "");
        String customerId = params.getProperty("customerid", "");
        String typeStr    = params.getProperty("cartype", "COMPACT").toUpperCase();

        if (license.isEmpty()) {
            return "ERROR: license is required";
        }
        if (customerId.isEmpty()) {
            return "ERROR: customerid is required";
        }

        // Find the customer by ID
        Customer customer = office.getCustomers().stream()
                .filter(c -> c.getCustomerId().equalsIgnoreCase(customerId))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            return "ERROR: No customer found with id=" + customerId;
        }

        CarType type;
        try {
            type = CarType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = CarType.COMPACT;
        }

        Car car = office.register(customer, license, type);
        return "Car registered: " + car.getLicense()
                + " | permit=" + car.getPermit().getPermitId()
                + " | owner=" + customer.getName();
    }
}
