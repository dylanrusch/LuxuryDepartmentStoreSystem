/*
Author: Prakarsha Poudel
*/
package main;

import model.Product;
import model.Sale;
import model.Store;
import model.Employee;
import model.Role;
import model.Truck;
import model.StoreInventoryItem;
import service.ProductService;
import service.SalesService;
import service.StoreService;
import service.InventoryService;
import service.TruckService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import util.PriceHistoryHandler;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProductService productService = new ProductService();
        SalesService salesService = new SalesService();
        StoreService storeService = new StoreService();
        InventoryService inventoryService = new InventoryService();
        TruckService truckService = new TruckService();

        boolean running = true;

        System.out.println("===== Luxury Department Store Management System =====");

        while (running) {
            System.out.println("1. Add New Luxury Item");
            System.out.println("2. View All Items");
            System.out.println("3. Edit An Item");
            System.out.println("4. Purchase Item");
            System.out.println("5. View Low Stock Items");
            System.out.println("6. Apply Discount");
            System.out.println("7. View Sales");
            System.out.println("8. View Product Price History");
            System.out.println("9. Manage Stores");
            System.out.println("10. Manage Employees");
            System.out.println("11. Manage Inventory");
            System.out.println("12. Manage Trucks");
            System.out.println("13. Search Product by Name/Brand");
            System.out.println("14. Void a Transaction");
            System.out.println("15. Quit");
            System.out.print("Select option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // clear buffer
            switch (choice) {
                case 1 -> {
                    int id = productService.getNextProductId();

                    // Item name
                    String name;
                    while (true) {
                        System.out.print("Enter item name: ");
                        name = sc.nextLine().trim();
                        if (!name.isEmpty()) break;
                        System.out.println("Item name cannot be empty.");
                    }

                    // Brand
                    String brand;
                    while (true) {
                        System.out.print("Enter brand: ");
                        brand = sc.nextLine().trim();
                        if (!brand.isEmpty()) break;
                        System.out.println("Brand cannot be empty.");
                    }

                    // Price
                    double price;
                    while (true) {
                        System.out.print("Enter price: ");
                        try {
                            price = Double.parseDouble(sc.nextLine());
                            if (price >= 0) break;
                            System.out.println("Price cannot be negative.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid price. Please enter a number.");
                        }
                    }

                    // Add product
                    Product product = new Product(id, name, brand, price, brand);
                    productService.addProduct(product);
                    System.out.println("Product added successfully! Product ID: " + id);
                }

                case 2 -> {
                    System.out.println("\nAvailable Items:");
                    var products = productService.getAllProducts();
                    if (products.isEmpty()) {
                        System.out.println("No products found.");
                    } else {
                        products.forEach(System.out::println);
                    }
                }

                case 3-> {
                    System.out.println("\n===== Edit an Item =====");
                    System.out.print("Enter item id to edit: ");
                    int id;
                    try {
                        id = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid item ID.");
                        break;
                    }

                    Product item = productService.getProductById(id);
                    if (item == null) {
                        System.out.println("Product not found.");
                        break;
                    }
                    System.out.println(item.editString());
                    System.out.println("Please enter data in same format as above ^^^");
                    System.out.println("Enter the updated information below:");
                    Scanner editItem = new Scanner(System.in);
                    String newText = editItem.nextLine();
                    String[] split = newText.split(",");

                    // Get old price before change for logging
                    double oldPrice = item.getPrice();

                    Product newItem = new Product(item.getId(),split[1],split[2],Double.parseDouble(split[3]), split[4]);

                    // Get new price after change for logging
                    double newPrice = newItem.getPrice();

                    productService.editProduct(newItem);

                    // Log price change in price_history file
                    PriceHistoryHandler.logPriceChange(item, oldPrice, newPrice);
                }


                case 4 -> {
                    // Purchase Item - select store and purchase from its inventory
                    System.out.println("\n===== Purchase Item =====");

                    var stores = storeService.getAllStores();
                    if (stores.isEmpty()) {
                        System.out.println("No stores available.");
                        break;
                    }

                    System.out.println("Available Stores:");
                    stores.forEach(System.out::println);

                    System.out.print("\nSelect store ID for purchase: ");
                    int storeId;
                    try {
                        storeId = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid store ID.");
                        break;
                    }

                    Store store = storeService.getStoreById(storeId);
                    if (store == null) {
                        System.out.println("Store not found.");
                        break;
                    }

                    var inventory = inventoryService.getAllInventoryForStore(storeId);
                    if (inventory.isEmpty()) {
                        System.out.println("No items available at this store.");
                        break;
                    }

                    System.out.println("\n===== Available Items at " + store.getName() + " =====");
                    for (StoreInventoryItem inv : inventory) {
                        if (inv.getQuantity() > 0) {
                            Product p = productService.getProductById(inv.getProductId());
                            double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();
                            System.out.printf("InvID:%d | %s - %s | Price: $%.2f | In Stock: %d%n",
                                    inv.getId(), p.getName(), p.getCategory(), effectivePrice, inv.getQuantity());
                        }
                    }

                    // Shopping cart
                    java.util.List<StoreInventoryItem> cart = new java.util.ArrayList<>();
                    java.util.List<Integer> quantities = new java.util.ArrayList<>();

                    boolean addingItems = true;
                    while (addingItems) {
                        System.out.print("\nEnter inventory ID to add to cart (or 0 to checkout): ");
                        int invId;
                        try {
                            invId = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid inventory ID.");
                            continue;
                        }

                        if (invId == 0) {
                            if (cart.isEmpty()) {
                                System.out.println("Cart is empty. Purchase cancelled.");
                            }
                            addingItems = false;
                            continue;
                        }

                        StoreInventoryItem invItem = inventoryService.getById(invId);
                        if (invItem == null || invItem.getStoreId() != storeId) {
                            System.out.println("Inventory item not found at this store.");
                            continue;
                        }

                        if (invItem.getQuantity() == 0) {
                            System.out.println("This item is out of stock.");
                            continue;
                        }

                        Product product = productService.getProductById(invItem.getProductId());
                        double effectivePrice = invItem.getPriceOverride() != null ? invItem.getPriceOverride() : product.getPrice();

                        System.out.println("Selected: " + product.getName() + " - " + product.getCategory());
                        System.out.println("Price: $" + String.format("%.2f", effectivePrice));
                        System.out.println("Available quantity: " + invItem.getQuantity());

                        System.out.print("Enter quantity to purchase: ");
                        int qty;
                        try {
                            qty = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity.");
                            continue;
                        }

                        if (qty <= 0) {
                            System.out.println("Quantity must be positive.");
                            continue;
                        }

                        if (qty > invItem.getQuantity()) {
                            System.out.println("Insufficient stock. Only " + invItem.getQuantity() + " available.");
                            continue;
                        }

                        cart.add(invItem);
                        quantities.add(qty);
                        System.out.println("✓ Added to cart: " + qty + "x " + product.getName());
                    }

                    if (cart.isEmpty()) {
                        break;
                    }

                    // Calculate totals
                    double subtotal = 0.0;
                    System.out.println("\n--- Cart Summary ---");
                    for (int i = 0; i < cart.size(); i++) {
                        StoreInventoryItem inv = cart.get(i);
                        Product p = productService.getProductById(inv.getProductId());
                        int q = quantities.get(i);
                        double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();
                        double itemTotal = effectivePrice * q;
                        subtotal += itemTotal;
                        System.out.printf("%dx %s - %s @ $%.2f = $%.2f%n",
                                q, p.getName(), p.getCategory(), effectivePrice, itemTotal);
                    }

                    double tax = subtotal * 0.07; // 7% tax
                    double totalWithTax = subtotal + tax;

                    System.out.println("----------------------------");
                    System.out.println("Subtotal: $" + String.format("%.2f", subtotal));
                    System.out.println("Tax (7%): $" + String.format("%.2f", tax));
                    System.out.println("----------------------------");
                    System.out.println("TOTAL: $" + String.format("%.2f", totalWithTax));

                    System.out.print("\nConfirm purchase? (y/n): ");
                    String confirm = sc.nextLine().trim().toLowerCase();
                    if (!confirm.equals("y") && !confirm.equals("yes")) {
                        System.out.println("Purchase cancelled.");
                        break;
                    }

                    // Process purchase
                    boolean allSuccessful = true;
                    for (int i = 0; i < cart.size(); i++) {
                        StoreInventoryItem inv = cart.get(i);
                        Product p = productService.getProductById(inv.getProductId());
                        int q = quantities.get(i);
                        double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();

                        // Reduce inventory
                        if (inventoryService.adjustQuantity(inv.getId(), -q)) {
                            // Record sale
                            int saleId = salesService.getNextSaleId();
                            Sale sale = new Sale(saleId, inv.getProductId(), p.getName(), q, effectivePrice, store.getStoreId());
                            salesService.recordSale(sale);
                        } else {
                            System.out.println("Failed to process: " + p.getName());
                            allSuccessful = false;
                        }
                    }

                    // Print receipt
                    if (allSuccessful) {
                        System.out.println("\n========== RECEIPT ==========");
                        System.out.println("Store: " + store.getName());
                        System.out.println("Date: " + java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        System.out.println("----------------------------");

                        for (int i = 0; i < cart.size(); i++) {
                            StoreInventoryItem inv = cart.get(i);
                            Product p = productService.getProductById(inv.getProductId());
                            int q = quantities.get(i);
                            double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();
                            System.out.printf("%dx %s @ $%.2f = $%.2f%n",
                                    q, p.getName(), effectivePrice, effectivePrice * q);
                        }

                        System.out.println("----------------------------");
                        System.out.println("Subtotal: $" + String.format("%.2f", subtotal));
                        System.out.println("Tax (7%): $" + String.format("%.2f", tax));
                        System.out.println("----------------------------");
                        System.out.println("TOTAL: $" + String.format("%.2f", totalWithTax));
                        System.out.println("============================");
                        System.out.println("Payment recorded. Thank you!");
                        System.out.println("Inventory updated.");
                    } else {
                        System.out.println("\nNote: Some items could not be processed.");
                    }
                }
                case 5 -> {
                    System.out.println("\n===== Low Stock Alerts =====");
                    //System.out.println("1. All Stores");
                    //System.out.println("2. By Specific Store");
                    //System.out.println("Select Option: ");
                    //int option = sc.nextInt();


                    System.out.print("Enter quantity threshold: ");
                    int threshold;
                    try {
                        threshold = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }

                    if (threshold < 0) {
                        System.out.println("Threshold cannot be negative.");
                        break;
                    }

                    // Fetch low stock items list
                    var lowStockItems = inventoryService.getLowStockItems(threshold);

                    if (lowStockItems.isEmpty()) {
                        System.out.println("No low stock items.");
                    }
                    else {
                        for (StoreInventoryItem item : lowStockItems) {
                            Product p = productService.getProductById(item.getProductId());
                            System.out.printf("%s - Qty: %d%n", p.getName(), item.getQuantity());
                        }
                    }
                }

                case 6 -> {
                    System.out.print("Enter item id: ");
                    int id;
                    try {
                        id = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }

                    Product item = productService.getProductById(id);
                    if (item == null) {
                        System.out.println("Item not found.");
                        break;
                    }

                    System.out.println("Current price: $" + item.getPrice());
                    System.out.print("Enter discount percentage: ");
                    int discount;
                    try {
                        discount = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }

                    if (discount < 0) {
                        System.out.println("Discount cannot be negative.");
                        break;
                    }

                    if (discount > 100) {
                        System.out.println("Discount cannot be greater than 100%.");
                        break;
                    }

                    productService.discountProduct(item, discount);
                    System.out.println("New price: $" + item.getPrice());
                }

                case 7 -> {
                    System.out.println("\n===== Sales Records =====");
                    var sales = salesService.getAllSales();
                    if (sales.isEmpty()) {
                        System.out.println("No sales yet.");
                    } else {
                        sales.forEach(s -> System.out.println(s.toString()));
                        System.out.println("\n--- Total Revenue ---");
                        double totalRevenue = salesService.calculateTotalRevenue();
                        System.out.println("Total Revenue: $" + String.format("%.2f", totalRevenue));
                    }
                }

                case 8 -> {
                    System.out.println("\n===== Product Price History =====");
                    System.out.print("Enter item id: ");
                    int id;
                    try {
                        id = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }

                    Product item = productService.getProductById(id);
                    if (item == null) {
                        System.out.println("Item not found.");
                        break;
                    }

                    // Print price history for given product id
                    System.out.println("\n=== Price Change History for '" + item.getBrand() + " " + item.getName() + "' ===");
                    PriceHistoryHandler.printPriceHistoryForProduct(item.getId());

                }

                case 9 -> {
                    // Manage Stores
                    System.out.println("\n===== Manage Stores =====");
                    System.out.println("1. View All Stores");
                    System.out.println("2. Add New Store");
                    System.out.println("3. Back to Main Menu");
                    System.out.print("Select option: ");
                    int storeChoice = sc.nextInt();
                    sc.nextLine();

                    switch (storeChoice) {
                        case 1 -> {
                            // View All Stores
                            System.out.println("\n===== All Stores =====");
                            var stores = storeService.getAllStores();
                            if (stores.isEmpty()) {
                                System.out.println("No stores found.");
                            } else {
                                stores.forEach(System.out::println);
                            }
                        }
                        case 2 -> {
                            // Add Store
                            System.out.println("\n===== Add Store =====");
                            int storeId = storeService.getNextStoreId();

                            String storeName;
                            while (true) {
                                System.out.print("Enter store name: ");
                                storeName = sc.nextLine().trim();
                                if (!storeName.isEmpty()) break;
                                System.out.println("Store name cannot be empty.");
                            }

                            String location;
                            while (true) {
                                System.out.print("Enter store location: ");
                                location = sc.nextLine().trim();
                                if (!location.isEmpty()) break;
                                System.out.println("Location cannot be empty.");
                            }

                            System.out.print("Is store active? (true/false): ");
                            boolean active = Boolean.parseBoolean(sc.nextLine());

                            Store store = new Store(storeId, storeName, location, active);
                            storeService.addStore(store);
                            System.out.println("Store added successfully! Store ID: " + storeId);
                        }
                        case 3 -> {
                            // Back to main menu
                        }
                        default -> System.out.println("Invalid option.");
                    }
                }

                case 10 -> {
                    // Manage Employees
                    System.out.println("\n===== Manage Employees =====");
                    System.out.println("1. View All Employees");
                    System.out.println("2. Add New Employee");
                    System.out.println("3. Assign Employee to Store");
                    System.out.println("4. Back to Main Menu");
                    System.out.print("Select option: ");
                    int empChoice = sc.nextInt();
                    sc.nextLine();

                    switch (empChoice) {
                        case 1 -> {
                            // View All Employees
                            System.out.println("\n===== All Employees =====");
                            var employees = storeService.getAllEmployees();
                            if (employees.isEmpty()) {
                                System.out.println("No employees found.");
                            } else {
                                employees.forEach(System.out::println);
                            }
                        }
                        case 2 -> {
                            // Add Employee
                            System.out.println("\n===== Add Employee =====");
                            int empId = storeService.getNextEmployeeId();

                            String empName;
                            while (true) {
                                System.out.print("Enter employee name: ");
                                empName = sc.nextLine().trim();
                                if (!empName.isEmpty()) break;
                                System.out.println("Employee name cannot be empty.");
                            }

                            System.out.println("Available roles: CASHIER, MANAGER, ADMIN, STOCKER, CUSTOMER_SERVICE, TEAM_LEAD");
                            Role role;
                            while (true) {
                                System.out.print("Enter role: ");
                                String roleStr = sc.nextLine().trim().toUpperCase();
                                role = Role.fromString(roleStr);
                                if (role != null) break;
                                System.out.println("Invalid role. Please try again.");
                            }

                            double salary;
                            while (true) {
                                System.out.print("Enter yearly salary: ");
                                try {
                                    salary = Double.parseDouble(sc.nextLine());
                                    if (salary >= 0) break;
                                    System.out.println("Salary cannot be negative.");
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid salary. Please enter a number.");
                                }
                            }

                            System.out.print("Is employee active? (true/false): ");
                            boolean empActive = Boolean.parseBoolean(sc.nextLine());

                            // Option to assign to store during creation
                            int empStoreId = 0; // Default to unassigned
                            var availableStores = storeService.getAllStores();
                            if (!availableStores.isEmpty()) {
                                System.out.print("\nAssign to a store now? (y/n): ");
                                String assignNow = sc.nextLine().trim().toLowerCase();
                                if (assignNow.equals("y") || assignNow.equals("yes")) {
                                    System.out.println("\nAvailable Stores:");
                                    availableStores.forEach(System.out::println);

                                    while (true) {
                                        System.out.print("Enter store ID (or 0 to skip): ");
                                        try {
                                            empStoreId = Integer.parseInt(sc.nextLine());
                                            if (empStoreId == 0) break; // Skip assignment
                                            Store selectedStore = storeService.getStoreById(empStoreId);
                                            if (selectedStore != null) break;
                                            System.out.println("Store not found. Please try again.");
                                        } catch (NumberFormatException e) {
                                            System.out.println("Invalid input. Please enter a number.");
                                        }
                                    }
                                }
                            }

                            Employee employee = new Employee(empId, empName, role, salary, empActive, empStoreId);
                            storeService.addEmployee(employee);

                            if (empStoreId > 0) {
                                Store assignedStore = storeService.getStoreById(empStoreId);
                                System.out.println("Employee added successfully! Employee ID: " + empId +
                                                 " - Assigned to store: " + assignedStore.getName());
                            } else {
                                System.out.println("Employee added successfully! Employee ID: " + empId + " - Not assigned to any store");
                            }
                        }
                        case 3 -> {
                            // Assign Employee to Store
                            System.out.println("\n===== Assign Employee to Store =====");

                            var employees = storeService.getAllEmployees();
                            if (employees.isEmpty()) {
                                System.out.println("No employees found.");
                                break;
                            }

                            System.out.println("\nAll Employees:");
                            employees.forEach(System.out::println);

                            System.out.print("\nEnter employee ID: ");
                            int empIdToAssign;
                            try {
                                empIdToAssign = Integer.parseInt(sc.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid employee ID.");
                                break;
                            }

                            Employee employeeToAssign = storeService.getEmployeeById(empIdToAssign);
                            if (employeeToAssign == null) {
                                System.out.println("Employee not found.");
                                break;
                            }

                            var stores = storeService.getAllStores();
                            if (stores.isEmpty()) {
                                System.out.println("No stores found. Please create a store first.");
                                break;
                            }

                            System.out.println("\nAll Stores:");
                            stores.forEach(System.out::println);

                            System.out.print("\nEnter store ID to assign employee to: ");
                            int storeIdToAssign;
                            try {
                                storeIdToAssign = Integer.parseInt(sc.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid store ID.");
                                break;
                            }

                            Store storeToAssign = storeService.getStoreById(storeIdToAssign);
                            if (storeToAssign == null) {
                                System.out.println("Store not found.");
                                break;
                            }

                            employeeToAssign.setStoreId(storeIdToAssign);
                            storeService.editEmployee(employeeToAssign);
                            if (employeeToAssign.getRole() == Role.MANAGER) {
                                storeService.assignManagerToStore(storeIdToAssign, empIdToAssign);
                                System.out.println("Employee '" + employeeToAssign.getName() + "' assigned as MANAGER to store '" + storeToAssign.getName() + "' successfully!");
                            } else {
                                System.out.println("Employee '" + employeeToAssign.getName() + "' assigned to store '" + storeToAssign.getName() + "' successfully!");
                            }
                            System.out.println("Employee '" + employeeToAssign.getName() + "' assigned to store '" + storeToAssign.getName() + "' successfully!");
                        }
                        case 4 -> {
                            // Back to main menu
                        }
                        default -> System.out.println("Invalid option.");
                    }
                }

                case 11 -> {
                    // Manage Inventory
                    System.out.println("\n===== Manage Inventory =====");
                    System.out.println("1. View All Inventory");
                    System.out.println("2. View Inventory by Store");
                    //System.out.println("3. Add Stock to Store");
                    System.out.println("4. Back to Main Menu");
                    System.out.print("Select option: ");
                    int invChoice = sc.nextInt();
                    sc.nextLine();

                    switch (invChoice) {
                        case 1 -> {
                            // View All Inventory
                            System.out.println("\n===== All Inventory =====");
                            var inventory = inventoryService.getAllInventory();
                            if (inventory.isEmpty()) {
                                System.out.println("No inventory found.");
                            } else {
                                for (StoreInventoryItem inv : inventory) {
                                    Product p = productService.getProductById(inv.getProductId());
                                    Store s = storeService.getStoreById(inv.getStoreId());
                                    double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();
                                    System.out.printf("InvID:%d | %s - %s | Store: %s | Qty: %d | Price: $%.2f%n",
                                            inv.getId(), p.getName(), p.getCategory(), s.getName(), inv.getQuantity(), effectivePrice);
                                }
                            }
                        }
                        case 2 -> {
                            // View Inventory by Store
                            var stores = storeService.getAllStores();
                            if (stores.isEmpty()) {
                                System.out.println("No stores found.");
                                break;
                            }

                            System.out.println("\nAvailable Stores:");
                            stores.forEach(System.out::println);

                            System.out.print("\nEnter store ID: ");
                            int storeId;
                            try {
                                storeId = Integer.parseInt(sc.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid store ID.");
                                break;
                            }

                            Store store = storeService.getStoreById(storeId);
                            if (store == null) {
                                System.out.println("Store not found.");
                                break;
                            }

                            System.out.println("\n===== Inventory for " + store.getName() + " =====");
                            var inventory = inventoryService.getAllInventoryForStore(storeId);
                            if (inventory.isEmpty()) {
                                System.out.println("No inventory at this store.");
                            } else {
                                for (StoreInventoryItem inv : inventory) {
                                    Product p = productService.getProductById(inv.getProductId());
                                    double effectivePrice = inv.getPriceOverride() != null ? inv.getPriceOverride() : p.getPrice();
                                    System.out.printf("InvID:%d | %s - %s | Qty: %d | Price: $%.2f%n",
                                            inv.getId(), p.getName(), p.getCategory(), inv.getQuantity(), effectivePrice);
                                }
                            }
                        }
                        case 3 -> {
                            // Add Stock to Store
//                            var products = productService.getAllProducts();
//                            if (products.isEmpty()) {
//                                System.out.println("No products in catalog. Add products first.");
//                                break;
//                            }
//
//                            var stores = storeService.getAllStores();
//                            if (stores.isEmpty()) {
//                                System.out.println("No stores found. Add stores first.");
//                                break;
//                            }
//
//                            System.out.println("\nAvailable Products:");
//                            products.forEach(System.out::println);
//
//                            System.out.print("\nEnter product ID: ");
//                            int productId;
//                            try {
//                                productId = Integer.parseInt(sc.nextLine());
//                            } catch (NumberFormatException e) {
//                                System.out.println("Invalid product ID.");
//                                break;
//                            }
//
//                            Product product = productService.getProductById(productId);
//                            if (product == null) {
//                                System.out.println("Product not found.");
//                                break;
//                            }
//
//                            System.out.println("\nAvailable Stores:");
//                            stores.forEach(System.out::println);
//
//                            System.out.print("\nEnter store ID: ");
//                            int storeId;
//                            try {
//                                storeId = Integer.parseInt(sc.nextLine());
//                            } catch (NumberFormatException e) {
//                                System.out.println("Invalid store ID.");
//                                break;
//                            }
//
//                            Store store = storeService.getStoreById(storeId);
//                            if (store == null) {
//                                System.out.println("Store not found.");
//                                break;
//                            }

                            // Check if inventory item already exists
//                            StoreInventoryItem existing = inventoryService.getByProductAndStore(productId, storeId);
//                            if (existing != null) {
//                                System.out.println("This product already has inventory at this store.");
//                                System.out.println("Current quantity: " + existing.getQuantity());
//                                System.out.print("Add more quantity? (y/n): ");
//                                String confirm = sc.nextLine().trim().toLowerCase();
//                                if (confirm.equals("y") || confirm.equals("yes")) {
//                                    System.out.print("Enter quantity to add: ");
//                                    int addQty;
//                                    try {
//                                        addQty = Integer.parseInt(sc.nextLine());
//                                        if (addQty > 0) {
//                                            inventoryService.adjustQuantity(existing.getId(), addQty);
//                                            System.out.println("Quantity added successfully! New quantity: " + (existing.getQuantity() + addQty));
//                                        } else {
//                                            System.out.println("Quantity must be positive.");
//                                        }
//                                    } catch (NumberFormatException e) {
//                                        System.out.println("Invalid quantity.");
//                                    }
//                                }
//                                break;
//                            }
//
//                            System.out.print("Enter quantity: ");
//                            int quantity;
//                            try {
//                                quantity = Integer.parseInt(sc.nextLine());
//                                if (quantity < 0) {
//                                    System.out.println("Quantity cannot be negative.");
//                                    break;
//                                }
//                            } catch (NumberFormatException e) {
//                                System.out.println("Invalid quantity.");
//                                break;
//                            }
//
//                            System.out.print("Override price for this store? (y/n): ");
//                            String overrideChoice = sc.nextLine().trim().toLowerCase();
//                            Double priceOverride = null;
//                            if (overrideChoice.equals("y") || overrideChoice.equals("yes")) {
//                                System.out.print("Enter store-specific price: ");
//                                try {
//                                    priceOverride = Double.parseDouble(sc.nextLine());
//                                } catch (NumberFormatException e) {
//                                    System.out.println("Invalid price. Using catalog price.");
//                                }
//                            }
//
//                            int invId = inventoryService.getNextInventoryId();
//                            StoreInventoryItem newInv = new StoreInventoryItem(invId, productId, storeId, quantity, priceOverride);
//                            inventoryService.addInventoryItem(newInv);
//                            System.out.println("Inventory added successfully! Inventory ID: " + invId);
                        }
                        case 4 -> {
                            // Back to main menu
                        }
                        default -> System.out.println("Invalid option.");
                    }
                }

                case 12 -> {
                    System.out.println("\n===== Manage Trucks =====");
                    System.out.println("1. View All Trucks");
                    System.out.println("2. Add New Truck");
                    System.out.println("3. Back to Main Menu");
                    System.out.print("Select option: ");
                    int truckChoice = sc.nextInt();
                    sc.nextLine();

                    switch (truckChoice) {
                        case 1 -> {
                            // View all trucks
                            System.out.println("\n===== All Trucks =====");
                            var trucks = truckService.getAllTrucks();
                            if (trucks.isEmpty()) {
                                System.out.println("No trucks found.");
                            }
                            else {
                                for (Truck truck : trucks) {
                                    String cargoSummary;
                                    if (truck.getCargo().isEmpty()) {
                                        cargoSummary = "No items";
                                    }
                                    else {
                                        cargoSummary = String.join(", ", truck.getCargo());
                                    }

                                    System.out.printf("TruckID: %d | Destination: %s | Distance: %d miles \n -> Cargo: %s%n",
                                            truck.getTruckId(),
                                            truck.getDestinationStore(),
                                            truck.getDistanceMiles(),
                                            cargoSummary);
                                }
                            }
                        }

                        case 2 -> {
                            // Add new truck
                            System.out.println("\n===== Add New Truck =====");
                            int truckId = truckService.getNextTruckId();

                            // Select store
                            var stores = storeService.getAllStores();
                            if (stores.isEmpty()) {
                                System.out.println("No stores found.");
                                break;
                            }

                            System.out.println("\nAvailable Stores:");
                            stores.forEach(System.out::println);

                            int storeId;
                            while (true) {
                                System.out.println("Enter store ID to assign truck to: ");
                                try {
                                    storeId = Integer.parseInt(sc.nextLine());
                                    if (storeService.getStoreById(storeId) == null) {
                                        System.out.println("Store not found.");
                                    }
                                    break;
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input. Please enter a number.");
                                }
                            }

                            Store store = storeService.getStoreById(storeId);

                            // Distance from store
                            int distance;

                            while (true) {
                                System.out.println("Please enter truck distance from store (in miles): ");
                                try {
                                    distance = Integer.parseInt(sc.nextLine());
                                    if (distance <= 0) {
                                        System.out.println("Distance cannot be negative.");
                                    }
                                    break;
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input. Please enter a number.");
                                }
                            }

                            // Create truck
                            Truck truck = new Truck(truckId, store.getName(), distance);

                            // Add cargo
                           var products = productService.getAllProducts();
                           if (!products.isEmpty()) {
                               System.out.println("\nAdd Cargo to Truck:");
                               products.forEach(p -> System.out.println(p.getId() + ": " + p.getName() + " (" + p.getBrand() + ")"));
                           }

                           while (true) {
                               System.out.println("Please enter product ID to add product (0 to finish)");
                               int productId;
                               try {
                                   productId = Integer.parseInt(sc.nextLine());

                                   if (productId == 0) {
                                       break;
                                   }
                                   Product product = productService.getProductById(productId);
                                   if (product == null) {
                                       System.out.println("Product not found.");
                                   } else {
                                       int quantity;
                                       while (true) {
                                           System.out.println("Enter quantity for " + product.getName() + ": ");
                                           try {
                                               quantity = Integer.parseInt(sc.nextLine());
                                               if (quantity <= 0) {
                                                   System.out.println("Quantity cannot be negative.");
                                               }
                                               break;
                                           } catch (NumberFormatException e) {
                                               System.out.println("Invalid input. Please enter a number.");
                                           }
                                       }
                                       String cargoItem = product.getName() + " (" + product.getBrand() + ") x" + quantity ;
                                       truck.addCargo(cargoItem);
                                       System.out.println("Added " + cargoItem + " to truck");
                                   }
                               } catch (NumberFormatException e) {
                                   System.out.println("Invalid input. Please enter a number.");
                               }
                           }

                            // Create truck and assign to store
                            truckService.addTruck(truck);

                            System.out.println("Truck added successfully!");
                        }
                        case 3 -> {
                            // Back to main menu
                        }

                    }
                }

                case 13 -> {
                    System.out.println("\n===== Search Product by Name/Brand =====");

                    String keyword;
                    while (true) {
                        System.out.print("Enter keyword: ");
                        keyword = sc.nextLine().trim().toLowerCase();
                        if (!keyword.isEmpty()) break; // valid input
                        System.out.println("Search term cannot be empty.");
                    }

                    List<Product> products;
                    try {
                        products = productService.getAllProducts();
                    } catch (Exception e) {
                        System.out.println("Error reading product data. Please try again.");
                        break; // abort search and return to main menu
                    }

                    if (products.isEmpty()) {
                        System.out.println("No matching products found.");
                        break;
                    }

                    boolean found = false;
                    System.out.println("Products matching keyword \"" + keyword + "\":");
                    for (Product p : products) {
                        if (p.getName().toLowerCase().contains(keyword) || p.getBrand().toLowerCase().contains(keyword)) {
                            System.out.println(p);
                            found = true;
                        }
                    }

                    if (!found) {
                        System.out.println("No matching products found.");
                    } else {
                        System.out.println("Search completed. Results displayed above.");
                    }
                }
                
                case 14 -> {
                    System.out.println("\n===== Void a Transaction =====");

                    var sales = salesService.getAllSales();
                    if (sales.isEmpty()) {
                        System.out.println("No sales to void.");
                        break;
                    }

                    System.out.println("\nExisting Sales:");
                    sales.forEach(System.out::println);

                    System.out.print("\nEnter Sale ID to void: ");
                    int saleId;
                    try {
                        saleId = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Sale ID.");
                        break;
                    }

                    Sale sale = salesService.getSaleById(saleId);
                    if (sale == null) {
                        System.out.println("Sale not found.");
                        break;
                    }

                    // Remove sale
                    salesService.removeSale(saleId);
                    System.out.println("✓ Transaction voided successfully.");
                }


                case 15 -> {
                    System.out.print("=== Goodbye ===");
                    running = false; // End program
                }

                default -> System.out.println("Invalid option. Please try again.");
            }
            System.out.println("\nPress any key to continue...");
            sc.nextLine();
        }
        sc.close();
    }
}