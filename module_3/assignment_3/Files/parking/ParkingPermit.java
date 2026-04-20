package parking;

/**
 A parking permit assigned to a specific vehicle.
 
 Extended from the original to hold a reference back to its Car so that ParkingChargeStrategy implementations can inspect vehicle type without needing a separate lookup.
 */

public class ParkingPermit extends Permit {

    private Car car;

    //Default constructor (keeps backward-compatibility with existing tests).
    public ParkingPermit() { }

    //Convenience constructor that binds the permit to its car immediately.
    public ParkingPermit(Car car) {
        this.car = car;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
