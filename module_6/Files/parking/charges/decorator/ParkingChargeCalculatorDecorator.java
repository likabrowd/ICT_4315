package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.Instant;

/*
 Abstract Decorator in the Decorator pattern.

 ParkingChargeCalculatorDecorator extends ParkingChargeCalculator (so it IS-A calculator) and also holds a reference to another ParkingChargeCalculator
 (so it HAS-A calculator to delegate to).
 
 This dual relationship is the heart of the Decorator pattern:
- IS-A  → decorators and concrete calculators are interchangeable.
 - HAS-A → a decorator can wrap any other calculator, building a chain.
 
 Concrete decorators extend this class and override getParkingCharge() to:
 1. Call component.getParkingCharge() to get the base amount.
 2. Apply their own pricing factor to that amount.
 3. Return the modified Money result.
 
 Decorator pattern role: Abstract Decorator
 */

public abstract class ParkingChargeCalculatorDecorator extends ParkingChargeCalculator {

    //The wrapped calculator — could be a FlatRateCalculator or another decorator.
    protected final ParkingChargeCalculator component;

    /**
     * @param component the calculator this decorator wraps; must not be null
     */
    
    protected ParkingChargeCalculatorDecorator(ParkingChargeCalculator component) {
        if (component == null) throw new IllegalArgumentException("component must not be null");
        this.component = component;
    }

    //Default delegation — subclasses override this and call super or component directly.
     
    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        return component.getParkingCharge(entryTime, lot, permit);
    }
}
