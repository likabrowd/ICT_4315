package parking.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import parking.Address;
import parking.ParkingOffice;
import parking.ParkingService;
import parking.TransactionManager;
import parking.PermitManager;

/**
 Guice module that wires together the Parking System dependencies.
 
 By declaring all bindings here we achieve:
 - A single authoritative place to see every dependency relationship.
 - Easy swap of implementations (e.g., swap ParkingService for a mock in tests by providing a different module).
 - Classes receive dependencies through constructors annotated with
   @Inject rather than calling "new" themselves.
 */

public class ParkingModule extends AbstractModule {

    private final String officeName;
    private final Address officeAddress;

    /**
     Create the module with office configuration.
     
     @param officeName    the display name of the parking office
     @param officeAddress the physical address of the parking office
     */

    public ParkingModule(String officeName, Address officeAddress) {
        this.officeName    = officeName;
        this.officeAddress = officeAddress;
    }

    @Override
    protected void configure() {
        //All bindings are handled by @Provides methods below.
        //AbstractModule.configure() is intentionally left empty so that the dependency graph is fully visible in one place.
    }

    /**
     Provides a singleton ParkingOffice configured with the given name and address.  Singleton scope ensures all components share the same office
     instance — critical for consistent customer/car/permit data.
     */

    @Provides
    @Singleton
    public ParkingOffice provideParkingOffice() {
        return new ParkingOffice(officeName, officeAddress);
    }

    /**
     Provides a singleton TransactionManager.
     Shared across all components so every transaction is recorded in one place.
     */

    @Provides
    @Singleton
    public TransactionManager provideTransactionManager() {
        return new TransactionManager();
    }

    /**
     Provides a singleton PermitManager.
     Shared so that permit lookups are consistent across the system.
     */

    @Provides
    @Singleton
    public PermitManager providePermitManager() {
        return new PermitManager();
    }

    /**
     Provides a singleton ParkingService backed by the shared ParkingOffice.
     ParkingService is the entry point for all command execution, so it must be a singleton to prevent duplicate command registrations.
     
     @param office the shared ParkingOffice instance
     */
    
    @Provides
    @Singleton
    public ParkingService provideParkingService(ParkingOffice office) {
        return new ParkingService(office);
    }
}
