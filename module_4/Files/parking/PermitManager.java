package parking;

import java.util.*;

//Manages ParkingPermit creation and lookups tied to cars + licenses. 

public class PermitManager {

    private final Map<String, ParkingPermit> permits = new HashMap<>();

    public ParkingPermit register(Car car) {
        if (car == null) return null;
        if (permits.containsKey(car.getLicense())) {
            ParkingPermit existing = permits.get(car.getLicense());
            if (car.getPermit() == null) car.setPermit(existing);
            return existing;
        }
        ParkingPermit permit = new ParkingPermit(car);
        permits.put(car.getLicense(), permit);
        car.setPermit(permit);
        return permit;
    }

    public Optional<ParkingPermit> findById(String permitId) {
        if (permitId == null) return Optional.empty();
        return permits.values().stream()
                .filter(p -> permitId.equals(p.getPermitId()))
                .findFirst();
    }

    public Optional<ParkingPermit> findByLicense(String license) {
        if (license == null) return Optional.empty();
        return Optional.ofNullable(permits.get(license));
    }
}
