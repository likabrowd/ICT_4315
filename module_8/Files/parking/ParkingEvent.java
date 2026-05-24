package parking;

import java.time.Instant;

/**
 Immutable event object passed from ParkingLot (Subject) to all registered ParkingAction observers whenever a vehicle enters or exits.
 
 This is the "notification payload" in the Observer pattern — it carries everything an observer needs to react: which lot fired the event, which
 permit is involved, when it happened, and whether this is an entry or exit.
 */

public class ParkingEvent {

    //Distinguishes an entry scan from an exit scan.
    public enum EventType { ENTER, EXIT }

    private final ParkingLot  lot;
    private final ParkingPermit permit;
    private final Instant     timestamp;
    private final EventType   eventType;

    public ParkingEvent(ParkingLot lot, ParkingPermit permit,
                        Instant timestamp, EventType eventType) {
        this.lot       = lot;
        this.permit    = permit;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public ParkingLot   getLot()       { return lot; }
    public ParkingPermit getPermit()   { return permit; }
    public Instant      getTimestamp() { return timestamp; }
    public EventType    getEventType() { return eventType; }

    @Override
    public String toString() {
        return String.format("ParkingEvent[%s, permit=%s, lot=%s, time=%s]",
                eventType, permit, lot, timestamp);
    }
}
