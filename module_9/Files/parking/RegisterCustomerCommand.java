package parking;

import java.util.Properties;

public class RegisterCustomerCommand implements Command {

    private final ParkingOffice office;

    public RegisterCustomerCommand(ParkingOffice office) {
        this.office = office;
    }

    @Override
    public String execute(Properties params) {
        String firstName = params.getProperty("firstname", "").trim();
        String lastName  = params.getProperty("lastname", "").trim();
        String name      = (firstName + " " + lastName).trim();

        if (firstName.isEmpty()) {
            return "ERROR: firstname is required";
        }

        String phone  = params.getProperty("phone", "000-000-0000");
        String street = params.getProperty("street", "Unknown St");
        String city   = params.getProperty("city", "Denver");
        String state  = params.getProperty("state", "CO");
        String zip    = params.getProperty("zip", "00000");

        Address address  = new Address(street, city, state, zip);
        Customer customer = office.register(name, address, phone);

        return "Customer registered: " + customer.getCustomerId() + " | " + customer.getName();
    }
}