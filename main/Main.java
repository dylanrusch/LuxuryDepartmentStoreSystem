/*
Author: Prakarsha Poudel
*/
package main;

import model.Product;
import service.ProductService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProductService productService = new ProductService();

        boolean running = true;

        System.out.println("===== Luxury Department Store Management System =====");

        while (running) {
            System.out.println("1. Add New Luxury Item");
            System.out.println("2. View All Items");
            System.out.println("3. Apply Discount");
            System.out.println("4. Quit");
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

                    // Quantity
                    int quantity;
                    while (true) {
                        System.out.print("Enter quantity: ");
                        try {
                            quantity = Integer.parseInt(sc.nextLine());
                            if (quantity >= 0) break;
                            System.out.println("Quantity cannot be negative.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity. Please enter a whole number.");
                        }
                    }

                    // Add product
                    Product product = new Product(id, name, brand, price, quantity);
                    productService.addProduct(product);
                    System.out.println("Product added successfully!");
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

                case 3 -> {
                    System.out.print("Enter item id: ");
                    int id = Integer.parseInt(sc.nextLine());
                    Product item = productService.getProductById(id);
                    if (item == null) {
                        System.out.println("Item not found.");
                        break;
                    }

                    System.out.println("Current price: $" + item.getPrice());
                    System.out.print("Enter discount percentage: ");
                    int discount = Integer.parseInt(sc.nextLine());
                    productService.discountProduct(item, discount);
                    System.out.println("New price: " + item.getPrice());
                }
                case 4 -> {
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
