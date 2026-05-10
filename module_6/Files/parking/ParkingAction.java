package parking;

/**
 Observer interface for parking lot events.

 Any class that wants to react to vehicle entry / exit events must implement this interface and register itself with one or more ParkingLot subjects!
 
 This is the formal "Observer" role in the Observer (Publish-Subscribe) pattern.
 The Subject (ParkingLot) holds a collection of ParkingAction instances and calls update() on each one whenever a relevant event occurs, without knowing
 anything about the concrete classes that implement it.
 */

public interface ParkingAction {
    /**
     Called by the Subject (ParkingLot) whenever a parking event occurs.
     
     @param event the event containing lot, permit, timestamp, and event type
     */
    
    void update(ParkingEvent event);
}
