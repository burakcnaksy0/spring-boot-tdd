package com.burakcanaksoy.springboottdd.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {
    private String name;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private String description;
}
