package parking.charges.strategy;

import parking.CarType;
import parking.Money;
import parking.ParkingPermit;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 Strategy 1: Vehicle Type + Day of Week
 
 Rules:
 - COMPACT cars receive a 20% discount off the base rate.
 - On weekends (Saturday / Sunday) ALL vehicles receive an additional 15% discount to attract more traffic to underutilised lots.
 - Discounts are applied multiplicatively (compact on a weekend = 0.80 × 0.85 of base).
 */

public class VehicleTypeAndDayOfWeekStrategy implements ParkingChargeStrategy {

    private static final double COMPACT_DISCOUNT   = 0.80; // 20% off
    private static final double WEEKEND_DISCOUNT   = 0.85; // 15% off

    @Override
    public Money calculateCharge(Money baseRate, Instant entryTime, ParkingPermit permit) {
        if (baseRate == null || entryTime == null || permit == null) {
            return Money.ofDollars(0);
        }

        double rate = baseRate.getDollars();

        //Factor 1: vehicle type discount for COMPACT cars
        CarType carType = getCarType(permit);
        if (carType == CarType.COMPACT) {
            rate *= COMPACT_DISCOUNT;
        }

        //Factor 2: weekend discount
        DayOfWeek day = ZonedDateTime.ofInstant(entryTime, ZoneId.systemDefault()).getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            rate *= WEEKEND_DISCOUNT;
        }

        return Money.ofDollars(rate);
    }

    /** Safely resolves CarType from the permit → car chain. */
    private CarType getCarType(ParkingPermit permit) {
        try {
            return permit.getCar().getType();
        } catch (NullPointerException e) {
            return CarType.SEDAN; // safe default — no discount applied
        }
    }
}
