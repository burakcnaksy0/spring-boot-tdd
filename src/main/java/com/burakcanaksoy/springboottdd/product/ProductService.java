package com.burakcanaksoy.springboottdd.product;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private List<Product> productList = new ArrayList<>();
    private long countProduct = 1;

    public Product create(String name , String categoryName, BigDecimal price, Integer stock , String description ){
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        if (categoryName == null || categoryName.isBlank()){
            throw new IllegalArgumentException("Category name cannot be blank.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Price cannot be null or negative.");
        }
        if (stock == null || stock < 0){
            throw new IllegalArgumentException("Stock cannot be null or negative.");
        }
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("Description cannot be blank.");
        }
        Product product = new Product(countProduct++,name,categoryName,price,stock,description,true);
        productList.add(product);
        return product;
    }

    public Product getProductById(Long id){
        return productList.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: "+id));
    }

    public void deleteProduct(Long id){
        Product product = getProductById(id);
        productList.remove(product);
    }

    public List<Product> getAllProduct(){
        return new ArrayList<>(productList);
    }

    public List<Product> getActiveProducts(){
        return productList.stream()
                .filter(Product::isActive)
                .toList();
    }

    public Product deactivateProduct(Long id){
        Product product = getProductById(id);
        product.setActive(false);
        return product;
    }

    public List<Product> getProductsByCategory(String categoryName){
        return productList.stream()
                .filter(p -> p.getCategoryName().equalsIgnoreCase(categoryName))
                .toList();
    }

    public void clearAll(){
        productList.clear();
        countProduct = 1;
    }
}
