package parking.charges.strategy;

import parking.ScanType;

/**
  Factory for creating ParkingChargeStrategy instances:
 
 Centralises strategy-selection logic so that the rest of the system never needs to know which concrete class it is constructing.
 
 Current policy:
 ENTRY_EXIT lots  → TimeOfDayAndSpecialDayStrategy
 (exit-scanned lots already know duration; time-of-day pricing makes sense)
 ENTRY_ONLY lots  → VehicleTypeAndDayOfWeekStrategy
 (entry-only lots charge a flat rate on arrival, so vehicle type + day is appropriate)
 */

public class ParkingChargeStrategyFactory {

    private ParkingChargeStrategyFactory() { /* utility class — no instantiation */ }

    /**
     Return the default strategy for the given lot scan type.
     
     @param scanType the ScanType of the parking lot
     @return a ready-to-use ParkingChargeStrategy
     */

    public static ParkingChargeStrategy getStrategy(ScanType scanType) {
        if (scanType == ScanType.ENTRY_EXIT) {
            return new TimeOfDayAndSpecialDayStrategy();
        }
        
        // Default (ENTRY_ONLY or null)
        return new VehicleTypeAndDayOfWeekStrategy();
    }
}
