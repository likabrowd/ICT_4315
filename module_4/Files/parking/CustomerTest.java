package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Ensures that customers correctly store personal info and maintain ownership of their cars. 
public class CustomerTest {

    @Test
    public void testEqualsAndHashCodeByCustomerId() {
        Address addr = new Address("189 Ash St", "Denver", "CO", "80202");
        Customer c1 = new Customer("Sara", addr, "303-000-0004");
        Customer c2 = new Customer("Sara", addr, "303-000-0004");

        //Different generated IDs, not equal. 
        assertNotEquals(c1, c2);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testAddCarMaintainsOwnership() {
        Customer c = new Customer("Bob", new Address("245 Bart St", "Denver", "CO", "80203"), "303-112-1111");
        Car car = new Car("XYZ-222", CarType.SUV, c);
        c.addCar(car);
        assertTrue(c.getCars().contains(car));
        assertEquals(c, car.getOwner());
    }
}
