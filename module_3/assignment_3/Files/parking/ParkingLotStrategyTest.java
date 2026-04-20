package parking;

import org.junit.jupiter.api.Test;
import parking.charges.strategy.TimeOfDayAndSpecialDayStrategy;
import parking.charges.strategy.VehicleTypeAndDayOfWeekStrategy;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 Integration tests: verifies that ParkingLot correctly delegates to its pluggable strategy, and that the strategy can be swapped at runtime.
 */
public class ParkingLotStrategyTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    //Build a ParkingPermit linked to a Car with the given CarType.
    private ParkingPermit permitFor(CarType type) {
        Address addr   = new Address("1 Main St", "Denver", "CO", "80200");
        Customer owner = new Customer("Test", addr, "000-000-0000");
        Car car        = new Car("TST-999", type, owner);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        return permit;
    }

    private Instant instantAt(int year, int month, int day, int hour) {
        return LocalDateTime.of(year, month, day, hour, 0).atZone(ZONE).toInstant();
    }

    @Test
    public void entryOnlyLotUsesVehicleTypeStrategyByDefault() {
        ParkingLot lot = new ParkingLot("L1", "Surface Lot", ScanType.ENTRY_ONLY,
                Money.ofDollars(10.00), Money.ofDollars(20.00));

        assertInstanceOf(VehicleTypeAndDayOfWeekStrategy.class, lot.getStrategy(),
                "ENTRY_ONLY lot should default to VehicleTypeAndDayOfWeekStrategy");
    }

    @Test
    public void entryExitLotUsesTimeOfDayStrategyByDefault() {
        ParkingLot lot = new ParkingLot("L2", "Garage", ScanType.ENTRY_EXIT,
                Money.ofDollars(10.00), Money.ofDollars(20.00));

        assertInstanceOf(TimeOfDayAndSpecialDayStrategy.class, lot.getStrategy(),
                "ENTRY_EXIT lot should default to TimeOfDayAndSpecialDayStrategy");
    }

    @Test
    public void compactCarGetsDiscountViaDefaultStrategy() {
        ParkingLot lot  = new ParkingLot("L1", "Lot A", ScanType.ENTRY_ONLY,
                Money.ofDollars(10.00), Money.ofDollars(20.00));
        ParkingPermit permit = permitFor(CarType.COMPACT);

        //Weekday noon — only the compact discount applies: $10 × 0.80 = $8
        Instant weekdayNoon = LocalDate.of(2025, 4, 7).atTime(12, 0).atZone(ZONE).toInstant();
        Money charge = lot.getParkingCharge(weekdayNoon, permit);
        assertEquals(Money.ofDollars(8.00), charge,
                "COMPACT in entry-only lot on weekday: $8.00");
    }

    @Test
    public void strategyCanBeSwappedAtRuntime() {
        ParkingLot lot = new ParkingLot("L1", "Lot A", ScanType.ENTRY_ONLY,
                Money.ofDollars(10.00), Money.ofDollars(20.00));
        ParkingPermit permit = permitFor(CarType.SUV);

        //Swap to time-of-day strategy
        lot.setStrategy(new TimeOfDayAndSpecialDayStrategy());

        //Prime-time morning — should be $10 × 1.50 = $15 (using ENTRY_ONLY base = $10)
        Instant primeTime = LocalDate.of(2025, 4, 9).atTime(8, 0).atZone(ZONE).toInstant();
        Money charge = lot.getParkingCharge(primeTime, permit);
        assertEquals(Money.ofDollars(15.00), charge,
                "After strategy swap to TimeOfDay: prime-time SUV should be $15.00");
    }

    @Test
    public void garageChargesHigherBaseRateAtPrimeTime() {
        //Garage is ENTRY_EXIT: base rate for strategy is feeOvernight ($20)
        ParkingLot garage = new ParkingLot("G1", "Garage", ScanType.ENTRY_EXIT,
                Money.ofDollars(5.00), Money.ofDollars(20.00));
        ParkingPermit permit = permitFor(CarType.SEDAN);

        Instant primeTime = LocalDate.of(2025, 4, 9).atTime(8, 0).atZone(ZONE).toInstant();
        Money charge = garage.getParkingCharge(primeTime, permit);
        // $20 × 1.50 = $30
        assertEquals(Money.ofDollars(30.00), charge,
                "Garage at prime-time: $20 base × 1.50 surcharge = $30.00");
    }
}
