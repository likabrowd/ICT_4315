package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Verifies Address class correctly formats + stores data. 

public class AddressTest {

    @Test
    public void testToStringFormats() {
        Address a = new Address("100 Aster St", "Denver", "CO", "80202");
        assertEquals("100 Aster St, Denver, CO 80202", a.toString());
    }
}
