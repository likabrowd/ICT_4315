package parking;

import org.junit.jupiter.api.Test;
import parking.charges.factory.ParkingChargeStrategyFactory;
import parking.charges.strategy.TimeOfDayAndSpecialDayStrategy;
import parking.charges.strategy.VehicleTypeAndDayOfWeekStrategy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingLot.
 
 Tests cover:
 1. Factory wires the correct strategy based on ScanType
 2. getParkingCharge() delegates to the strategy
 3. Strategy can be swapped at runtime
 4. Equality and toString behave correctly
 */

public class ParkingLotTest {

    private static final Instant MONDAY_NOON = LocalDate.of(2025, 4, 7)
            .atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant();

    //Helper: build a permit with a known CarType.
    private ParkingPermit permitFor(CarType type) {
        Address addr   = new Address("1 Test St", "Denver", "CO", "80200");
        Customer owner = new Customer("Tester", addr, "000-000-0000");
        Car car        = new Car("TST-001", type, owner);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        return permit;
    }

    //Factory wiring 

    @Test
    public void entryOnlyLotGetsVehicleTypeStrategy() {
        ParkingLot lot = new ParkingLot("L1", "Lot 1", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        assertInstanceOf(VehicleTypeAndDayOfWeekStrategy.class, lot.getStrategy(),
                "ENTRY_ONLY lot should have VehicleTypeAndDayOfWeekStrategy");
    }

    @Test
    public void entryExitLotGetsTimeOfDayStrategy() {
        ParkingLot lot = new ParkingLot("L2", "Lot 2", ScanType.ENTRY_EXIT,
                Money.ofDollars(5), Money.ofDollars(20));
        assertInstanceOf(TimeOfDayAndSpecialDayStrategy.class, lot.getStrategy(),
                "ENTRY_EXIT lot should have TimeOfDayAndSpecialDayStrategy");
    }

    //Charge delegation 

    @Test
    public void entryOnlyLotChargesDelegatedToStrategy() {
        ParkingLot lot = new ParkingLot("L3", "Lot 3", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        ParkingPermit permit = permitFor(CarType.SEDAN);

        //SEDAN weekday noon: no discount → $10.00
        Money charge = lot.getParkingCharge(MONDAY_NOON, permit);
        assertEquals(Money.ofDollars(10.00), charge);
    }

    @Test
    public void entryOnlyLotCompactGets20PercentDiscount() {
        ParkingLot lot = new ParkingLot("L4", "Lot 4", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        ParkingPermit permit = permitFor(CarType.COMPACT);
        //COMPACT weekday: $10 × 0.80 = $8.00
        Money charge = lot.getParkingCharge(MONDAY_NOON, permit);
        assertEquals(Money.ofDollars(8.00), charge);
    }

    //Runtime strategy swap 

    @Test
    public void strategyCanBeSwappedAtRuntime() {
        ParkingLot lot = new ParkingLot("L5", "Lot 5", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        assertInstanceOf(VehicleTypeAndDayOfWeekStrategy.class, lot.getStrategy());

        lot.setStrategy(new TimeOfDayAndSpecialDayStrategy());
        assertInstanceOf(TimeOfDayAndSpecialDayStrategy.class, lot.getStrategy(),
                "After setStrategy(), lot should use the new strategy");
    }

    //Equality and toString

    @Test
    public void testEqualityByLotId() {
        ParkingLot l1 = new ParkingLot("L1", "Main Lot", ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0), Money.ofDollars(12.0));
        ParkingLot l2 = new ParkingLot("L1", "Main Lot", ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0), Money.ofDollars(12.0));
        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    public void testToStringNotNull() {
        ParkingLot lot = new ParkingLot("L2", "Overflow", ScanType.ENTRY_ONLY,
                Money.ofDollars(1.5), Money.ofDollars(10.0));
        assertNotNull(lot.toString());
    }
}
