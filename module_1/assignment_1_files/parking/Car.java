package parking;

import java.util.Objects;

// Models a vehicle owned by a customer. 

public class Car {
    private final String license;
    private final CarType type;
    private final Customer owner;
    private ParkingPermit permit;

    public Car(String license, CarType type, Customer owner) {
        this.license = license;
        this.type = type;
        this.owner = owner;
    }

    public String getLicense() {
        return license;
    }

    public CarType getType() {
        return type;
    }

    public Customer getOwner() {
        return owner;
    }

    public ParkingPermit getPermit() {
        return permit;
    }

    public void setPermit(ParkingPermit permit) {
        this.permit = permit;
    }

    @Override
    public String toString() {
        return String.format("Car['%s,%s,%s]", license, type, owner.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car)) return false;
        Car c = (Car) o;
        return Objects.equals(license, c.license);
    }

    @Override
    public int hashCode() {
        return Objects.hash(license);
    }
}
