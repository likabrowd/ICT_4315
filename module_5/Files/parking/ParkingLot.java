package parking;

import parking.charges.factory.ParkingChargeStrategyFactory;
import parking.charges.strategy.ParkingChargeStrategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 Represents a parking lot — and now also acts as the Subject (Observable)in the Observer pattern.

 Observer pattern additions (for week 5):
 Maintains a list of ParkingAction observers.
 - addObserver() / removeObserver() register and deregister observers.
 - notifyObservers(ParkingEvent) broadcasts an event to every registered observer.
 - enter(ParkingPermit) and exit(ParkingPermit) create ParkingEvent objects and call notifyObservers(), keeping ParkingLot unaware of what observers do.

 The lot still holds its pricing strategy (Strategy pattern) and delegatescharge calculation to it exactly as before.
 */

public class ParkingLot {

    //Identity / configuration.

    private final String   lotId;
    private final String   name;
    private final ScanType scanType;
    private final Money    feeOnEntry;
    private final Money    feeOvernight;

    //trategy pattern.

    private ParkingChargeStrategy strategy;

    //Observer pattern: the Subject's observer collection.

    private final List<ParkingAction> observers = new ArrayList<>();

    //Constructor.

    public ParkingLot(String lotId, String name, ScanType scanType,
                      Money feeOnEntry, Money feeOvernight) {
        this.lotId        = lotId;
        this.name         = name;
        this.scanType     = scanType     != null ? scanType     : ScanType.ENTRY_ONLY;
        this.feeOnEntry   = feeOnEntry   != null ? feeOnEntry   : Money.ofDollars(0);
        this.feeOvernight = feeOvernight != null ? feeOvernight : Money.ofDollars(0);

        //Factory selects the appropriate default strategy for this lot type.
        this.strategy = ParkingChargeStrategyFactory.getStrategy(this.scanType);
    }

    //Getters.

    public String   getLotId()        { return lotId; }
    public String   getName()         { return name; }
    public ScanType getScanType()     { return scanType; }
    public Money    getFeeOnEntry()   { return feeOnEntry; }
    public Money    getFeeOvernight() { return feeOvernight; }
    public boolean  isScanOnExit()   { return scanType == ScanType.ENTRY_EXIT; }

    //Strategy getter/setter (runtime swap).

    public ParkingChargeStrategy getStrategy()              { return strategy; }
    public void setStrategy(ParkingChargeStrategy strategy) { this.strategy = strategy; }

    //Observer pattern: Subject methods.

    /**
     Register an observer to receive future parking events from this lot.
    
     @param observer the ParkingAction to add; ignored if null or already registered
     */

    public void addObserver(ParkingAction observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     Deregister a previously registered observer.
     
     @param observer the ParkingAction to remove
     */

    public void removeObserver(ParkingAction observer) {
        observers.remove(observer);
    }

    /**
     Return an unmodifiable view of the current observer list (useful for tests).
     */

    public List<ParkingAction> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    /**
     Broadcast a ParkingEvent to every registered observer. The lot does not know — or care — what each observer does with the event.
     
     @param event the event to broadcast
     */

    public void notifyObservers(ParkingEvent event) {
        for (ParkingAction observer : new ArrayList<>(observers)) {
            observer.update(event);
        }
    }

    //Entry / Exit (Observer-pattern-based).

    /**
     Record a vehicle entering this lot.
     Creates a ParkingEvent (ENTER) and notifies all observers.
     
     @param permit the permit of the entering vehicle
     */

    public void enter(ParkingPermit permit) {
        if (permit == null) return;
        ParkingEvent event = new ParkingEvent(
                this, permit, Instant.now(), ParkingEvent.EventType.ENTER);
        notifyObservers(event);
    }

    /**
     Record a vehicle exiting this lot.
     Creates a ParkingEvent (EXIT) and notifies all observers.
     
     @param permit the permit of the exiting vehicle
     */

    public void exit(ParkingPermit permit) {
        if (permit == null) return;
        ParkingEvent event = new ParkingEvent(
                this, permit, Instant.now(), ParkingEvent.EventType.EXIT);
        notifyObservers(event);
    }

    //Charge calculation.

    /**
     Calculate the parking charge for a permit entering at the given time.
     Delegates entirely to the current strategy.
     */

    public Money getParkingCharge(Instant entryTime, ParkingPermit permit) {
        Money baseRate = (scanType == ScanType.ENTRY_EXIT) ? feeOvernight : feeOnEntry;
        return strategy.calculateCharge(baseRate, entryTime, permit);
    }

    //Legacy entry/exit methods (kept for backward compatibility).

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

    //Legacy fee calculator (backward compatibility).

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

    //Object overrides.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        return Objects.equals(lotId, ((ParkingLot) o).lotId);
    }

    @Override public int hashCode() { return Objects.hash(lotId); }

    @Override public String toString() {
        return String.format("ParkingLot[%s, %s, %s]", lotId, name, scanType);
    }
}
