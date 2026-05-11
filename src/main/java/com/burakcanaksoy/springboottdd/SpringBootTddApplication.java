package com.burakcanaksoy.springboottdd;

import com.burakcanaksoy.springboottdd.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringBootTddApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTddApplication.class, args);
    }

    private final ProductService productService;

    @Override
    public void run(String... args) {
        productService.create(
                "Laptop",
                "Electronics",
                new BigDecimal("25000"),
                10,
                "Gaming laptop"
        );

        productService.create(
                "iPhone 15",
                "Electronics",
                new BigDecimal("55000"),
                15,
                "Apple smartphone"
        );

        productService.create(
                "Mechanical Keyboard",
                "Accessories",
                new BigDecimal("3500"),
                30,
                "RGB mechanical keyboard"
        );

        productService.create(
                "Gaming Mouse",
                "Accessories",
                new BigDecimal("1800"),
                25,
                "Wireless gaming mouse"
        );

        productService.create(
                "Monitor 27 Inch",
                "Electronics",
                new BigDecimal("12000"),
                8,
                "2K IPS monitor"
        );

        productService.create(
                "Office Chair",
                "Furniture",
                new BigDecimal("7000"),
                12,
                "Ergonomic office chair"
        );

        productService.create(
                "Desk Lamp",
                "Furniture",
                new BigDecimal("900"),
                40,
                "LED desk lamp"
        );

        productService.create(
                "External SSD",
                "Storage",
                new BigDecimal("4500"),
                18,
                "1TB portable SSD"
        );
    }
}
