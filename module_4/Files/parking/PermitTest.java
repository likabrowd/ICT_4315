package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PermitTest {

    @Test
    public void testIdAndToString() {
        Permit p = new Permit();
        assertNotNull(p.getPermitId());
        assertNotNull(p.toString());
    }

    @Test
    public void testDistinctPermitsAreNotEqual() {
        Permit p1 = new Permit();
        Permit p2 = new Permit();
        assertNotEquals(p1, p2);
    }
}
