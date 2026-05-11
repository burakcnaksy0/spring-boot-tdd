package com.burakcanaksoy.springboottdd.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
    }

    /*
    @PostMapping()
    public ResponseEntity<Product> createProduct(String name , String categoryName, BigDecimal price, Integer stock , String description){
        return new ResponseEntity<>(productService.create(name,categoryName,price,stock,description),HttpStatus.CREATED);
    }
     */


    @PostMapping()
    public ResponseEntity<Product> createProduct(@RequestBody ProductCreateRequest productCreateRequest){
        return new ResponseEntity<>(productService.create(productCreateRequest.getName(),
                productCreateRequest.getCategoryName(),productCreateRequest.getPrice(),productCreateRequest.getStock(),productCreateRequest.getDescription()),HttpStatus.CREATED);

    }


    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts(){
        return new ResponseEntity<>(productService.getAllProduct(),HttpStatus.OK);
    }
}
