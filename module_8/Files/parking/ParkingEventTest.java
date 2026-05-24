package parking;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingEvent.

 Tests cover:
 1. Constructor stores all fields correctly.
 2. EventType enum distinguishes ENTER from EXIT.
 3. toString() is non-null and contains key info.
 */

public class ParkingEventTest {

    private ParkingPermit buildPermit(CarType type, String license) {
        Customer c = new Customer("Test",
                new Address("15 St", "Denver", "CO", "80200"), "000-000-0000");
        Car car = new Car(license, type, c);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        return permit;
    }

    @Test
    public void constructorStoresAllFields() {
        ParkingLot lot = new ParkingLot("L1", "Lot 1", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        ParkingPermit permit = buildPermit(CarType.SEDAN, "TST-001");
        Instant now = Instant.now();

        ParkingEvent event = new ParkingEvent(lot, permit, now, ParkingEvent.EventType.ENTER);

        assertEquals(lot,                          event.getLot());
        assertEquals(permit,                       event.getPermit());
        assertEquals(now,                          event.getTimestamp());
        assertEquals(ParkingEvent.EventType.ENTER, event.getEventType());
    }

    @Test
    public void eventTypeEnterNotEqualToExit() {
        assertNotEquals(ParkingEvent.EventType.ENTER, ParkingEvent.EventType.EXIT);
    }

    @Test
    public void toStringIsNonNull() {
        ParkingLot lot = new ParkingLot("L2", "Lot 2", ScanType.ENTRY_EXIT,
                Money.ofDollars(5), Money.ofDollars(15));
        ParkingPermit permit = buildPermit(CarType.COMPACT, "TST-002");
        ParkingEvent event = new ParkingEvent(lot, permit, Instant.now(),
                ParkingEvent.EventType.EXIT);
        assertNotNull(event.toString());
        assertTrue(event.toString().contains("EXIT"));
    }
}
