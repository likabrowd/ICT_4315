package parking;

// Demonstrates the parking system as a whole. 

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Address addr = new Address("2199 S. University Blvd.", "Denver", "CO", "80208");
        ParkingOffice office = new ParkingOffice("DU Parking", addr);

        // create lot: Entry + Exit, $2/hr, $12/day
        ParkingLot lotA = new ParkingLot("L1", "Lot A", ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0), Money.ofDollars(12.0));
                
        // create lot: Entry Only, $1.50 entry
        ParkingLot lotB = new ParkingLot("L2", "Lot B", ScanType.ENTRY_ONLY,
                Money.ofDollars(1.5), Money.ofDollars(15.0));
        office.addLot(lotA);
        office.addLot(lotB);

        // Register a customer
        Customer kalika = office.register("Kalika", addr, "303-123-4567");
        Car kalikaCar = office.register(kalika, "OCD-123", CarType.COMPACT);
        System.out.println("Registered: " + kalika + " with car " + kalikaCar + " permit " + kalikaCar.getPermit());

        // Car enters Lot B (entry-only), causes immediate charge
        boolean enteredB = lotB.entry(kalikaCar.getPermit().getPermitId(), office);
        System.out.println("Entered Lot B (entry-only): " + enteredB);
        office.getAllCharges().forEach(System.out::println);

        // Car enters Lot A (entry-exit)
        boolean enteredA = lotA.entry(kalikaCar.getPermit().getPermitId(), office);
        System.out.println("Entered Lot A (entry-exit): " + enteredA);

        // simulate parked 70 minutes by manually calculating fee
        long hoursParked = 2; // simulate 2 hours
        Money fee = lotA.calculateFee(hoursParked, kalikaCar.getType() == CarType.COMPACT);
        office.addCharge(new ParkingCharge(kalikaCar.getPermit().getPermitId(), lotA.getLotId(),
                java.time.Instant.now(), java.math.BigDecimal.valueOf(fee.getDollars())));

        boolean exitedA = lotA.exit(kalikaCar.getPermit().getPermitId(), office);
        System.out.println("Exited Lot A: " + exitedA);

        System.out.println("All charges:");
        office.getAllCharges().forEach(System.out::println);

        System.out.println("Charges for Kalika:");
        office.getChargesForCustomer(kalika).forEach(System.out::println);
    }
}
