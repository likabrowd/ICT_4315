package parking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

//Represents a parking customer + stores info and ownership of one or more cars. 
public class Customer {
    private final String customerId = UUID.randomUUID().toString();
    private final String name;
    private final Address address;
    private final String phone;
    private final List<Car> cars = new ArrayList<>();

    public Customer(String name, Address address, String phone) {
        this.name = name; this.address = address; this.phone = phone;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public List<Car> getCars() { return cars; }

    public void addCar(Car c) { cars.add(c); }

    @Override public String toString() {
        return String.format("Customer[%s, %s]", name, customerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer c = (Customer) o;
        return Objects.equals(customerId, c.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}
