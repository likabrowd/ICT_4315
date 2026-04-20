package parking.charges.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import parking.*;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for VehicleTypeAndDayOfWeekStrategy.
 
 Rules under test:
  1. COMPACT car on a weekday  → 20% discount  (×0.80)
  2. SUV car on a weekday      → no discount   (×1.00)
  3. Any car on a weekend      → 15% discount  (×0.85)
  4. COMPACT car on a weekend  → 20% + 15%     (×0.68)
  5. Null inputs return $0.00
 */
public class VehicleTypeAndDayOfWeekStrategyTest {

    private VehicleTypeAndDayOfWeekStrategy strategy;
    private Money baseRate;

    //A Monday at noon (weekday) in the system's default timezone
    private Instant weekdayNoon;
    //A Saturday at noon (weekend)
    private Instant weekendNoon;

    @BeforeEach
    public void setUp() {
        strategy  = new VehicleTypeAndDayOfWeekStrategy();
        baseRate  = Money.ofDollars(10.00);

        ZoneId zone   = ZoneId.systemDefault();
        //Find the next Monday to ensure test is timezone-safe
        LocalDate monday   = LocalDate.of(2025, 4, 7);   // a known Monday
        LocalDate saturday = LocalDate.of(2025, 4, 12);  // a known Saturday
        weekdayNoon = monday.atTime(12, 0).atZone(zone).toInstant();
        weekendNoon = saturday.atTime(12, 0).atZone(zone).toInstant();
    }

    //Helper to build a permit with a known CarType 

    private ParkingPermit permitFor(CarType type) {
        Address addr    = new Address("1 Test St", "Denver", "CO", "80200");
        Customer owner  = new Customer("Test Owner", addr, "000-000-0000");
        Car car         = new Car("TST-001", type, owner);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        return permit;
    }

    //Tests 

    @Test
    public void compactOnWeekdayGets20PercentDiscount() {
        ParkingPermit permit = permitFor(CarType.COMPACT);
        Money charge = strategy.calculateCharge(baseRate, weekdayNoon, permit);
        assertEquals(Money.ofDollars(8.00), charge,
                "COMPACT weekday: expected $8.00 (20% off $10.00)");
    }

    @Test
    public void suvOnWeekdayPaysFullRate() {
        ParkingPermit permit = permitFor(CarType.SUV);
        Money charge = strategy.calculateCharge(baseRate, weekdayNoon, permit);
        assertEquals(Money.ofDollars(10.00), charge,
                "SUV weekday: expected full rate $10.00");
    }

    @Test
    public void sedanOnWeekdayPaysFullRate() {
        ParkingPermit permit = permitFor(CarType.SEDAN);
        Money charge = strategy.calculateCharge(baseRate, weekdayNoon, permit);
        assertEquals(Money.ofDollars(10.00), charge,
                "SEDAN weekday: expected full rate $10.00");
    }

    @Test
    public void suvOnWeekendGets15PercentDiscount() {
        ParkingPermit permit = permitFor(CarType.SUV);
        Money charge = strategy.calculateCharge(baseRate, weekendNoon, permit);
        // 10.00 × 0.85 = 8.50
        assertEquals(Money.ofDollars(8.50), charge,
                "SUV weekend: expected $8.50 (15% off $10.00)");
    }

    @Test
    public void compactOnWeekendGetsBothDiscounts() {
        ParkingPermit permit = permitFor(CarType.COMPACT);
        Money charge = strategy.calculateCharge(baseRate, weekendNoon, permit);
        // 10.00 × 0.80 × 0.85 = 6.80
        assertEquals(Money.ofDollars(6.80), charge,
                "COMPACT weekend: expected $6.80 (20% + 15% off $10.00)");
    }

    @Test
    public void nullBaseRateReturnsZero() {
        ParkingPermit permit = permitFor(CarType.COMPACT);
        Money charge = strategy.calculateCharge(null, weekdayNoon, permit);
        assertEquals(Money.ofDollars(0), charge);
    }

    @Test
    public void nullEntryTimeReturnsZero() {
        ParkingPermit permit = permitFor(CarType.COMPACT);
        Money charge = strategy.calculateCharge(baseRate, null, permit);
        assertEquals(Money.ofDollars(0), charge);
    }

    @Test
    public void nullPermitReturnsZero() {
        Money charge = strategy.calculateCharge(baseRate, weekdayNoon, null);
        assertEquals(Money.ofDollars(0), charge);
    }
}
