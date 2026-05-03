package parking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Records and manages all parking transactions and calculates total fees.
 
 TransactionManager is responsible for:
 1. Creating ParkingTransaction objects via park().
 2. Delegating fee calculation to the lot's ParkingChargeStrategy (obtained through ParkingChargeStrategyFactory at lot construction time).
 3. Aggregating charges by permit or by customer.
 
 This class does not select or create strategies directly. That responsibility belongs to ParkingChargeStrategyFactory, keeping concerns cleanly separated.
 */

public class TransactionManager {

    private final List<ParkingTransaction> transactions = new ArrayList<>();

    /**
     Create and record a parking transaction.
     
     The fee is calculated by asking the lot (which delegates to its ParkingChargeStrategy). TransactionManager never touches the strategy
     directly. It instead calls lot.getParkingCharge() and stores the result.
     
     @param when   the entry (or exit) instant
     @param permit the permit for the vehicle
     @param lot    the lot being parked in
     @return       the created ParkingTransaction, or null if any arg is null
     */

    public ParkingTransaction park(Instant when, ParkingPermit permit, ParkingLot lot) {
        if (when == null || permit == null || lot == null) {
            return null;
        }

        // Ask the lot (and its strategy) for the charge.
        Money fee = lot.getParkingCharge(when, permit);
        ParkingTransaction tx = new ParkingTransaction(when, permit, lot, fee);
        transactions.add(tx);
        return tx;
    }

    /**
     Return an unmodifiable view of all recorded transactions.
     
     @return all transactions
     */

    public List<ParkingTransaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     Sum all charges incurred by a specific permit.
     
     @param permit the permit to query
     @return total Money charged, or $0.00 if permit is null
     */

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

    /**
     Sum all charges across every permit owned by a customer.
     
     @param customer the customer to query
     @return total Money charged, or $0.00 if customer is null
     */

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
