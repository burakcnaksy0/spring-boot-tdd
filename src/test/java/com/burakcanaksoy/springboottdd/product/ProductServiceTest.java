package com.burakcanaksoy.springboottdd.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        String name = "Laptop";
        String category = "Electronics";
        BigDecimal price = new BigDecimal("1500.00");
        Integer stock = 10;
        String description = "High performance laptop";

        // When
        Product product = productService.create(name, category, price, stock, description);

        // Then (AssertJ)
        assertThat(product).isNotNull();
        assertThat(product.getId()).isPositive();
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getPrice()).isEqualByComparingTo("1500.00"); // BigDecimal comparison
        assertThat(product.isActive()).isTrue();
        
        // Using hasFieldOrPropertyWithValue
        assertThat(product).hasFieldOrPropertyWithValue("categoryName", category);
        assertThat(product).hasFieldOrPropertyWithValue("stock", stock);
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        // When & Then
        assertThatThrownBy(() -> productService.create("", "Category", BigDecimal.TEN, 5, "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be blank.");
                
        // Alternative syntax
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.create(null, "Category", BigDecimal.TEN, 5, "Desc"))
                .withMessage("Name cannot be blank.");
    }

    @Test
    void shouldThrowExceptionWhenPriceIsNegative() {
        assertThatThrownBy(() -> productService.create("Name", "Category", new BigDecimal("-1"), 5, "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");
    }

    @Test
    void shouldGetProductById() {
        // Given
        Product created = productService.create("Phone", "Electronics", BigDecimal.valueOf(800), 20, "Smartphone");

        // When
        Product found = productService.getProductById(created.getId());

        // Then
        assertThat(found).isNotNull()
                .isEqualTo(created) // Checks object equality
                .isSameAs(created); // Checks reference equality
                
        assertThat(found.getName()).startsWith("Pho").endsWith("ne");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldGetAllProducts() {
        // Given
        productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        productService.create("P2", "C2", BigDecimal.TEN, 5, "D2");

        // When
        List<Product> products = productService.getAllProduct();

        // Then
        assertThat(products).isNotEmpty()
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactly("P1", "P2"); // Order matters
                
        assertThat(products).extracting("categoryName")
                .contains("C1", "C2");
    }

    @Test
    void shouldGetActiveProducts() {
        // Given
        Product p1 = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        Product p2 = productService.create("P2", "C1", BigDecimal.TEN, 5, "D2");
        
        productService.deactivateProduct(p1.getId());

        // When
        List<Product> activeProducts = productService.getActiveProducts();

        // Then
        assertThat(activeProducts).hasSize(1)
                .containsOnly(p2)
                .doesNotContain(p1);
                
        assertThat(activeProducts).allSatisfy(product -> {
            assertThat(product.isActive()).isTrue();
            assertThat(product.getName()).isEqualTo("P2");
        });
    }

    @Test
    void shouldDeactivateProduct() {
        // Given
        Product product = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");

        // When
        Product deactivated = productService.deactivateProduct(product.getId());

        // Then
        assertThat(deactivated.isActive()).isFalse();
        
        // Let's check if it's updated in the list
        Product fetched = productService.getProductById(product.getId());
        assertThat(fetched.isActive()).isFalse();
    }

    @Test
    void shouldGetProductsByCategory() {
        // Given
        productService.create("Laptop", "Electronics", BigDecimal.TEN, 5, "D1");
        productService.create("Phone", "Electronics", BigDecimal.TEN, 5, "D2");
        productService.create("Book", "Books", BigDecimal.TEN, 5, "D3");

        // When
        List<Product> electronics = productService.getProductsByCategory("electronics"); // Testing case insensitivity

        // Then
        assertThat(electronics).hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Phone");
    }

    @Test
    void shouldDeleteProduct() {
        // Given
        Product product = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        assertThat(productService.getAllProduct()).hasSize(1);

        // When
        productService.deleteProduct(product.getId());

        // Then
        assertThat(productService.getAllProduct()).isEmpty();
        
        assertThatThrownBy(() -> productService.getProductById(product.getId()))
                .isInstanceOf(ProductNotFoundException.class);
    }
}