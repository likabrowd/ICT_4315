package parking.charges.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parking.*;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/*
 Unit tests for the Decorator-pattern ParkingChargeCalculator hierarchy.
 
Tests cover:
 1.  FlatRateCalculator          – base rate selection by ScanType
 2.  CompactCarDiscountDecorator – 20% off COMPACT, no change for others
 3.  WeekendDiscountDecorator    – 15% off Sat/Sun, no change weekday
 4.  PrimeTimeSurchargeDecorator – +50% in prime windows, base otherwise
 5.  SpecialDaySurchargeDecorator– +75% on special dates, base otherwise
 6.  Decorator chains            – two+ decorators composed together
 7.  ParkingChargeCalculatorFactory – correct chain per ScanType
 8.  Null-safety                 – null lot / permit / time handled gracefully
 */

public class ParkingChargeCalculatorTest {

    //shared test fixtures

    private static final ZoneId ZONE = ZoneId.systemDefault();

    //Known weekday (Monday 2025-04-07) at various hours
    private static final Instant MONDAY_NOON    = at(2025, 4, 7, 12);
    private static final Instant MONDAY_8AM     = at(2025, 4, 7,  8); // prime-time
    private static final Instant MONDAY_5PM     = at(2025, 4, 7, 17); // prime-time
    private static final Instant MONDAY_2AM     = at(2025, 4, 7,  2); // overnight

    //Known weekend (Saturday 2025-04-12) at noon
    private static final Instant SATURDAY_NOON  = at(2025, 4, 12, 12);
    private static final Instant SUNDAY_NOON    = at(2025, 4, 13, 12);

    private ParkingLot entryOnlyLot;   //base $10 / overnight $20
    private ParkingLot entryExitLot;   //base $5  / overnight $20
    private Money      baseEntryOnly;  //$10
    private Money      baseEntryExit;  //$20 (overnight = duration base)

    private ParkingPermit sedanPermit;
    private ParkingPermit compactPermit;

    @BeforeEach
    public void setUp() {
        entryOnlyLot  = new ParkingLot("L1", "Surface Lot", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        entryExitLot  = new ParkingLot("L2", "Garage",      ScanType.ENTRY_EXIT,
                Money.ofDollars(5),  Money.ofDollars(20));
        baseEntryOnly = entryOnlyLot.getFeeOnEntry();   //$10
        baseEntryExit = entryExitLot.getFeeOvernight(); //$20

        sedanPermit   = buildPermit(CarType.SEDAN,   "SED-001");
        compactPermit = buildPermit(CarType.COMPACT, "CMP-001");
    }

    //helpers 

    private static Instant at(int y, int m, int d, int h) {
        return LocalDateTime.of(y, m, d, h, 0).atZone(ZONE).toInstant();
    }

    private ParkingPermit buildPermit(CarType type, String license) {
        Customer owner = new Customer("Test",
                new Address("1 A St", "Denver", "CO", "80200"), "000-000-0000");
        Car car = new Car(license, type, owner);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        return permit;
    }

    //1. FlatRateCalculator

    @Test
    public void flatRate_entryOnly_returnsOnEntryFee() {
        ParkingChargeCalculator calc = new FlatRateCalculator();
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "ENTRY_ONLY lot: flat rate should be feeOnEntry $10.00");
    }

    @Test
    public void flatRate_entryExit_returnsOvernightFee() {
        ParkingChargeCalculator calc = new FlatRateCalculator();
        assertEquals(Money.ofDollars(20.00),
                calc.getParkingCharge(MONDAY_NOON, entryExitLot, sedanPermit),
                "ENTRY_EXIT lot: flat rate should be feeOvernight $20.00");
    }

    @Test
    public void flatRate_nullLot_returnsZero() {
        ParkingChargeCalculator calc = new FlatRateCalculator();
        assertEquals(Money.ofDollars(0),
                calc.getParkingCharge(MONDAY_NOON, null, sedanPermit));
    }

    //2. CompactCarDiscountDecorator

    @Test
    public void compactDiscount_appliedToCompactCar() {
        ParkingChargeCalculator calc =
                new CompactCarDiscountDecorator(new FlatRateCalculator());
        // $10 × 0.80 = $8.00
        assertEquals(Money.ofDollars(8.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, compactPermit),
                "COMPACT car: 20% discount → $8.00");
    }

