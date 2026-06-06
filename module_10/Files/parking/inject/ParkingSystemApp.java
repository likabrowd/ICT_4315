package parking.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import parking.Address;
import parking.ParkingLot;
import parking.ParkingOffice;
import parking.ScanType;
import parking.Money;

/**
 Application entry point that bootstraps the Parking System using Guice.
 
 The Guice Injector is created here with a ParkingModule, which tells Guice how to construct every dependency.  After the injector is built, we ask it
 for an InjectedServer instance — Guice wires the full object graph (ParkingOffice → ParkingService → InjectedServer) automatically.
 
 Compare with the Assignment 9 Server.main():
 Old approach (manual wiring):
      ParkingOffice office = new ParkingOffice("DU Parking", addr);
      ParkingService service = new ParkingService(office);
      new Server().start();   //Server also created its own ParkingService!
 
 New approach (Guice DI):
      Injector injector = Guice.createInjector(new ParkingModule(...));
      InjectedServer server = injector.getInstance(InjectedServer.class);
      server.start();
 
 All "new" calls for the core singletons now live exclusively in ParkingModule.
 */

public class ParkingSystemApp {

    public static void main(String[] args) throws Exception {

        //1. Configure the office address (externally supplied to the module)
        Address officeAddress = new Address(
                "2199 S. University Blvd.", "Denver", "CO", "80208");

        //2. Build the Guice injector — this is the DI container
        Injector injector = Guice.createInjector(
                new ParkingModule("DU Parking", officeAddress));

        //3. Retrieve the fully-wired ParkingOffice and add lots
        ParkingOffice office = injector.getInstance(ParkingOffice.class);
        office.addLot(new ParkingLot("L1", "Main Lot",    ScanType.ENTRY_EXIT,
                Money.ofDollars(2.0),  Money.ofDollars(12.0)));
        office.addLot(new ParkingLot("L2", "Surface Lot", ScanType.ENTRY_ONLY,
                Money.ofDollars(1.5),  Money.ofDollars(15.0)));

        System.out.println("[App] Guice injector created successfully.");
        System.out.println("[App] Office: " + office.getOfficeName());
        System.out.println("[App] Lots registered: " + office.getLots().size());
        System.out.println("[App] Dependency Injection wiring complete.");
        System.out.println();

        //4. Retrieve and start the server — all dependencies injected by Guice
        InjectedServer server = injector.getInstance(InjectedServer.class);
        server.start();
    }
}
