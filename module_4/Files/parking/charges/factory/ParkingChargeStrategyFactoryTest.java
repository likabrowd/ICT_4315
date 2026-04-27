package parking.charges.factory;

import org.junit.jupiter.api.Test;
import parking.ScanType;
import parking.charges.strategy.ParkingChargeStrategy;
import parking.charges.strategy.TimeOfDayAndSpecialDayStrategy;
import parking.charges.strategy.VehicleTypeAndDayOfWeekStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingChargeStrategyFactory.
 
 Verifies that the factory returns the correct concrete strategy for each ScanType, and handles null input gracefully.
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

    @Test
    public void factoryReturnsNewInstanceEachCall() {
        ParkingChargeStrategy s1 = ParkingChargeStrategyFactory.getStrategy(ScanType.ENTRY_ONLY);
        ParkingChargeStrategy s2 = ParkingChargeStrategyFactory.getStrategy(ScanType.ENTRY_ONLY);
        assertNotSame(s1, s2,
                "Factory should return a fresh instance on each call");
    }
}
