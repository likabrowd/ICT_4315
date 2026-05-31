package parking.charges.decorator;

import parking.CarType;
import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.Instant;

/*
 Concrete Decorator: applies a 20% discount for COMPACT vehicles.
 
 When getParkingCharge() is called:
 1. Delegates to the wrapped component to get the current charge.
 2. If the permit's car is of type COMPACT, multiplies the charge by 0.80.
 3. Returns the (possibly reduced) Money amount.
 
 This mirrors the original Strategy rule: "compact cars get 20% off".

 Decorator pattern role: Concrete Decorator
 */

public class CompactCarDiscountDecorator extends ParkingChargeCalculatorDecorator {

    private static final double COMPACT_DISCOUNT = 0.80; //20% off

    public CompactCarDiscountDecorator(ParkingChargeCalculator component) {
        super(component);
    }

    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        //Step 1: get the charge from the wrapped calculator
        Money charge = component.getParkingCharge(entryTime, lot, permit);

        //Step 2: apply compact discount if applicable
        if (permit != null
                && permit.getCar() != null
                && permit.getCar().getType() == CarType.COMPACT) {
            return Money.ofDollars(charge.getDollars() * COMPACT_DISCOUNT);
        }
        return charge;
    }
}
