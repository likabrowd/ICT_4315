package parking;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects; 

public class Permit {
    private final String permitId = UUID.randomUUID().toString();
    private final Instant issuedAt = Instant.now();
    private boolean active = true;

    public String getPermitId() { return permitId; }
    public Instant getIssuedAt() { return issuedAt; }
    public boolean isActive() { return active; }
    public void revoke() { active = false; }

    @Override public String toString() {
        return String.format("Permit[%s]", permitId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permit)) return false;
        Permit p = (Permit) o;
        return Objects.equals(permitId, p.permitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permitId);
    }
}
