// java
package service;

import model.Product;
import model.Store;
import model.StoreInventoryItem;
import util.FileHandler;
import java.util.ArrayList;
import java.util.List;


public class InventoryService {
    private static final String INVENTORY_FILE_PATH = "data/inventory.txt";

    public int getNextInventoryId() {
        List<StoreInventoryItem> items = getAllInventory();
        if (items.isEmpty()) return 1;
        return items.get(items.size() - 1).getId() + 1;
    }

    public void addInventoryItem(StoreInventoryItem item) {
        String line = item.getId() + "," + item.getProductId() + "," +
                      item.getStoreId() + "," + item.getQuantity() + "," +
                      (item.getPriceOverride() != null ? item.getPriceOverride() : "");
        FileHandler.writeLine(INVENTORY_FILE_PATH, line);
    }

    public List<StoreInventoryItem> getAllInventory() {
        List<String> lines = FileHandler.readAllLines(INVENTORY_FILE_PATH);
        List<StoreInventoryItem> inventory = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    int productId = Integer.parseInt(parts[1]);
                    int storeId = Integer.parseInt(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    Double priceOverride = (parts.length > 4 && !parts[4].isEmpty()) ?
                                           Double.parseDouble(parts[4]) : null;
                    inventory.add(new StoreInventoryItem(id, productId, storeId, quantity, priceOverride));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid inventory line: " + line);
                }
            }
        }
        return inventory;
    }

    public List<StoreInventoryItem> getAllInventoryForStore(int storeId) {
        List<StoreInventoryItem> result = new ArrayList<>();
        for (StoreInventoryItem it : getAllInventory()) {
            if (it.getStoreId() == storeId) result.add(it);
        }
        return result;
    }

    public StoreInventoryItem getByProductAndStore(int productId, int storeId) {
        for (StoreInventoryItem it : getAllInventory()) {
            if (it.getProductId() == productId && it.getStoreId() == storeId) return it;
        }
        return null;
    }

    public StoreInventoryItem getById(int inventoryId) {
        for (StoreInventoryItem it : getAllInventory()) {
            if (it.getId() == inventoryId) return it;
        }
        return null;
    }

    public boolean adjustQuantity(int inventoryId, int delta) {
        List<StoreInventoryItem> allInventory = getAllInventory();
        int index = -1;
        for (int i = 0; i < allInventory.size(); i++) {
            if (allInventory.get(i).getId() == inventoryId) {
                index = i;
                break;
            }
        }

        if (index == -1) return false;

        StoreInventoryItem item = allInventory.get(index);
        int newQty = item.getQuantity() + delta;
        if (newQty < 0) return false;

        item.setQuantity(newQty);
        allInventory.set(index, item);

        // Rewrite entire file
        List<String> lines = new ArrayList<>();
        for (StoreInventoryItem inv : allInventory) {
            lines.add(inv.getId() + "," + inv.getProductId() + "," +
                     inv.getStoreId() + "," + inv.getQuantity() + "," +
                     (inv.getPriceOverride() != null ? inv.getPriceOverride() : ""));
        }
        FileHandler.writeAllLines(INVENTORY_FILE_PATH, lines);
        return true;
    }

    public void updateInventoryItem(StoreInventoryItem item) {
        List<StoreInventoryItem> allInventory = getAllInventory();
        int index = -1;
        for (int i = 0; i < allInventory.size(); i++) {
            if (allInventory.get(i).getId() == item.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            allInventory.set(index, item);
            List<String> lines = new ArrayList<>();
            for (StoreInventoryItem inv : allInventory) {
                lines.add(inv.getId() + "," + inv.getProductId() + "," +
                         inv.getStoreId() + "," + inv.getQuantity() + "," +
                         (inv.getPriceOverride() != null ? inv.getPriceOverride() : ""));
            }
            FileHandler.writeAllLines(INVENTORY_FILE_PATH, lines);
        }
    }

    /*
        Author: Dylan Rusch (next 2 functions)
     */

    // Create a list of items whos quantity is below a certain threshold
    public List<StoreInventoryItem> getLowStockItems(int threshold) {
        List<StoreInventoryItem> lowStock = new ArrayList<>();
        for (StoreInventoryItem item : getAllInventory()) {
            if (item.getQuantity() <= threshold) {
                lowStock.add(item);
            }
        }
        return lowStock;
    }

    // Print all low stock items
    public void printLowStockItems(Store store, List<StoreInventoryItem> lowStockItems, ProductService productService) {
        System.out.println("\nStore: " + store.getName());
        boolean hasLowStock = false;

        for (StoreInventoryItem item : lowStockItems) {
            if (item.getStoreId() == store.getStoreId()) {
                Product p = productService.getProductById(item.getProductId());
                System.out.printf(" - %s (Qty: %d)%n", p.getName(), item.getQuantity());
                hasLowStock = true;
            }
        }

        if (!hasLowStock) {
            System.out.println("No low stock items at this store.");
        }
    }
}