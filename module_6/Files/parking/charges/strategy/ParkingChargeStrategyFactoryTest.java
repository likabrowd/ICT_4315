package parking.charges.strategy;

import org.junit.jupiter.api.Test;
import parking.ScanType;
import parking.charges.factory.ParkingChargeStrategyFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 Verifies that the factory returns the correct strategy for each lot type.
 
 NOTE: This test lives in parking.charges.strategy but imports ParkingChargeStrategyFactory from parking.charges.factory. The factory was moved to its own package as part of Assignment 5.
 */

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
