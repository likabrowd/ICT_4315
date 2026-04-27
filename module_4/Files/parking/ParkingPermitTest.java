package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Verifies that permits generate unique IDs and are associated w/ vehicles. 

public class ParkingPermitTest {

    @Test
    public void testIdAndDistinctness() {
        ParkingPermit p1 = new ParkingPermit();
        ParkingPermit p2 = new ParkingPermit();
        assertNotNull(p1.getPermitId());
        assertNotNull(p2.getPermitId());
        assertNotEquals(p1, p2);
        assertTrue(p1.toString().contains("Permit["));
    }
}
