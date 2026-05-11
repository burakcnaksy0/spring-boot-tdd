package com.burakcanaksoy.springboottdd.product;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Product {
    private Long id;
    private String name;
    private String categoryName;
    private BigDecimal price;
    private int numberOfStock;
    private String description;
    private boolean active;
}