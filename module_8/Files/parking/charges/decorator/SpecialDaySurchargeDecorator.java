package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/*
 Concrete Decorator: applies a 75% surcharge on designated special days
 (i.e., graduation, homecoming, major campus events).
 
 When getParkingCharge() is called:
 1. Delegates to the wrapped component to get the current charge.
 2. If the entry date matches a registered special day, multiplies by 1.75.
 3. Returns the (possibly increased) Money amount.
 
 Special days can be registered at construction time or added later via addSpecialDay().
 
 Decorator pattern role: Concrete Decorator
 */

public class SpecialDaySurchargeDecorator extends ParkingChargeCalculatorDecorator {

    private static final double SPECIAL_DAY_MULTIPLIER = 1.75; // +75%

    private final Set<LocalDate> specialDays;

    public SpecialDaySurchargeDecorator(ParkingChargeCalculator component, Set<LocalDate> specialDays) {
        super(component);
        this.specialDays = (specialDays != null) ? new HashSet<>(specialDays) : new HashSet<>();
    }

    public SpecialDaySurchargeDecorator(ParkingChargeCalculator component) {
        this(component, new HashSet<>());
    }

    //Register an additional special date at runtime.
    public void addSpecialDay(LocalDate date) {
        if (date != null) specialDays.add(date);
    }

    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        Money charge = component.getParkingCharge(entryTime, lot, permit);

        if (entryTime != null) {
            LocalDate date = ZonedDateTime.ofInstant(entryTime, ZoneId.systemDefault()).toLocalDate();
            if (specialDays.contains(date)) {
                return Money.ofDollars(charge.getDollars() * SPECIAL_DAY_MULTIPLIER);
            }
        }
        return charge;
    }
}
