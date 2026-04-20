package parking.charges.strategy;

import org.junit.jupiter.api.Test;
import parking.ScanType;

import static org.junit.jupiter.api.Assertions.*;

// Verifies that the factory returns the correct strategy for each lot type.
 
public class ParkingChargeStrategyFactoryTest {

    @Test
    public void entryExitLotGetsTimeOfDayStrategy() {
        ParkingChargeStrategy strategy = ParkingChargeStrategyFactory.getStrategy(ScanType.ENTRY_EXIT);
        assertInstanceOf(TimeOfDayAndSpecialDayStrategy.class, strategy,
                "ENTRY_EXIT lot should use TimeOfDayAndSpecialDayStrategy");
    }

    @Test
    public void entryOnlyLotGetsVehicleTypeAndDayStrategy() {
        ParkingChargeStrategy strategy = ParkingChargeStrategyFactory.getStrategy(ScanType.ENTRY_ONLY);
        assertInstanceOf(VehicleTypeAndDayOfWeekStrategy.class, strategy,
                "ENTRY_ONLY lot should use VehicleTypeAndDayOfWeekStrategy");
    }

    @Test
    public void nullScanTypeDefaultsToVehicleTypeStrategy() {
        ParkingChargeStrategy strategy = ParkingChargeStrategyFactory.getStrategy(null);
        assertInstanceOf(VehicleTypeAndDayOfWeekStrategy.class, strategy,
                "Null ScanType should default to VehicleTypeAndDayOfWeekStrategy");
    }
}
