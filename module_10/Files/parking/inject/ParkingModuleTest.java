package parking.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parking.*;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 Tests verifying that the Guice DI wiring is correct.
 
 These tests demonstrate a key benefit of Dependency Injection: we can create a fully-wired application object graph in a single
 line (Guice.createInjector), and every object in that graph shares the same singleton instances.
 
 * Testability improvements shown:
   1. ParkingModule can be created with any office address — tests use a test address without touching production config.
   2. All singletons (ParkingOffice, ParkingService, etc.) are shared; a customer registered through ParkingService is visible when you inspect ParkingOffice directly.
   3. No static state, no global singletons — each test creates its own fresh injector (and therefore fresh object graph).
 */

public class ParkingModuleTest {

    private Injector injector;

    @BeforeEach
    public void setUp() {
        Address testAddress = new Address(
                "2199 S. University Blvd.", "Denver", "CO", "80208");
        injector = Guice.createInjector(
                new ParkingModule("Test Office", testAddress));
    }

    //Injector wiring

    @Test
    public void injectorProvidesParkingOffice() {
        ParkingOffice office = injector.getInstance(ParkingOffice.class);
        assertNotNull(office, "Guice should provide a ParkingOffice");
        assertEquals("Test Office", office.getOfficeName());
    }

    @Test
    public void injectorProvidesParkingService() {
        ParkingService service = injector.getInstance(ParkingService.class);
        assertNotNull(service, "Guice should provide a ParkingService");
    }

    @Test
    public void injectorProvidesTransactionManager() {
        TransactionManager tm = injector.getInstance(TransactionManager.class);
        assertNotNull(tm, "Guice should provide a TransactionManager");
    }

    @Test
    public void injectorProvidesPermitManager() {
        PermitManager pm = injector.getInstance(PermitManager.class);
        assertNotNull(pm, "Guice should provide a PermitManager");
    }

    //Singleton scope 

    @Test
    public void parkingOfficeisSingleton() {
        ParkingOffice o1 = injector.getInstance(ParkingOffice.class);
        ParkingOffice o2 = injector.getInstance(ParkingOffice.class);
        assertSame(o1, o2, "ParkingOffice should be a singleton");
    }

    @Test
    public void parkingServiceIsSingleton() {
        ParkingService s1 = injector.getInstance(ParkingService.class);
        ParkingService s2 = injector.getInstance(ParkingService.class);
        assertSame(s1, s2, "ParkingService should be a singleton");
    }

    @Test
    public void parkingServiceAndOfficeShareSameInstance() {
        ParkingOffice  office  = injector.getInstance(ParkingOffice.class);
        ParkingService service = injector.getInstance(ParkingService.class);
        assertSame(office, service.getOffice(),
                "ParkingService must use the same ParkingOffice singleton");
    }

    //Functional correctness through DI

    @Test
    public void registerCustomerViaInjectedService_appearsInOffice() {
        ParkingService service = injector.getInstance(ParkingService.class);
        ParkingOffice  office  = injector.getInstance(ParkingOffice.class);

        Properties params = new Properties();
        params.setProperty("firstname", "Kalika");
        params.setProperty("phone",     "303-123-4567");

        String result = service.performCommand("CUSTOMER", params);
        assertTrue(result.contains("Kalika"),
                "Registration result should contain customer name");

        assertEquals(1, office.getCustomers().size(),
                "Office should have exactly one customer after registration");
    }

    @Test
    public void registerCarViaInjectedService_returns200() {
        ParkingService service = injector.getInstance(ParkingService.class);

        //Step 1: register customer
        Properties cParams = new Properties();
        cParams.setProperty("firstname", "Kalika");
        String custResult = service.performCommand("CUSTOMER", cParams);
        String customerId = custResult.substring(
                "Customer registered: ".length(), custResult.indexOf(" | "));

        //Step 2: register car
        Properties carParams = new Properties();
        carParams.setProperty("license",    "KAL4CO");
        carParams.setProperty("customerid", customerId);
        carParams.setProperty("cartype",    "COMPACT");
        String carResult = service.performCommand("CAR", carParams);

        assertTrue(carResult.contains("KAL4CO"),
                "Car registration result should contain license plate");
    }

    @Test
    public void injectedServerProcessReturns200ForValidCustomer() {
        InjectedServer server = injector.getInstance(InjectedServer.class);

        Properties params = new Properties();
        params.setProperty("firstname", "Guice");
        ParkingRequest req  = new ParkingRequest("CUSTOMER", params);
        ParkingResponse resp = server.process(req.toJson());

        assertEquals(200, resp.getStatusCode(),
                "InjectedServer.process() should return 200 for valid CUSTOMER");
        assertTrue(resp.getMessage().contains("Guice"));
    }

    @Test
    public void injectedServerProcessReturns400ForMissingFirstname() {
        InjectedServer server = injector.getInstance(InjectedServer.class);
        ParkingResponse resp = server.process(
                new ParkingRequest("CUSTOMER", new Properties()).toJson());
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    public void differentModuleInstancesProduceDifferentObjectGraphs() {
        Address addr = new Address("1 Test St", "Denver", "CO", "80200");
        Injector injector2 = Guice.createInjector(
                new ParkingModule("Office B", addr));

        ParkingOffice o1 = injector.getInstance(ParkingOffice.class);
        ParkingOffice o2 = injector2.getInstance(ParkingOffice.class);

        assertNotSame(o1, o2,
                "Two separate injectors must produce independent object graphs");
        assertEquals("Test Office", o1.getOfficeName());
        assertEquals("Office B",    o2.getOfficeName());
    }
}
