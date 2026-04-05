package parking;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/* Acts as the central system that 
   registers both the customers and cars, manages the lots, creates permits,
   and computes parking charges. */

public class ParkingOffice {
    private final String officeName;
    private final Address address;

    private final List<Customer> customers = new ArrayList<>();
    private final List<Car> cars = new ArrayList<>();
    private final List<ParkingLot> lots = new ArrayList<>();
    private final List<ParkingCharge> charges = new ArrayList<>();

    private final TransactionManager transactionManager = new TransactionManager();
    private final PermitManager permitManager = new PermitManager();

    public ParkingOffice(String officeName, Address address) {
        this.officeName = officeName;
        this.address = address;
    }

    // Customer and Car management 
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Car> getCars() {
        return Collections.unmodifiableList(cars);
    }

    public List<ParkingLot> getLots() {
        return Collections.unmodifiableList(lots);
    }

    public String getOfficeName() {
        return officeName;
    }

    public Address getAddress() {
        return address;
    }

    public void addCustomer(Customer c) {
        if (c != null && !customers.contains(c)) {
            customers.add(c);
        }
    }

    public void addCar(Car c) {
        if (c != null && !cars.contains(c)) {
            cars.add(c);
        }
    }

    public void addLot(ParkingLot lot) {
        if (lot != null && !lots.contains(lot)) {
            lots.add(lot);
        }
    }

    public void addCharge(ParkingCharge pc) {
        if (pc != null) {
            charges.add(pc);
        }
    }

    //  Register methods 
    public Customer register(String name, Address address, String phone) {
        Customer c = new Customer(name, address, phone);
        addCustomer(c);
        return c;
    }

    public void register(Customer customer) {
        addCustomer(customer);
    }

    public Car register(Customer customer, String license, CarType type) {
        if (customer == null) throw new IllegalArgumentException("customer null");
        Car car = new Car(license, type, customer);
        customer.addCar(car);
        addCar(car);
        permitManager.register(car);
        return car;
    }

    public ParkingPermit register(Car car) {
        if (car == null) return null;
        Customer owner = car.getOwner();
        if (owner != null && !customers.contains(owner)) {
            addCustomer(owner);
        }
        if (!cars.contains(car)) {
            addCar(car);
        }
        if (car.getPermit() != null) {
            return car.getPermit();
        }
        return permitManager.register(car);
    }

    //  Query methods 
    public Collection<String> getCustomerIds() {
        return customers.stream().map(Customer::getCustomerId).collect(Collectors.toSet());
    }

    public Collection<String> getPermitIds() {
        return cars.stream()
                .map(Car::getPermit)
                .filter(Objects::nonNull)
                .map(Permit::getPermitId)
                .collect(Collectors.toSet());
    }

    public Collection<String> getPermitIds(Customer customer) {
        if (customer == null) return Collections.emptyList();
        return customer.getCars().stream()
                .map(Car::getPermit)
                .filter(Objects::nonNull)
                .map(Permit::getPermitId)
                .collect(Collectors.toSet());
    }

    public Optional<Car> findCarByPermit(String permitId) {
        if (permitId == null) return Optional.empty();
        return cars.stream()
                .filter(c -> c.getPermit() != null && permitId.equals(c.getPermit().getPermitId()))
                .findFirst();
    }

    //  Parking charges 
    public List<ParkingCharge> getAllCharges() {
        return Collections.unmodifiableList(charges);
    }

    public List<ParkingCharge> getChargesForCustomer(Customer customer) {
        if (customer == null) return Collections.emptyList();
        Collection<String> permitIds = getPermitIds(customer);
        return charges.stream()
                .filter(pc -> permitIds.contains(pc.getPermitId()))
                .collect(Collectors.toList());
    }

    //  Parking transactions 
    public ParkingTransaction park(java.util.Date date, ParkingPermit permit, ParkingLot lot) {
        if (permit == null || lot == null) return null;
        Instant when = (date == null ? Instant.now() : date.toInstant());
        return transactionManager.park(when, permit, lot);
    }

    public Money getParkingCharges(ParkingPermit permit) {
        return transactionManager.getParkingCharges(permit);
    }

    public Money getParkingCharges(Customer customer) {
        return transactionManager.getParkingChargesByCustomer(customer);
    }

    @Override
    public String toString() {
        return String.format("ParkingOffice[%s]", officeName);
    }
}