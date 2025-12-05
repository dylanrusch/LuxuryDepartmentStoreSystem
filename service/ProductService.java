/*
Author: Prakarsha Poudel
 */
package service;

import model.Product;
import util.FileHandler;
import java.util.ArrayList;
import java.util.List;
import util.PriceHistoryHandler;

public class ProductService {
    private static final String FILE_PATH = "data/products.txt";
    private static final String DISCONTINUED_FILEPATH = "data/discontinued.txt";
    private static int nextID = 101;

    public void addProduct(Product product) {
        String line = product.getId() + "," + product.getName() + "," +
                product.getCategory() + "," + product.getPrice() + "," + product.getBrand();
        FileHandler.writeLine(FILE_PATH, line);
    }

    public void editProduct(Product product) {
        List<Product> products = getAllProducts();
        int replace = -1;
        for(int i = 0; i < products.size(); i++){
            if(products.get(i).getId() == product.getId()){
                replace = i;
                break;
            }
        }
        if (replace != -1) {
            products.set(replace, product);
            FileHandler.writeAllLines(FILE_PATH, products.stream().map(Product::editString).toList());
        }
    }

    public void removeProduct(int productID) {
        List<Product> products = getAllProducts();
        int replace = -1;
        for(int i = 0; i < products.size(); i++){
            if(products.get(i).getId() == productID){
                replace = i;
                break;
            }
        }
        if (replace != -1) {
            products.remove(replace);
            FileHandler.writeAllLines(FILE_PATH, products.stream().map(Product::editString).toList());
        }
    }

    public void addToDiscontinued(Product product) {
        String line = product.getId() + "," + product.getName() + "," +
                product.getCategory() + "," + product.getPrice() + "," + product.getBrand();
        FileHandler.writeLine(DISCONTINUED_FILEPATH, line);
    }

    public Product getDiscontinuedProduct(int id) {
        List<Product> products = getAllDiscontinuedProducts();
        int replace = -1;
        for(int i = 0; i < products.size(); i++){
            if(products.get(i).getId() == id){
                replace = i;
                break;
            }
        }
        if (replace != -1) {
            return products.get(replace);
        }
        return null;
    }

    private List<Product> getAllDiscontinuedProducts() {
        List<String> lines = FileHandler.readAllLines(DISCONTINUED_FILEPATH);
        List<Product> products = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String category = parts[2];
                    double price = Double.parseDouble(parts[3]);
                    String brand = parts[4];
                    products.add(new Product(id, name, category, price, brand));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        }
        return products;
    }


    public void removeDiscontinuedProduct(int id) {
        List<Product> products = getAllDiscontinuedProducts();
        int replace = -1;
        for(int i = 0; i < products.size(); i++){
            if(products.get(i).getId() == id){
                replace = i;
                break;
            }
        }
        if (replace != -1) {
            products.remove(replace);
            FileHandler.writeAllLines(DISCONTINUED_FILEPATH, products.stream().map(Product::editString).toList());
        }
    }

    public List<Product> getAllProducts() {
        List<String> lines = FileHandler.readAllLines(FILE_PATH);
        List<Product> products = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String category = parts[2];
                    double price = Double.parseDouble(parts[3]);
                    String brand = parts[4];
                    products.add(new Product(id, name, category, price, brand));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        }
        return products;
    }

    public int getNextProductId() {
        List<Product> products = getAllProducts();
        List<Product> discontinued = getAllDiscontinuedProducts();
        int total = products.size() + discontinued.size();
        if (total == 0) return 101;
        return 101 + total;
    }

    // Get specific product by ID
    public Product getProductById(int id) {
        List<Product> products = getAllProducts();
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    // Calculate and apply discount to products.txt file
    public void discountProduct(Product product, Integer discountPercentage) {

        // Calculate discount from percentage
        double discountedPrice = product.getPrice() * (1 - discountPercentage / 100.0);

        double oldPrice = product.getPrice();

        product.setPrice(discountedPrice);

        // Add price change to price document
        PriceHistoryHandler.logPriceChange(product, oldPrice, discountedPrice);

        // Update the product in file
        editProduct(product);
    }

}
