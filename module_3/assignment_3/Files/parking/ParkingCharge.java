package parking;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

//Represents a monetary charge applied to a permit in a specific lot, at a certain time. 

public class ParkingCharge {

    private final String permitId;
    private final String lotId;
    private final Instant incurred;
    private final BigDecimal amount;

    public ParkingCharge(String permitId, String lotId, Instant incurred, BigDecimal amount) {
        this.permitId = permitId;
        this.lotId = lotId;
        this.incurred = incurred;
        this.amount = amount;
    }

    public String getPermitId() {
        return permitId;
    }

    public String getLotId() {
        return lotId;
    }

    public Instant getIncurred() {
        return incurred;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format(
            "ParkingCharge[permitId=%s, lotId=%s, incurred=%s, amount=%s]",
            permitId, lotId, incurred, amount
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingCharge)) return false;
        ParkingCharge pc = (ParkingCharge) o;
        return Objects.equals(permitId, pc.permitId)
                && Objects.equals(lotId, pc.lotId)
                && Objects.equals(incurred, pc.incurred)
                && Objects.equals(amount, pc.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permitId, lotId, incurred, amount);
    }
}
