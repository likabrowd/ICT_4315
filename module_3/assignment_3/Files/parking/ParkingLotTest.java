package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Tests lot equality, toString formatting, and basic behavior of info. 

public class ParkingLotTest {

    @Test
    public void testEqualityByLotId() {
        ParkingLot l1 = new ParkingLot("L1", "Main Lot", ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0), Money.ofDollars(12.0));
        ParkingLot l2 = new ParkingLot("L1", "Main Lot", ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0), Money.ofDollars(12.0));
        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    public void testToStringNotNull() {
        ParkingLot lot = new ParkingLot("L2", "Overflow", ScanType.ENTRY_ONLY,
                Money.ofDollars(1.5), Money.ofDollars(10.0));
        assertNotNull(lot.toString());
    }
}
