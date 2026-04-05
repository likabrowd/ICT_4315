package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;

//Tests transaction creation and fee aggregation. 

public class TransactionManagerTest {

    @Test
    public void testParkAndSumByPermit() {
        TransactionManager tm = new TransactionManager();
        ParkingLot lot = new ParkingLot("A1", "Lot A", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        Customer customer = new Customer("Mary", new Address("123 A St", "Denver", "CO", "80208"), "303-111-2222");
        Car car = new Car("KAL-123", CarType.SEDAN, customer);
        ParkingPermit permit = new ParkingPermit();
        car.setPermit(permit);

        tm.park(Instant.now(), permit, lot);

        assertEquals(10.0, tm.getParkingCharges(permit).getDollars(), 0.0001);
    }
}
