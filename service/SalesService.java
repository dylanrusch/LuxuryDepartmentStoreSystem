package service;

import model.Sale;
import model.Store;
import util.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class SalesService {
    private static final String FILE_PATH = "data/sales.txt";

    // Append a sale record to the sales file
    public void recordSale(Sale sale) {
        if (sale == null) return;
        FileHandler.writeLine(FILE_PATH, sale.toString());
    }

    // Read all sales from the sales file
    public List<Sale> getAllSales() {
        List<String> lines = FileHandler.readAllLines(FILE_PATH);
        List<Sale> sales = new ArrayList<>();
        if (lines == null) return sales;
        for (String line : lines) {
            Sale s = Sale.fromLine(line);
            if (s != null) sales.add(s);
        }
        return sales;
    }

    // Return the next sale id (incremental)
    public int getNextSaleId() {
        List<Sale> sales = getAllSales();
        if (sales.isEmpty()) return 1;
        return sales.get(sales.size() - 1).getSaleId() + 1;
    }

    // Calculate total revenue from all sales
    public double calculateTotalRevenue() {
        List<Sale> sales = getAllSales();
        double total = 0.0;
        for (Sale s : sales) {
            total += s.getTotalPrice();
        }
        return total;
    }

    public Sale getSaleById(int saleId) {
        for (Sale s : getAllSales()) {
            if (s.getSaleId() == saleId) {
                return s;
            }
        }
        return null;
    }

    public void removeSale(int saleId) {
        List<String> lines = FileHandler.readAllLines(FILE_PATH);
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            Sale s = Sale.fromLine(line);
            if (s != null && s.getSaleId() != saleId) {
                updatedLines.add(line); // keep all sales except the one to remove
            }
        }

        FileHandler.writeAllLines(FILE_PATH, updatedLines);
        System.out.println("Sale with ID " + saleId + " removed (if it existed).");
    }

    public void printRevenueSummary(StoreService storeService, ProductService productService) {
        List<Sale> sales = getAllSales();
        List<Store> stores = storeService.getAllStores();

        double grandTotal = 0;
        System.out.println("\nStore | Revenue ($)");
        System.out.println("-------------------");

        for (Store s : stores) {
            double total = 0;
            for (Sale sale : sales) {
                if (sale.getStoreId() == s.getStoreId()) {
                    total += sale.getTotalPrice();
                }
            }
            System.out.printf("%s | %.2f%n", s.getName(), total);
            grandTotal += total;
        }
        System.out.println("-------------------");
        System.out.printf("Grand Total Revenue = $%.2f%n", grandTotal);
    }

}
