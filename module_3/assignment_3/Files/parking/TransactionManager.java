package parking;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 Records and manages all parking transactions and calculates total fees.
 
 Updated for newest addition to assignment and delegates fee calculation to the lot's ParkingChargeStrategy instead of using a flat rate.
 */

public class TransactionManager {

    private final List<ParkingTransaction> transactions = new ArrayList<>();

    /**
     Create a parking transaction using the lot's current strategy.
     
     @param when   the entry (or exit) instant
     @param permit the permit for the vehicle
     @param lot    the lot being parked in
     @return       the created ParkingTransaction
     */


    public ParkingTransaction park(Instant when, ParkingPermit permit, ParkingLot lot) {
        if (when == null || permit == null || lot == null) {
            return null;
        }

        //Ask the lot (and its strategy) for the charge.
        Money fee = lot.getParkingCharge(when, permit);
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
