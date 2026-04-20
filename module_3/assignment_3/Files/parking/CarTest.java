package parking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

//Verifies car creation, permit assignment, and equality in the system :) 

public class CarTest {

    @Test
    public void testEqualityByLicensePlate() {
        Customer owner = new Customer("Eve", new Address("373 Cold St", "Denver", "CO", "80204"), "720-555-0000");
        Car car1 = new Car("EEV-123", CarType.COMPACT, owner);
        Car car2 = new Car("EEV-123", CarType.COMPACT, owner);
        assertEquals(car1, car2);
        assertEquals(car1.hashCode(), car2.hashCode());
    }

    @Test
    public void testSetPermit() {
        Customer owner = new Customer("Tom", new Address("404 Den St", "Denver", "CO", "80205"), "720-555-1111");
        Car car = new Car("TUM-100", CarType.SUV, owner);
        ParkingPermit p = new ParkingPermit();
        car.setPermit(p);
        assertEquals(p, car.getPermit());
    }
}
