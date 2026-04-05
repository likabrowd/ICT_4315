package parking;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// Records + manages all parking transactions and calculates total fees. 

public class TransactionManager {

    private final List<ParkingTransaction> transactions = new ArrayList<>();

    public ParkingTransaction park(Instant when, ParkingPermit permit, ParkingLot lot) {
    if (when == null || permit == null || lot == null) {
        return null;
    }

    Money fee = lot.getFeeOnEntry();
    ParkingTransaction tx = new ParkingTransaction(when, permit, lot, fee);
    transactions.add(tx);
    return tx;
}


    public List<ParkingTransaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    public Money getParkingCharges(ParkingPermit permit) {
        if (permit == null) return Money.ofDollars(0);
        String pid = permit.getPermitId();
        long totalCents = transactions.stream()
                .filter(t -> t.getPermit() != null &&
                             pid.equals(t.getPermit().getPermitId()))
                .mapToLong(t -> t.getFee().getCents())
                .sum();
        return Money.ofCents(totalCents);
    }

    public Money getParkingChargesByCustomer(Customer customer) {
        if (customer == null) return Money.ofDollars(0);
        Set<String> permitIds = customer.getCars().stream()
                .map(Car::getPermit)
                .filter(Objects::nonNull)
                .map(Permit::getPermitId)
                .collect(Collectors.toSet());
        long totalCents = transactions.stream()
                .filter(t -> t.getPermit() != null &&
                             permitIds.contains(t.getPermit().getPermitId()))
                .mapToLong(t -> t.getFee().getCents())
                .sum();
        return Money.ofCents(totalCents);
    }
}
