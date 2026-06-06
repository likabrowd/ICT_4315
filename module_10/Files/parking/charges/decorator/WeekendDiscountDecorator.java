package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/*
 Concrete Decorator: applies a 15% weekend discount (Saturday and Sunday).
 
 When getParkingCharge() is called:
 1. Delegates to the wrapped component to get the current charge.
 2. If the entry time falls on a Saturday or Sunday, multiplies by 0.85.
 3. Returns the (possibly reduced) Money amount.
 
 Decorator pattern role: Concrete Decorator
 */

public class WeekendDiscountDecorator extends ParkingChargeCalculatorDecorator {

    private static final double WEEKEND_DISCOUNT = 0.85; //15% off

    public WeekendDiscountDecorator(ParkingChargeCalculator component) {
        super(component);
    }

    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        Money charge = component.getParkingCharge(entryTime, lot, permit);

        if (entryTime != null) {
            DayOfWeek day = ZonedDateTime.ofInstant(entryTime, ZoneId.systemDefault()).getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                return Money.ofDollars(charge.getDollars() * WEEKEND_DISCOUNT);
            }
        }
        return charge;
    }
}
