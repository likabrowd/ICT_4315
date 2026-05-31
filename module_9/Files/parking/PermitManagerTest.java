package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Makes sure that PermitManager correctly issues and stores ParkingPermit objects. 

public class PermitManagerTest {

    @Test
    public void testRegisterBindsPermitToCar() {
        PermitManager pm = new PermitManager();
        Customer c = new Customer("Mary", new Address("147 Cherry St", "Denver", "CO", "80202"), "303-000-0000");
        Car car = new Car("KAL-123", CarType.SEDAN, c);

        Permit permit = pm.register(car);

        assertNotNull(permit);
        assertNotNull(car.getPermit());
        assertEquals(permit, car.getPermit());
        assertTrue(pm.findById(permit.getPermitId()).isPresent());
        assertTrue(pm.findByLicense("KAL-123").isPresent());
    }
}
