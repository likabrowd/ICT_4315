package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;
import parking.ScanType;

import java.time.Instant;

/*
 Concrete Component in the Decorator pattern.
 
 FlatRateCalculator is the innermost object in every decorator chain.
 It returns the lot's configured base rate with no modification:
 - ENTRY_ONLY lots  → feeOnEntry
 - ENTRY_EXIT lots  → feeOvernight (the daily/duration base)
 
 Every decorator wraps a ParkingChargeCalculator and calls component.getParkingCharge() first, then applies its own factor.
 FlatRateCalculator is always at the bottom of that call stack.
 
 Decorator pattern role: Concrete Component
 */

public class FlatRateCalculator extends ParkingChargeCalculator {

    /*
     Return the lot's plain base rate — no surcharges, no discounts.
     
     @param entryTime ignored by this implementation (no time-based logic here)
     @param lot       source of the base rate
     @param permit    ignored by this implementation
     @return          the lot's base Money rate
     */


    @Override
    public Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit) {
        if (lot == null) return Money.ofDollars(0);
        //ENTRY_EXIT lots track duration → use feeOvernight as the duration base.
        //ENTRY_ONLY lots charge a flat entry fee → use feeOnEntry.
        return (lot.getScanType() == ScanType.ENTRY_EXIT)
                ? lot.getFeeOvernight()
                : lot.getFeeOnEntry();
    }
}
