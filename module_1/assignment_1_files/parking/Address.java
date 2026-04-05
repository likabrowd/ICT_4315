package parking;

// Represents a physical street address. 

public class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zip;

    public Address(String street, String city, String state, String zip) {
        this.street = street; this.city = city; this.state = state; this.zip = zip;
    }

    @Override public String toString() {
        return String.format("%s, %s, %s %s", street, city, state, zip);
    }
}
