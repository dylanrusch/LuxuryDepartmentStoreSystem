package model;

import java.util.ArrayList;
import java.util.List;

public class Truck {
    private int truckId;
    private String destinationStore;
    private int distanceMiles;
    private List<String> cargo;

    public Truck(int truckId, String destinationStore, int distanceMiles) {
        this(truckId, destinationStore, distanceMiles, new ArrayList<>());
    }

    public Truck(int truckId, String destinationStore, int distanceMiles, List<String> cargo) {
        this.truckId = truckId;
        this.destinationStore = destinationStore;
        this.distanceMiles = distanceMiles;
        this.cargo = cargo;
    }

    public int getTruckId() {
        return truckId;
    }

    public String getDestinationStore() {
        return destinationStore;
    }

    public int getDistanceMiles() {
        return distanceMiles;
    }

    public List<String> getCargo() {
        return cargo;
    }

    public boolean addCargo(String item) {
        return cargo.add(item);
    }

    @Override
    public String toString() {
        return truckId + "," +
                destinationStore + "," +
                distanceMiles + "," +
                String.join(",", cargo);
    }
}
