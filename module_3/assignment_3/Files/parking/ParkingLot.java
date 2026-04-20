package parking;

import parking.charges.strategy.ParkingChargeStrategy;
import parking.charges.strategy.ParkingChargeStrategyFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
  Represents a parking lot with a fee structure and entry/exit behavior based on its ScanType.
 
 Updated for Week 3 assignment: each lot now holds a ParkingChargeStrategy
 that calculates charges using a pluggable algorithm (Strategy Pattern).
 The strategy is chosen at construction time via the factory, but can be swapped at runtime via setStrategy().
 */

public class ParkingLot {

    private final String lotId;
    private final String name;
    private final ScanType scanType;
    private final Money feeOnEntry;
    private final Money feeOvernight;

    //The pluggable pricing algorithm for this lot. 
    private ParkingChargeStrategy strategy;

    public ParkingLot(String lotId, String name, ScanType scanType,
                      Money feeOnEntry, Money feeOvernight) {
        this.lotId       = lotId;
        this.name        = name;
        this.scanType    = scanType    != null ? scanType    : ScanType.ENTRY_ONLY;
        this.feeOnEntry  = feeOnEntry  != null ? feeOnEntry  : Money.ofDollars(0);
        this.feeOvernight= feeOvernight!= null ? feeOvernight: Money.ofDollars(0);

        // Factory selects the appropriate default strategy for this lot type.
        this.strategy = ParkingChargeStrategyFactory.getStrategy(this.scanType);
    }

    //Getters

    public String getLotId()        { return lotId; }
    public String getName()         { return name; }
    public ScanType getScanType()   { return scanType; }
    public Money getFeeOnEntry()    { return feeOnEntry; }
    public Money getFeeOvernight()  { return feeOvernight; }
    public boolean isScanOnExit()   { return scanType == ScanType.ENTRY_EXIT; }

    //Strategy getter / setter (allows runtime swap)
    public ParkingChargeStrategy getStrategy()                  { return strategy; }
    public void setStrategy(ParkingChargeStrategy strategy)     { this.strategy = strategy; }

    //Charge calculation

    /**
     Calculate the parking charge for a permit entering at the given time.
     Delegates entirely to the current strategy.
     
     @param entryTime when the vehicle entered
     @param permit    the vehicle's permit (carries CarType etc.)
     @return          the Money amount to charge
     */

    public Money getParkingCharge(Instant entryTime, ParkingPermit permit) {
        Money baseRate = (scanType == ScanType.ENTRY_EXIT) ? feeOvernight : feeOnEntry;
        return strategy.calculateCharge(baseRate, entryTime, permit);
    }

    //Legacy fee calculator kept for backward-compatibility with existing tests and the Main demo class.
     
    public Money calculateFee(long hoursParked, boolean isCompact) {
        Money base;
        if (scanType == ScanType.ENTRY_ONLY) {
            base = feeOnEntry;
        } else if (hoursParked <= 1) {
            base = feeOnEntry;
        } else {
            base = feeOvernight.times(hoursParked);
        }
        if (isCompact) {
            BigDecimal discounted = BigDecimal.valueOf(base.getDollars())
                    .multiply(BigDecimal.valueOf(0.8));
            return Money.ofDollars(discounted.doubleValue());
        }
        return base;
    }

    //Entry / Exit 

    public boolean entry(String permitId, ParkingOffice office) {
        if (permitId == null || office == null) return false;
        Car car = office.findCarByPermit(permitId).orElse(null);
        if (car == null) return false;
        if (scanType == ScanType.ENTRY_ONLY) {
            ParkingPermit permit = car.getPermit();
            Money charge = getParkingCharge(Instant.now(), permit);
            office.addCharge(new ParkingCharge(permitId, lotId, Instant.now(),
                    BigDecimal.valueOf(charge.getDollars())));
        }
        return true;
    }

    public boolean exit(String permitId, ParkingOffice office) {
        if (permitId == null || office == null) return false;
        Car car = office.findCarByPermit(permitId).orElse(null);
        if (car == null) return false;
        if (scanType == ScanType.ENTRY_EXIT) {
            ParkingPermit permit = car.getPermit();
            Money charge = getParkingCharge(Instant.now(), permit);
            office.addCharge(new ParkingCharge(permitId, lotId, Instant.now(),
                    BigDecimal.valueOf(charge.getDollars())));
        }
        return true;
    }

    //Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        ParkingLot lot = (ParkingLot) o;
        return Objects.equals(lotId, lot.lotId);
    }

    @Override public int hashCode() { return Objects.hash(lotId); }

    @Override public String toString() {
        return String.format("ParkingLot[%s, %s, %s]", lotId, name, scanType);
    }
}
