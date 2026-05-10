package parking.charges.decorator;

import parking.Money;
import parking.ParkingLot;
import parking.ParkingPermit;

import java.time.Instant;

/*
 Abstract base component in the Decorator pattern.
 
 ParkingChargeCalculator defines the interface (getParkingCharge) that every concrete calculator and every decorator must honour.  All participants in the
 chain share this common type, which is what lets decorators wrap any other calculator — concrete or already-decorated — transparently.
 
 Decorator pattern role: Component
 */

public abstract class ParkingChargeCalculator {

    /*
     Calculate the parking charge for a single transaction.
     
     @param entryTime the Instant the vehicle entered the lot
     @param lot       the ParkingLot being used
     @param permit    the ParkingPermit of the vehicle
     @return          the Money amount to charge
     */
    
    public abstract Money getParkingCharge(Instant entryTime, ParkingLot lot, ParkingPermit permit);
}
