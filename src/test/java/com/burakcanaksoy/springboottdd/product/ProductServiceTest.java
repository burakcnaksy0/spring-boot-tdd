package com.burakcanaksoy.springboottdd.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @AfterEach
    void tearDown() {
        productService.clearAll();
    }
}