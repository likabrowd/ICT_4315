package parking;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

//Validates the correctness of ParkingTransaction equality and components. 

public class ParkingTransactionTest {

    @Test
    public void testEquality() {
        ParkingPermit permit = new ParkingPermit();
        ParkingLot lot = new ParkingLot("L1", "Lot 1",
                ScanType.ENTRY_ONLY, Money.ofDollars(2), Money.ofDollars(10));

        ParkingTransaction t1 =
                new ParkingTransaction(Instant.EPOCH, permit, lot, Money.ofDollars(2));
        ParkingTransaction t2 =
                new ParkingTransaction(Instant.EPOCH, permit, lot, Money.ofDollars(2));

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        ParkingTransaction t3 =
                new ParkingTransaction(Instant.now(), permit, lot, Money.ofDollars(2));

        assertNotEquals(t1, t3);
    }
}
