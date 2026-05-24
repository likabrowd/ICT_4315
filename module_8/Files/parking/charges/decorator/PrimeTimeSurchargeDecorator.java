package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/*
 Concrete Decorator: applies a 50% surcharge during prime-time commuter hours.
 
 Prime-time windows: 7 am–9 am  and  4 pm–6 pm.
 
 When getParkingCharge() is called:
 1. Delegates to the wrapped component to get the current charge.
 2. If the entry time falls in a prime-time window, multiplies by 1.50.
 3. Returns the (possibly increased) Money amount.
 
 Decorator pattern role: Concrete Decorator
 */

public class PrimeTimeSurchargeDecorator extends ParkingChargeCalculatorDecorator {

    private static final double PRIME_TIME_MULTIPLIER = 1.50; //+50%

    public PrimeTimeSurchargeDecorator(ParkingChargeCalculator component) {
        super(component);
    }

    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        Money charge = component.getParkingCharge(entryTime, lot, permit);

        if (entryTime != null) {
            int hour = ZonedDateTime.ofInstant(entryTime, ZoneId.systemDefault()).getHour();
            if ((hour >= 7 && hour < 9) || (hour >= 16 && hour < 18)) {
                return Money.ofDollars(charge.getDollars() * PRIME_TIME_MULTIPLIER);
            }
        }
        return charge;
    }
}
