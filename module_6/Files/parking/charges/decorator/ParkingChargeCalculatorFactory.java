package parking.charges.decorator;

import parking.ScanType;

/**
 Factory for building ParkingChargeCalculator decorator chains.
 
 This factory centralises all "which decorators should I use?" decisions so that the rest of the system (ParkingLot, TransactionManager) never needs to
 know which concrete calculator or decorator classes are being constructed.
 
 Factory pattern role:  Static Factory / Utility class
 Decorator pattern role: orchestrates the chain construction
 
 Default chains per ScanType

 ENTRY_ONLY:
 *   SpecialDaySurchargeDecorator
 *     └─ WeekendDiscountDecorator
 *          └─ CompactCarDiscountDecorator
 *               └─ FlatRateCalculator
 

 ENTRY_EXIT:
 *   SpecialDaySurchargeDecorator
 *     └─ PrimeTimeSurchargeDecorator
 *          └─ CompactCarDiscountDecorator
 *               └─ FlatRateCalculator
 

 The innermost object is always FlatRateCalculator; each outer layer adds one pricing factor.  Factors are applied from innermost outward, i.e. the
 outermost decorator's factor is applied last — but because each decorator receives the already-modified value from its inner component, the order in
 which multiplicative factors are applied does not change the final result for percentage-based adjustments.
 */

public class ParkingChargeCalculatorFactory {

    //Utility class — no instantiation
    private ParkingChargeCalculatorFactory() {
        throw new AssertionError("ParkingChargeCalculatorFactory is a utility class.");
    }

    /**
     Build the default decorator chain for the given ScanType.
     
     @param scanType the lot's scan type; null defaults to ENTRY_ONLY behaviour
     @return a fully constructed ParkingChargeCalculator chain
     */

    public static ParkingChargeCalculator buildCalculator(ScanType scanType) {
        //Always start with the flat base rate
        ParkingChargeCalculator base = new FlatRateCalculator();

        if (scanType == ScanType.ENTRY_EXIT) {
            //ENTRY_EXIT: compact discount → prime-time surcharge → special day surcharge
            ParkingChargeCalculator withCompact   = new CompactCarDiscountDecorator(base);
            ParkingChargeCalculator withPrimeTime = new PrimeTimeSurchargeDecorator(withCompact);
            return new SpecialDaySurchargeDecorator(withPrimeTime);
        } else {
            //ENTRY_ONLY (default): compact discount → weekend discount → special day surcharge
            ParkingChargeCalculator withCompact  = new CompactCarDiscountDecorator(base);
            ParkingChargeCalculator withWeekend  = new WeekendDiscountDecorator(withCompact);
            return new SpecialDaySurchargeDecorator(withWeekend);
        }
    }
}
