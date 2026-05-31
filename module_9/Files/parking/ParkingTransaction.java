package parking;

import java.time.Instant;
import java.util.Objects;

//Represents a single event of a vehicle in one of the parking lots. 

public class ParkingTransaction {

    private final Instant transactionDate;
    private final ParkingPermit permit;
    private final ParkingLot lot;
    private final Money feeCharged;

    public ParkingTransaction(Instant transactionDate, ParkingPermit permit, ParkingLot lot, Money feeCharged) {
        this.transactionDate = transactionDate;
        this.permit = permit;
        this.lot = lot;
        this.feeCharged = feeCharged;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public ParkingPermit getPermit() {
        return permit;
    }

    public ParkingLot getParkingLot() {
        return lot;
    }

    public Money getFee() {
        return feeCharged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingTransaction)) return false;
        ParkingTransaction that = (ParkingTransaction) o;
        return Objects.equals(transactionDate, that.transactionDate)
                && Objects.equals(permit, that.permit)
                && Objects.equals(lot, that.lot)
                && Objects.equals(feeCharged, that.feeCharged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionDate, permit, lot, feeCharged);
    }

    @Override
    public String toString() {
        return "ParkingTransaction[" + permit.getPermitId() + "]";
    }
}
