/*
    Author: Dylan Rusch
*/
package service;

import model.Product;
import model.Truck;
import util.FileHandler;
import java.util.ArrayList;
import java.util.List;


public class TruckService {
    private static final String FILE_PATH = "data/trucks.txt";

    public void addTruck(Truck truck) {
        FileHandler.writeLine(FILE_PATH, truck.toString());
    }

    public void editTruck(Truck truck) {
        List<Truck> trucks = getAllTrucks();
        int replace = -1;
        for(int i = 0; i < trucks.size(); i++){
            if(trucks.get(i).getTruckId() == truck.getTruckId()){
                replace = i;
                break;
            }
        }
        if (replace != -1) {
            trucks.set(replace, truck);
            FileHandler.writeAllLines(FILE_PATH, trucks.stream().map(Truck::toString).toList());
        }
    }


    public List<Truck> getAllTrucks() {
        List<String> lines = FileHandler.readAllLines(FILE_PATH);
        List<Truck> trucks = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    String store = parts[1];
                    int distance = Integer.parseInt(parts[2]);
                    List<String> cargo = new ArrayList<>();

                    for (int i = 3; i < parts.length; i++) {
                        if (!parts[i].isBlank()) {
                            cargo.add(parts[i].trim());
                        }
                    }
                    trucks.add(new Truck(id, store, distance, cargo));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        }
        return trucks;
    }

    public int getNextTruckId() {
        List<Truck> trucks = getAllTrucks();
        if (trucks.isEmpty()) return 1;
        return trucks.get(trucks.size() - 1).getTruckId() + 1;
    }

    public Truck getTruckById(int id) {
        List<Truck> trucks = getAllTrucks();
        for (Truck truck : trucks) {
            if (truck.getTruckId() == id) {
                return truck;
            }
        }
        return null;
    }

    public boolean removeTruckById(int id) {
        List<Truck> trucks = getAllTrucks();
        boolean removed_truck = trucks.removeIf(truck -> truck.getTruckId() == id);
        if (removed_truck) {
            FileHandler.writeAllLines(FILE_PATH, trucks.stream().map(Truck::toString).toList());
        }
        return removed_truck;
    }

    public boolean addCargoToTruck(int truckId, String cargoItem) {
        Truck truck = getTruckById(truckId);
        if (truck == null) {
            System.out.println("No truck found");
            return false;
        }

        truck.getCargo().add(cargoItem);
        editTruck(truck);
        return true;
    }
}

