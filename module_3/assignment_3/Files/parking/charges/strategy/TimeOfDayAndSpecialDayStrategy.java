package parking.charges.strategy;

import parking.Money;
import parking.ParkingPermit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 Strategy 2: Time of Day + Special Days
 
 Rules:
 - PRIME TIME (7 am – 9 am  and  4 pm – 6 pm): 50% surcharge applied to the base rate. These are high-demand commuter windows and should discourage casual use of busy lots.
 - OVERNIGHT (midnight – 6 am): flat 10% surcharge to cover the overnight-check penalty described in the assignment (traffic department visits between midnight and 6 am).
 - SPECIAL DAYS (e.g. graduation): 75% surcharge.  Special dates are registered at construction time so they can be configured per-lot without changing this class.
  Surcharges are NOT stacked — the highest applicable surcharge wins.
 */

public class TimeOfDayAndSpecialDayStrategy implements ParkingChargeStrategy {

    private static final double PRIME_TIME_SURCHARGE   = 1.50; // +50%
    private static final double OVERNIGHT_SURCHARGE    = 1.10; // +10%
    private static final double SPECIAL_DAY_SURCHARGE  = 1.75; // +75%

    private final Set<LocalDate> specialDays;

    //Construct with an explicit set of special dates.
    public TimeOfDayAndSpecialDayStrategy(Set<LocalDate> specialDays) {
        this.specialDays = (specialDays != null) ? new HashSet<>(specialDays) : new HashSet<>();
    }

    //Convenience constructor — no special days pre-loaded.
    public TimeOfDayAndSpecialDayStrategy() {
        this(new HashSet<>());
    }

    //Allow the lot manager to register an additional special date at runtime.
    public void addSpecialDay(LocalDate date) {
        if (date != null) specialDays.add(date);
    }

    @Override
    public Money calculateCharge(Money baseRate, Instant entryTime, ParkingPermit permit) {
        if (baseRate == null || entryTime == null) {
            return Money.ofDollars(0);
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(entryTime, ZoneId.systemDefault());
        int hour          = zdt.getHour();
        LocalDate date    = zdt.toLocalDate();

        double multiplier = 1.0; //default: no surcharge

        //Factor 1: special day — highest priority, checked first
        if (specialDays.contains(date)) {
            multiplier = SPECIAL_DAY_SURCHARGE;

        //Factor 2: prime-time commuter windows
        } else if (isPrimeTime(hour)) {
            multiplier = PRIME_TIME_SURCHARGE;

        //Factor 3: overnight penalty window (midnight to 6 am)
        } else if (hour < 6) {
            multiplier = OVERNIGHT_SURCHARGE;
        }

        return Money.ofDollars(baseRate.getDollars() * multiplier);
    }

    //Returns true during the two daily prime-time windows.
    private boolean isPrimeTime(int hour) {
        return (hour >= 7 && hour < 9) || (hour >= 16 && hour < 18);
    }
}
