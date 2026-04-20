package parking.charges.strategy;

import parking.Money;
import parking.ParkingPermit;

import java.time.Instant;

/* Strategy interface for calculating parking charges:
 
 Each implementing class encapsulates one pricing algorithm.
 The TransactionManager calls calculateCharge() without knowing which concrete strategy is in use, making strategies freely swappable.
 
 Factors available to any strategy:
    - baseRate   : the lot's configured daily/hourly base rate.
    - entryTime  : when the vehicle entered (for day-of-week, time-of-day checks).
    - permit     : links back to the Car and its CarType (for vehicle-type discounts).
 */


public interface ParkingChargeStrategy {

    /**  Calculate the charge for one parking event.
     
      @param baseRate   the lot's base Money rate
      @param entryTime  the Instant the vehicle entered the lot
      @param permit     the ParkingPermit associated with the vehicle
      @return           the Money amount to charge for this transaction
     */
    Money calculateCharge(Money baseRate, Instant entryTime, ParkingPermit permit);
}
