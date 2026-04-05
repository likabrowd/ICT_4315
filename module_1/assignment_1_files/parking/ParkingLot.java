package parking;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

// Represents a parking lot with a fee structure / entry and exit behavior based on the scan type. 
public class ParkingLot {
    private final String lotId;
    private final String name;
    private final ScanType scanType;
    private final Money feeOnEntry;
    private final Money feeOvernight;

    public ParkingLot(String lotId, String name, ScanType scanType, Money feeOnEntry, Money feeOvernight) {
        this.lotId = lotId;
        this.name = name;
        this.scanType = scanType != null ? scanType : ScanType.ENTRY_ONLY;
        this.feeOnEntry = feeOnEntry != null ? feeOnEntry : Money.ofDollars(0);
        this.feeOvernight = feeOvernight != null ? feeOvernight : Money.ofDollars(0);
    }

    public String getLotId() { return lotId; }
    public String getName() { return name; }
    public ScanType getScanType() { return scanType; }
    public Money getFeeOnEntry() { return feeOnEntry; }
    public Money getFeeOvernight() { return feeOvernight; }
    public boolean isScanOnExit() { return scanType == ScanType.ENTRY_EXIT; }

    public Money calculateFee(long hoursParked, boolean isCompact) {
        Money base;
        if (scanType == ScanType.ENTRY_ONLY) {
            base = feeOnEntry;
        } else if (hoursParked <= 1) {
            base = feeOnEntry;
        } else {
            base = feeOvernight.times(hoursParked);
        }
        if (isCompact) {
            BigDecimal discounted = BigDecimal.valueOf(base.getDollars()).multiply(BigDecimal.valueOf(0.8));
            return Money.ofDollars(discounted.doubleValue());
        }
        return base;
    }

    public boolean entry(String permitId, ParkingOffice office) {
        if (permitId == null || office == null) return false;
        Car car = office.findCarByPermit(permitId).orElse(null);
        if (car == null) return false;
        if (scanType == ScanType.ENTRY_ONLY) {
            office.addCharge(new ParkingCharge(permitId, lotId, Instant.now(),
                    BigDecimal.valueOf(feeOnEntry.getDollars())));
        }
        return true;
    }

    public boolean exit(String permitId, ParkingOffice office) {
        if (permitId == null || office == null) return false;
        Car car = office.findCarByPermit(permitId).orElse(null);
        if (car == null) return false;
        if (scanType == ScanType.ENTRY_EXIT) {
            office.addCharge(new ParkingCharge(permitId, lotId, Instant.now(),
                    BigDecimal.valueOf(feeOvernight.getDollars())));
        }
        return true;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        ParkingLot lot = (ParkingLot) o;
        return Objects.equals(lotId, lot.lotId);
    }

    @Override public int hashCode() { return Objects.hash(lotId); }

    @Override public String toString() {
        return String.format("ParkingLot[%s, %s, scanType=%s]", lotId, name, scanType);
    }
}
