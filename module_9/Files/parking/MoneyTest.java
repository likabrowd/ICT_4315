package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Tests monetary arithmetic and equality to ensure safe handling of currency values. 

public class MoneyTest {

    @Test
    public void testOfDollarsAndEquality() {
        Money m1 = Money.ofDollars(10.00);
        Money m2 = Money.ofDollars(10.00);
        assertEquals(m1, m2);
        assertEquals("$10.00", m1.toString());
    }

    @Test
    public void testCentsArithmetic() {
        Money m1 = Money.ofDollars(2.50);
        Money m2 = Money.ofDollars(1.50);
        Money sum = Money.ofCents(m1.getCents() + m2.getCents());
        assertEquals(Money.ofDollars(4.00), sum);
    }
}
