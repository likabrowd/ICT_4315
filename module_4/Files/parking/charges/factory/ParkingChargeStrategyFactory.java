package parking.charges.factory;

import parking.ScanType;
import parking.charges.strategy.ParkingChargeStrategy;
import parking.charges.strategy.TimeOfDayAndSpecialDayStrategy;
import parking.charges.strategy.VehicleTypeAndDayOfWeekStrategy;

/**
 Factory for creating ParkingChargeStrategy instances.

 Centralises strategy-selection logic so that the rest of the system never needs to know which concrete class it is constructing.
 
 This class implements the Factory pattern (Gang of Four): it defines the interface for creating a ParkingChargeStrategy object
 and allows the ScanType parameter to determine which concrete subclass is instantiated. The caller never references a concrete strategy directly.
 
 Current policy:
 ENTRY_EXIT lots  → TimeOfDayAndSpecialDayStrategy
 (exit-scanned lots know duration; time-of-day pricing is appropriate)
 ENTRY_ONLY lots  → VehicleTypeAndDayOfWeekStrategy
 (entry-only lots charge a flat rate on arrival; vehicle type + day fits)
 */

public class ParkingChargeStrategyFactory {

    //Utility class — prevent instantiation
    private ParkingChargeStrategyFactory() {
        throw new AssertionError("ParkingChargeStrategyFactory is a utility class.");
    }

    /**
     Return the appropriate strategy for the given lot scan type.
     
     @param scanType the ScanType of the parking lot
     @return a ready-to-use ParkingChargeStrategy instance
     */

    public static ParkingChargeStrategy getStrategy(ScanType scanType) {
        if (scanType == ScanType.ENTRY_EXIT) {
            return new TimeOfDayAndSpecialDayStrategy();
        }
        //Default: ENTRY_ONLY or null → vehicle type + day-of-week strategy
        return new VehicleTypeAndDayOfWeekStrategy();
    }
}
