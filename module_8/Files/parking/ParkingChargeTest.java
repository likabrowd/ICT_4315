package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;

//Validates that ParkingCharge objects store correct information. 

public class ParkingChargeTest {

    @Test
    public void testEqualityByAllFields() {
        String permitId = "PERM-1";
        String lotId = "L1";
        Instant when = Instant.now();
        BigDecimal amount = new BigDecimal("2.00");

        ParkingCharge a = new ParkingCharge(permitId, lotId, when, amount);
        ParkingCharge b = new ParkingCharge(permitId, lotId, when, amount);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }
}
