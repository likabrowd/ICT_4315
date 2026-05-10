package parking.charges.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parking.*;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for TimeOfDayAndSpecialDayStrategy.
 
 Rules under test:
 1. Normal hours (9 am–4 pm)    → base rate × 1.00
 2. Prime-time morning (7–9 am) → base rate × 1.50
 3. Prime-time evening (4–6 pm) → base rate × 1.50
 4. Overnight (midnight–6 am)   → base rate × 1.10
 5. Special day (e.g. graduation) → base rate × 1.75
 6. Special day beats prime-time (highest surcharge wins)
 7. Null inputs return $0.00
 */

public class TimeOfDayAndSpecialDayStrategyTest {

    private TimeOfDayAndSpecialDayStrategy strategy;
    private Money baseRate;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    @BeforeEach
    public void setUp() {
        strategy = new TimeOfDayAndSpecialDayStrategy();
        baseRate = Money.ofDollars(10.00);
    }

    //Convenience: build an Instant for a given date and hour.
    private Instant instantAt(int year, int month, int day, int hour) {
        return LocalDateTime.of(year, month, day, hour, 0)
                            .atZone(ZONE).toInstant();
    }

    //Normal hours

    @Test
    public void normalHoursMidMorningPaysBaseRate() {
        Instant tenAm = instantAt(2025, 4, 9, 10);
        Money charge  = strategy.calculateCharge(baseRate, tenAm, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "10 am on a normal day: expect base rate $10.00");
    }

    @Test
    public void normalHoursAfternoonPaysBaseRate() {
        Instant twoPm = instantAt(2025, 4, 9, 14);
        Money charge  = strategy.calculateCharge(baseRate, twoPm, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "2 pm on a normal day: expect base rate $10.00");
    }

    //Prime-time surcharge

    @Test
    public void morningPrimeTimeAppliesSurcharge() {
        Instant sevenAm = instantAt(2025, 4, 9, 7);
        Money charge    = strategy.calculateCharge(baseRate, sevenAm, null);
        assertEquals(Money.ofDollars(15.00), charge,
                "7 am prime-time: expect $15.00 (50% surcharge)");
    }

    @Test
    public void eightAmIsPrimeTime() {
        Instant eightAm = instantAt(2025, 4, 9, 8);
        Money charge    = strategy.calculateCharge(baseRate, eightAm, null);
        assertEquals(Money.ofDollars(15.00), charge,
                "8 am prime-time: expect $15.00");
    }

    @Test
    public void nineAmIsNotPrimeTime() {
        Instant nineAm = instantAt(2025, 4, 9, 9);
        Money charge   = strategy.calculateCharge(baseRate, nineAm, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "9 am is outside morning prime window: expect base rate");
    }

    @Test
    public void eveningPrimeTimeAppliesSurcharge() {
        Instant fourPm = instantAt(2025, 4, 9, 16);
        Money charge   = strategy.calculateCharge(baseRate, fourPm, null);
        assertEquals(Money.ofDollars(15.00), charge,
                "4 pm prime-time: expect $15.00 (50% surcharge)");
    }

    @Test
    public void fivePmIsPrimeTime() {
        Instant fivePm = instantAt(2025, 4, 9, 17);
        Money charge   = strategy.calculateCharge(baseRate, fivePm, null);
        assertEquals(Money.ofDollars(15.00), charge,
                "5 pm prime-time: expect $15.00");
    }

    @Test
    public void sixPmIsNotPrimeTime() {
        Instant sixPm = instantAt(2025, 4, 9, 18);
        Money charge  = strategy.calculateCharge(baseRate, sixPm, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "6 pm is outside evening prime window: expect base rate");
    }

    //Overnight surcharge

    @Test
    public void midnightAppliesOvernightSurcharge() {
        Instant midnight = instantAt(2025, 4, 9, 0);
        Money charge     = strategy.calculateCharge(baseRate, midnight, null);
        assertEquals(Money.ofDollars(11.00), charge,
                "Midnight: expect $11.00 (10% overnight surcharge)");
    }

    @Test
    public void threeAmAppliesOvernightSurcharge() {
        Instant threeAm = instantAt(2025, 4, 9, 3);
        Money charge    = strategy.calculateCharge(baseRate, threeAm, null);
        assertEquals(Money.ofDollars(11.00), charge,
                "3 am: expect $11.00 (10% overnight surcharge)");
    }

    @Test
    public void sixAmIsNotOvernight() {
        Instant sixAm = instantAt(2025, 4, 9, 6);
        Money charge  = strategy.calculateCharge(baseRate, sixAm, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "6 am is outside overnight window: expect base rate");
    }

    //Special day surcharge

    @Test
    public void specialDayAppliesHighestSurcharge() {
        LocalDate graduation = LocalDate.of(2025, 5, 10);
        strategy.addSpecialDay(graduation);
        Instant graduationNoon = graduation.atTime(12, 0).atZone(ZONE).toInstant();
        Money charge = strategy.calculateCharge(baseRate, graduationNoon, null);
        assertEquals(Money.ofDollars(17.50), charge,
                "Graduation day noon: expect $17.50 (75% surcharge)");
    }

    @Test
    public void specialDayBeatsNormalHours() {
        LocalDate event = LocalDate.of(2025, 6, 1);
        strategy.addSpecialDay(event);
        Instant eventMorning = event.atTime(10, 0).atZone(ZONE).toInstant();
        Money charge = strategy.calculateCharge(baseRate, eventMorning, null);
        assertEquals(Money.ofDollars(17.50), charge,
                "Special day beats normal hours: expect $17.50");
    }

    @Test
    public void specialDayBeatsPrimeTime() {
        LocalDate event = LocalDate.of(2025, 6, 1);
        strategy.addSpecialDay(event);
        Instant eventPrimeTime = event.atTime(8, 0).atZone(ZONE).toInstant();
        Money charge = strategy.calculateCharge(baseRate, eventPrimeTime, null);
        assertEquals(Money.ofDollars(17.50), charge,
                "Special day during prime-time: special day rate ($17.50) wins");
    }

    @Test
    public void nonSpecialDayIsUnaffected() {
        LocalDate event = LocalDate.of(2025, 5, 10);
        strategy.addSpecialDay(event);
        Instant normalNoon = LocalDate.of(2025, 5, 11).atTime(12, 0).atZone(ZONE).toInstant();
        Money charge = strategy.calculateCharge(baseRate, normalNoon, null);
        assertEquals(Money.ofDollars(10.00), charge,
                "Day after special day: expect base rate $10.00");
    }

    //Null safety

    @Test
    public void nullBaseRateReturnsZero() {
        Instant now = Instant.now();
        Money charge = strategy.calculateCharge(null, now, null);
        assertEquals(Money.ofDollars(0), charge);
    }

    @Test
    public void nullEntryTimeReturnsZero() {
        Money charge = strategy.calculateCharge(baseRate, null, null);
        assertEquals(Money.ofDollars(0), charge);
    }
}
