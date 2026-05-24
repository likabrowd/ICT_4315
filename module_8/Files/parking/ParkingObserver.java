package parking;

import java.util.List;

/**
 Concrete Observer in the Observer pattern.

 ParkingObserver implements the ParkingAction interface and subscribes to every ParkingLot in the office at construction time.  When a lot fires a
 parking event (enter or exit), update() is called automatically.

 What ParkingObserver does with the event:
 - For ENTRY_ONLY lots: record a transaction on ENTER.
 - For ENTRY_EXIT lots: record a transaction on EXIT (charge is assessed when the vehicle leaves, since duration is now known).
 *

 The ParkingLot has no knowledge of this class — it only knows ParkingAction. This decoupling is the central benefit of the Observer pattern.
 */

public class ParkingObserver implements ParkingAction {

    private final TransactionManager transactionManager;

    /**
     Create a ParkingObserver and immediately register it with every lot currently known to the ParkingOffice.
     
     @param transactionManager the manager that will record each charge
     @param office             the office whose lots this observer will watch
     */

    public ParkingObserver(TransactionManager transactionManager, ParkingOffice office) {
        this.transactionManager = transactionManager;
        if (office != null) {
            List<ParkingLot> lots = office.getLots();
            for (ParkingLot lot : lots) {
                lot.addObserver(this);
            }
        }
    }

    /**
     Receive a parking event from a ParkingLot and decide whether to record a transaction.
     
     Decision logic:
     - ENTRY_ONLY lots charge on entry → act on ENTER events.
     - ENTRY_EXIT lots charge on exit  → act on EXIT events.
    
     @param event the event containing lot, permit, timestamp, and event type
     */

    @Override
    public void update(ParkingEvent event) {
        if (event == null) return;

        ParkingLot          lot       = event.getLot();
        ParkingPermit       permit    = event.getPermit();
        ParkingEvent.EventType type   = event.getEventType();

        boolean shouldCharge =
                (lot.getScanType() == ScanType.ENTRY_ONLY
                        && type == ParkingEvent.EventType.ENTER)
             || (lot.getScanType() == ScanType.ENTRY_EXIT
                        && type == ParkingEvent.EventType.EXIT);

        if (shouldCharge) {
            transactionManager.park(event.getTimestamp(), permit, lot);
        }
    }
}
