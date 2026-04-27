package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 Unit tests for TransactionManager.
 
 Tests cover:
 1. park() creates and stores a transaction
 2. Charges are aggregated correctly by permit
 3. Charges are aggregated correctly by customer (across multiple cars)
 4. Null arguments to park() return null safely
 5. Querying a null permit or null customer returns $0.00
 */
public class TransactionManagerTest {

    private TransactionManager tm;
    private ParkingLot entryOnlyLot;
    private ParkingLot entryExitLot;

    //Pinned to a known weekday (Monday) at noon. SEDAN, no discount = full rate.
    private static final Instant MONDAY_NOON = LocalDate.of(2025, 4, 7)
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant();

    @BeforeEach
    public void setUp() {
        tm = new TransactionManager();
        entryOnlyLot = new ParkingLot("A1", "Lot A", ScanType.ENTRY_ONLY,
                Money.ofDollars(10), Money.ofDollars(20));
        entryExitLot = new ParkingLot("B1", "Lot B", ScanType.ENTRY_EXIT,
                Money.ofDollars(5), Money.ofDollars(20));
    }

    //Helper: build a wired-up permit for a given CarType.
    private ParkingPermit permitFor(CarType type, String license) {
        Customer customer = new Customer("Test User",
                new Address("1 A St", "Denver", "CO", "80200"), "000-000-0000");
        Car car = new Car(license, type, customer);
        ParkingPermit permit = new ParkingPermit(car);
        car.setPermit(permit);
        customer.addCar(car);
        return permit;
    }

    //park() basic behaviour

    @Test
    public void parkReturnsParkingTransaction() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "AAA-001");
        ParkingTransaction tx = tm.park(MONDAY_NOON, permit, entryOnlyLot);
        assertNotNull(tx, "park() should return a non-null transaction");
    }

    @Test
    public void parkStoresTransactionInList() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "AAA-002");
        tm.park(MONDAY_NOON, permit, entryOnlyLot);
        assertEquals(1, tm.getAll().size());
    }

    @Test
    public void parkSetsCorrectPermitAndLot() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "AAA-003");
        ParkingTransaction tx = tm.park(MONDAY_NOON, permit, entryOnlyLot);
        assertEquals(permit, tx.getPermit());
        assertEquals(entryOnlyLot, tx.getParkingLot());
    }

    @Test
    public void parkNullWhenReturnsNull() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "AAA-004");
        assertNull(tm.park(null, permit, entryOnlyLot));
    }

    @Test
    public void parkNullPermitReturnsNull() {
        assertNull(tm.park(MONDAY_NOON, null, entryOnlyLot));
    }

    @Test
    public void parkNullLotReturnsNull() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "AAA-005");
        assertNull(tm.park(MONDAY_NOON, permit, null));
    }

    //Fee aggregation by permit

    @Test
    public void sedanWeekdayEntryOnlyPaysFullRate() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "BBB-001");
        tm.park(MONDAY_NOON, permit, entryOnlyLot);
        //SEDAN on weekday, ENTRY_ONLY lot base = $10 → no discount → $10.00
        assertEquals(10.0, tm.getParkingCharges(permit).getDollars(), 0.0001);
    }

    @Test
    public void compactWeekdayEntryOnlyPays20PercentLess() {
        ParkingPermit permit = permitFor(CarType.COMPACT, "BBB-002");
        tm.park(MONDAY_NOON, permit, entryOnlyLot);
        //COMPACT weekday: $10 × 0.80 = $8.00
        assertEquals(8.0, tm.getParkingCharges(permit).getDollars(), 0.0001);
    }

    @Test
    public void multipleParksAccumulateCharges() {
        ParkingPermit permit = permitFor(CarType.SEDAN, "CCC-001");
        tm.park(MONDAY_NOON, permit, entryOnlyLot);
        tm.park(MONDAY_NOON, permit, entryOnlyLot);
        //$10 + $10 = $20
        assertEquals(20.0, tm.getParkingCharges(permit).getDollars(), 0.0001);
    }

    @Test
    public void nullPermitQueryReturnsZero() {
        assertEquals(0.0, tm.getParkingCharges(null).getDollars(), 0.0001);
    }

    //Fee aggregation by customer

    @Test
    public void chargesByCustomerSumsAcrossCars() {
        Customer customer = new Customer("Multi Car",
                new Address("1 B St", "Denver", "CO", "80200"), "111-111-1111");

        Car car1 = new Car("DDD-001", CarType.SEDAN, customer);
        ParkingPermit p1 = new ParkingPermit(car1);
        car1.setPermit(p1);
        customer.addCar(car1);

        Car car2 = new Car("DDD-002", CarType.SEDAN, customer);
        ParkingPermit p2 = new ParkingPermit(car2);
        car2.setPermit(p2);
        customer.addCar(car2);

        tm.park(MONDAY_NOON, p1, entryOnlyLot);  // $10
        tm.park(MONDAY_NOON, p2, entryOnlyLot);  // $10

        assertEquals(20.0,
                tm.getParkingChargesByCustomer(customer).getDollars(), 0.0001,
                "Customer with 2 cars should see combined charges of $20.00");
    }

    @Test
    public void nullCustomerQueryReturnsZero() {
        assertEquals(0.0,
                tm.getParkingChargesByCustomer(null).getDollars(), 0.0001);
    }
}
