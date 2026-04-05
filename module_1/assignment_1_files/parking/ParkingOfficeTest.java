package parking;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

// Makes sure that ParkingOffice correctly registers customers + cars and properly retrieves charges. 

public class ParkingOfficeTest {

    @Test
    public void testCustomerAndPermitIdAccessors() {
        Address addr = new Address("2199 S. University Blvd.", "Denver", "CO", "80208");
        ParkingOffice office = new ParkingOffice("DU Parking", addr);

        Customer jane = new Customer("Jane", new Address("143 Ash St", "Denver", "CO", "80202"), "303-004-0000");
        Customer teddy = new Customer("Teddy", new Address("222 Bush St", "Denver", "CO", "80203"), "303-123-1111");

        office.addCustomer(jane);
        office.addCustomer(teddy);

        Car car1 = new Car("ABC-111", CarType.COMPACT, jane);
        Car car2 = new Car("XYZ-222", CarType.SUV, teddy);

        ParkingPermit p1 = new ParkingPermit();
        ParkingPermit p2 = new ParkingPermit();

        car1.setPermit(p1);
        car2.setPermit(p2);

        jane.addCar(car1);
        teddy.addCar(car2);

        office.addCar(car1);
        office.addCar(car2);

        Collection<String> customerIds = office.getCustomerIds();
        assertTrue(customerIds.contains(jane.getCustomerId()));
        assertTrue(customerIds.contains(teddy.getCustomerId()));

        Collection<String> permitIds = office.getPermitIds();
        assertTrue(permitIds.contains(p1.getPermitId()));
        assertTrue(permitIds.contains(p2.getPermitId()));

        Collection<String> janePermits = office.getPermitIds(jane);
        assertEquals(1, janePermits.size());
        assertTrue(janePermits.contains(p1.getPermitId()));
    }

    @Test
    public void testParkCreatesTransaction() {
        ParkingOffice office = new ParkingOffice("DU Parking",
                new Address("2199 S. University Blvd.", "Denver", "CO", "80208"));

        Customer jane = new Customer("Jane",
                new Address("147 Cherry St", "Denver", "CO", "80202"), "303-000-0000");
        office.register(jane);

        Car car = new Car("ALC-006", CarType.SEDAN, jane);
        ParkingPermit permit = office.register(car);

        ParkingLot lot = new ParkingLot("LOT-A", "Lot A", ScanType.ENTRY_ONLY,
                Money.ofDollars(5), Money.ofDollars(20));
        office.addLot(lot);

        ParkingTransaction tx = office.park(new Date(), permit, lot);

        assertNotNull(tx);
        assertEquals(permit, tx.getPermit());
        assertEquals(lot, tx.getParkingLot());
    }
}