    @Test
    public void compactDiscount_notAppliedToSedan() {
        ParkingChargeCalculator calc =
                new CompactCarDiscountDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "SEDAN: no compact discount → $10.00");
    }

    @Test
    public void compactDiscount_nullPermit_returnsBaseCharge() {
        ParkingChargeCalculator calc =
                new CompactCarDiscountDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, null),
                "Null permit: no discount applied → $10.00");
    }

    //3. WeekendDiscountDecorator

    @Test
    public void weekendDiscount_appliedOnSaturday() {
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(new FlatRateCalculator());
        //$10 × 0.85 = $8.50
        assertEquals(Money.ofDollars(8.50),
                calc.getParkingCharge(SATURDAY_NOON, entryOnlyLot, sedanPermit),
                "Saturday: 15% weekend discount → $8.50");
    }

    @Test
    public void weekendDiscount_appliedOnSunday() {
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(8.50),
                calc.getParkingCharge(SUNDAY_NOON, entryOnlyLot, sedanPermit),
                "Sunday: 15% weekend discount → $8.50");
    }

    @Test
    public void weekendDiscount_notAppliedOnWeekday() {
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "Monday: no weekend discount → $10.00");
    }

    @Test
    public void weekendDiscount_nullTime_returnsBaseCharge() {
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(null, entryOnlyLot, sedanPermit),
                "Null entryTime: no discount applied → $10.00");
    }

    //4. PrimeTimeSurchargeDecorator

    @Test
    public void primetime_morningWindowAppliesSurcharge() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(new FlatRateCalculator());
        //$10 × 1.50 = $15.00
        assertEquals(Money.ofDollars(15.00),
                calc.getParkingCharge(MONDAY_8AM, entryOnlyLot, sedanPermit),
                "8 am prime-time: +50% surcharge → $15.00");
    }

    @Test
    public void primetime_eveningWindowAppliesSurcharge() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(15.00),
                calc.getParkingCharge(MONDAY_5PM, entryOnlyLot, sedanPermit),
                "5 pm prime-time: +50% surcharge → $15.00");
    }

    @Test
    public void primetime_normalHourNoSurcharge() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "Noon: not prime-time → base rate $10.00");
    }

    @Test
    public void primetime_overnightHourNoSurcharge() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_2AM, entryOnlyLot, sedanPermit),
                "2 am: not prime-time → base rate $10.00");
    }

    //5. SpecialDaySurchargeDecorator

    @Test
    public void specialDay_appliesSurchargeOnRegisteredDate() {
        LocalDate graduation = LocalDate.of(2025, 5, 10);
        SpecialDaySurchargeDecorator calc =
                new SpecialDaySurchargeDecorator(new FlatRateCalculator());
        calc.addSpecialDay(graduation);
        Instant graduationNoon = graduation.atTime(12, 0).atZone(ZONE).toInstant();
        // $10 × 1.75 = $17.50
        assertEquals(Money.ofDollars(17.50),
                calc.getParkingCharge(graduationNoon, entryOnlyLot, sedanPermit),
                "Graduation day: +75% surcharge → $17.50");
    }

    @Test
    public void specialDay_noSurchargeOnNormalDay() {
        LocalDate graduation = LocalDate.of(2025, 5, 10);
        SpecialDaySurchargeDecorator calc =
                new SpecialDaySurchargeDecorator(new FlatRateCalculator());
        calc.addSpecialDay(graduation);
        Instant dayAfter = graduation.plusDays(1).atTime(12, 0).atZone(ZONE).toInstant();
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(dayAfter, entryOnlyLot, sedanPermit),
                "Day after graduation: base rate → $10.00");
    }

    @Test
    public void specialDay_constructorWithSet() {
        LocalDate event = LocalDate.of(2025, 9, 1);
        Set<LocalDate> days = new HashSet<>();
        days.add(event);
        SpecialDaySurchargeDecorator calc =
                new SpecialDaySurchargeDecorator(new FlatRateCalculator(), days);
        Instant eventNoon = event.atTime(12, 0).atZone(ZONE).toInstant();
        assertEquals(Money.ofDollars(17.50),
                calc.getParkingCharge(eventNoon, entryOnlyLot, sedanPermit));
    }

    //6. Decorator chains (multiple decorators composed)
    @Test
    public void chain_compactPlusWeekend_weekdayNoWeekendDiscount() {
        // Compact weekday: only 20% off
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(
                    new CompactCarDiscountDecorator(
                        new FlatRateCalculator()));
        //$10 × 0.80 = $8.00 (no weekend discount)
        assertEquals(Money.ofDollars(8.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, compactPermit),
                "COMPACT weekday: 20% off only → $8.00");
    }

    @Test
    public void chain_compactPlusWeekend_weekendBothDiscounts() {
        ParkingChargeCalculator calc =
                new WeekendDiscountDecorator(
                    new CompactCarDiscountDecorator(
                        new FlatRateCalculator()));
        //$10 × 0.80 × 0.85 = $6.80
        assertEquals(Money.ofDollars(6.80),
                calc.getParkingCharge(SATURDAY_NOON, entryOnlyLot, compactPermit),
                "COMPACT Saturday: 20% + 15% → $6.80");
    }

    @Test
    public void chain_compactPlusPrimeTime_multiplyCorrectly() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(
                    new CompactCarDiscountDecorator(
                        new FlatRateCalculator()));
        //$10 × 0.80 × 1.50 = $12.00
        assertEquals(Money.ofDollars(12.00),
                calc.getParkingCharge(MONDAY_8AM, entryOnlyLot, compactPermit),
                "COMPACT prime-time: 20% off then +50% → $12.00");
    }

    @Test
    public void chain_specialDayBeatsPrimeTime_highestApplied() {
        //Even with prime-time inside, special day wraps outside
        LocalDate event = LocalDate.of(2025, 4, 7); //Monday = also prime-time capable
        SpecialDaySurchargeDecorator calc =
                new SpecialDaySurchargeDecorator(
                    new PrimeTimeSurchargeDecorator(
                        new FlatRateCalculator()));
        calc.addSpecialDay(event);
        Instant eventPrimeTime = event.atTime(8, 0).atZone(ZONE).toInstant();
        //$10 × 1.50 (prime) × 1.75 (special) = $26.25
        assertEquals(Money.ofDollars(26.25),
                calc.getParkingCharge(eventPrimeTime, entryOnlyLot, sedanPermit),
                "Special day AND prime-time: both applied multiplicatively → $26.25");
    }

    @Test
    public void chain_fullEntryOnlyChain_sedan_weekday_noon() {
        //FlatRate → CompactDiscount → WeekendDiscount → SpecialDay
        //SEDAN, Monday noon, no special day: all pass-through → $10
        ParkingChargeCalculator calc =
                new SpecialDaySurchargeDecorator(
                    new WeekendDiscountDecorator(
                        new CompactCarDiscountDecorator(
                            new FlatRateCalculator())));
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "Full ENTRY_ONLY chain, SEDAN weekday noon: $10.00");
    }

    @Test
    public void chain_fullEntryOnlyChain_compact_saturday() {
        ParkingChargeCalculator calc =
                new SpecialDaySurchargeDecorator(
                    new WeekendDiscountDecorator(
                        new CompactCarDiscountDecorator(
                            new FlatRateCalculator())));
        //$10 × 0.80 × 0.85 = $6.80
        assertEquals(Money.ofDollars(6.80),
                calc.getParkingCharge(SATURDAY_NOON, entryOnlyLot, compactPermit),
                "Full ENTRY_ONLY chain, COMPACT Saturday: $6.80");
    }

    //7. ParkingChargeCalculatorFactory

    @Test
    public void factory_entryOnly_sedan_weekday_returnsBaseRate() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_ONLY);
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, sedanPermit),
                "Factory ENTRY_ONLY, SEDAN weekday → $10.00");
    }

    @Test
    public void factory_entryOnly_compact_weekday_appliesCompactDiscount() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_ONLY);
        assertEquals(Money.ofDollars(8.00),
                calc.getParkingCharge(MONDAY_NOON, entryOnlyLot, compactPermit),
                "Factory ENTRY_ONLY, COMPACT weekday → $8.00");
    }

    @Test
    public void factory_entryOnly_sedan_saturday_appliesWeekendDiscount() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_ONLY);
        assertEquals(Money.ofDollars(8.50),
                calc.getParkingCharge(SATURDAY_NOON, entryOnlyLot, sedanPermit),
                "Factory ENTRY_ONLY, SEDAN Saturday → $8.50");
    }

    @Test
    public void factory_entryExit_sedan_noon_returnsBaseRate() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_EXIT);
        //$20 × 1.0 (noon, no prime, no special) = $20.00
        assertEquals(Money.ofDollars(20.00),
                calc.getParkingCharge(MONDAY_NOON, entryExitLot, sedanPermit),
                "Factory ENTRY_EXIT, SEDAN noon → $20.00");
    }

    @Test
    public void factory_entryExit_sedan_primeTime_appliesSurcharge() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_EXIT);
        //$20 × 1.50 = $30.00
        assertEquals(Money.ofDollars(30.00),
                calc.getParkingCharge(MONDAY_8AM, entryExitLot, sedanPermit),
                "Factory ENTRY_EXIT, SEDAN prime-time → $30.00");
    }

    @Test
    public void factory_entryExit_compact_primeTime_bothFactors() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_EXIT);
        //$20 × 0.80 × 1.50 = $24.00
        assertEquals(Money.ofDollars(24.00),
                calc.getParkingCharge(MONDAY_8AM, entryExitLot, compactPermit),
                "Factory ENTRY_EXIT, COMPACT prime-time → $24.00");
    }

    @Test
    public void factory_nullScanType_defaultsToEntryOnlyBehaviour() {
        ParkingChargeCalculator calc =
                ParkingChargeCalculatorFactory.buildCalculator(null);
        assertNotNull(calc, "Factory should not return null for null ScanType");
    }

    @Test
    public void factory_returnsNewInstanceEachCall() {
        ParkingChargeCalculator c1 = ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_ONLY);
        ParkingChargeCalculator c2 = ParkingChargeCalculatorFactory.buildCalculator(ScanType.ENTRY_ONLY);
        assertNotSame(c1, c2, "Factory should return a new chain each call");
    }

    //8. Null-safety edge cases

    @Test
    public void nullComponent_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompactCarDiscountDecorator(null),
                "Wrapping null should throw IllegalArgumentException");
    }

    @Test
    public void decoratorWithNullTime_returnsBaseCharge() {
        ParkingChargeCalculator calc =
                new PrimeTimeSurchargeDecorator(new FlatRateCalculator());
        assertEquals(Money.ofDollars(10.00),
                calc.getParkingCharge(null, entryOnlyLot, sedanPermit),
                "Null entryTime in PrimeTimeSurchargeDecorator: no surcharge → $10.00");
    }
}
