package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for ParkingObserver!
 
 The Tests cover:
 1. Observer registers itself with all lots at construction.
 2. ENTRY_ONLY lot records a transaction on ENTER.
 3. ENTRY_ONLY lot does NOT record a transaction on EXIT.
 4. ENTRY_EXIT lot records a transaction on EXIT.
 5. ENTRY_EXIT lot does NOT record a transaction on ENTER.
 6. Multiple observers receive the same event independently.
 7. Removing an observer stops it from receiving events.
 8. Null event to update() is handled gracefully.

 */

public class ParkingObserverTest {

    private ParkingOffice       office;
    private TransactionManager  tm;
    private ParkingLot          entryOnlyLot;
    private ParkingLot          entryExitLot;
    private ParkingPermit       sedanPermit;
    private ParkingPermit       compactPermit;

    @BeforeEach
    public void setUp() {
        office = new ParkingOffice("Test Office",
                new Address("1 Main St", "Denver", "CO", "80200"));

        entryOnlyLot = new ParkingLot("L1", "Entry Only Lot", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        entryExitLot = new ParkingLot("L2", "Entry Exit Lot", ScanType.ENTRY_EXIT,
                Money.ofDollars(5), Money.ofDollars(20));

        office.addLot(entryOnlyLot);
        office.addLot(entryExitLot);

        tm = new TransactionManager();

        //Build sedan permit.
        Customer c1 = new Customer("Alice",
                new Address("1 A St", "Denver", "CO", "80200"), "303-100-0001");
        Car sedan = new Car("AAA-001", CarType.SEDAN, c1);
        sedanPermit = new ParkingPermit(sedan);
        sedan.setPermit(sedanPermit);
        c1.addCar(sedan);

        //Build compact permit.
        Customer c2 = new Customer("Bob",
                new Address("2 B St", "Denver", "CO", "80200"), "303-100-0002");
        Car compact = new Car("BBB-001", CarType.COMPACT, c2);
        compactPermit = new ParkingPermit(compact);
        compact.setPermit(compactPermit);
        c2.addCar(compact);
    }

    //Observer registration. 

    @Test
    public void observerRegistersWithAllLotsOnConstruction() {
        ParkingObserver observer = new ParkingObserver(tm, office);
        assertTrue(entryOnlyLot.getObservers().contains(observer),
                "Observer should be registered with entryOnlyLot");
        assertTrue(entryExitLot.getObservers().contains(observer),
                "Observer should be registered with entryExitLot");
    }

    //ENTRY_ONLY lot behaviour. 

    @Test
    public void entryOnlyLotRecordsTransactionOnEnter() {
        new ParkingObserver(tm, office);
        entryOnlyLot.enter(sedanPermit);
        assertEquals(1, tm.getAll().size(),
                "ENTRY_ONLY enter should produce exactly one transaction");
    }

    @Test
    public void entryOnlyLotDoesNotRecordTransactionOnExit() {
        new ParkingObserver(tm, office);
        entryOnlyLot.exit(sedanPermit);
        assertEquals(0, tm.getAll().size(),
                "ENTRY_ONLY exit should produce no transaction");
    }

    @Test
    public void entryOnlyLotTransactionHasCorrectPermitAndLot() {
        new ParkingObserver(tm, office);
        entryOnlyLot.enter(sedanPermit);
        ParkingTransaction tx = tm.getAll().get(0);
        assertEquals(sedanPermit, tx.getPermit());
        assertEquals(entryOnlyLot, tx.getParkingLot());
    }

    //ENTRY_EXIT lot behaviour. 

    @Test
    public void entryExitLotRecordsTransactionOnExit() {
        new ParkingObserver(tm, office);
        entryExitLot.enter(sedanPermit);   //should NOT trigger charge
        entryExitLot.exit(sedanPermit);    //SHOULD trigger charge
        assertEquals(1, tm.getAll().size(),
                "ENTRY_EXIT exit should produce exactly one transaction");
    }

    @Test
    public void entryExitLotDoesNotRecordTransactionOnEnter() {
        new ParkingObserver(tm, office);
        entryExitLot.enter(sedanPermit);
        assertEquals(0, tm.getAll().size(),
                "ENTRY_EXIT enter should produce no transaction");
    }

    //Multiple observers.

    @Test
    public void multipleObserversEachReceiveEvent() {
        TransactionManager tm2 = new TransactionManager();
        new ParkingObserver(tm,  office);
        new ParkingObserver(tm2, office);

        entryOnlyLot.enter(sedanPermit);

        assertEquals(1, tm.getAll().size(),
                "First observer should record one transaction");
        assertEquals(1, tm2.getAll().size(),
                "Second observer should also record one transaction");
    }

    //Remove observer.

    @Test
    public void removedObserverStopsReceivingEvents() {
        ParkingObserver observer = new ParkingObserver(tm, office);
        entryOnlyLot.removeObserver(observer);

        entryOnlyLot.enter(sedanPermit);

        assertEquals(0, tm.getAll().size(),
                "Removed observer should not record any transaction");
    }

    //Compact discount through observer.

    @Test
    public void compactCarReceives20PercentDiscountViaObserver() {
        new ParkingObserver(tm, office);
        // entryOnlyLot base = $10; compact weekday discount = 20% off → $8.00
        entryOnlyLot.enter(compactPermit);
        assertEquals(1, tm.getAll().size());
        // Charge should be less than the $10 base
        assertTrue(tm.getAll().get(0).getFee().getDollars() < 10.0,
                "Compact car should pay less than base rate");
    }

    //Null safety.

    @Test
    public void updateWithNullEventDoesNotThrow() {
        ParkingObserver observer = new ParkingObserver(tm, office);
        assertDoesNotThrow(() -> observer.update(null));
        assertEquals(0, tm.getAll().size());
    }
}
