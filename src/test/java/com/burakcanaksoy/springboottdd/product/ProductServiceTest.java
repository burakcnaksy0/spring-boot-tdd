package com.burakcanaksoy.springboottdd.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Assertions Örnek Test Sınıfı
 *
 * Kapsanan assertion'lar:
 *  - assertEquals / assertNotEquals
 *  - assertTrue / assertFalse
 *  - assertNull / assertNotNull
 *  - assertThrows
 *  - assertAll
 *  - assertSame / assertNotSame
 *  - assertInstanceOf
 *  - assertDoesNotThrow
 *  - assertIterableEquals
 */
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

    // create() metodu testleri

    @Test
    @DisplayName("assertEquals: Başarılı Ürün Ekleme")
    void assertEquals_addProduct(){
        Product product = productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),200,"128GB 6GB Ram");

        assertEquals("Iphone 12",product.getName(),"Ürün adı 'Iphone 12' olmalıdır");
        assertEquals("Phone",product.getCategoryName(),"Kategori adı 'Phone' olmalıdır");
        assertEquals(BigDecimal.valueOf(28999),product.getPrice(),"Fiyat '28999' olmalıdır");
        assertEquals(200,product.getNumberOfStock(),"Stok sayısı '200' olmalıdır");
        assertEquals("128GB 6GB Ram",product.getDescription(),"Açıklama '128GB 6GB Ram' olmalıdır");
    }

    @Test
    @DisplayName("assertTrue: Aktiflik Durumu")
    void assertTrue_checkActive(){
        Product product = productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),200,"128GB 6GB Ram");
        assertTrue(product.isActive(),"Yeni eklene ürün aktif olmalıdır.");
    }

    @Test
    @DisplayName("assertThrows: Geçersiz İsim")
    void assertThrows_invalidName(){
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.create("","Phone", BigDecimal.valueOf(28999),200,"128GB 6GB Ram"),
                "Geçersiz name için 'IllegalArgumentException' hatası fırlat"
        );
    }

    @Test
    @DisplayName("assertThrows: Geçersiz Kategori")
    void assertThrows_invalidCategoryName(){
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.create("Iphone 12","", BigDecimal.valueOf(28999),200,"128GB 6GB Ram"),
                "Geçersiz category-name için 'IllegalArgumentException' hatası fırlat"
        );
    }

    @Test
    @DisplayName("assertThrows: Geçersiz Fiyat")
    void assertThrows_invalidPrice(){
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.create("Iphone 12","Phone", BigDecimal.valueOf(-10),200,"128GB 6GB Ram"),
                "Geçersiz price için 'IllegalArgumentException' hatası fırlat"
        );
    }

    @Test
    @DisplayName("assertThrows: Geçersiz Stok Sayısı")
    void assertThrows_invalidStockNumber(){
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),-5,"128GB 6GB Ram"),
                "Geçersiz stock-number için 'IllegalArgumentException' hatası fırlat"
        );
    }

    @Test
    @DisplayName("assertThrows: Geçersiz Açıklama")
    void assertThrows_invalidDescription(){
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),200,""),
                "Geçersiz description için 'IllegalArgumentException' hatası fırlat"
        );
    }

    @Test
    @DisplayName("assertEquals: Başarılı Bulma")
    void assertEquals_getProductById(){
        Product product = productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),200,"128GB 6GB Ram");

        Product foundProduct = productService.getProductById(product.getId());

        assertEquals(product.getId(),foundProduct.getId(),"Id'ler aynı olmalıdır.");
    }


    @Test
    @DisplayName("assertThrows: Olmayan ürün icin ProductNotFoundException firlat")
    void assertThrows_productNotFound() {
        // Exception firlatilmali
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L),
                "Olmayan id icin ProductNotFoundException firlat"
        );

        assertEquals("Product not found with id: 999",exception.getMessage());
    }


    @Test
    @DisplayName("assertDoesNotThrow: Başarılı Silme")
    void assertDoesNotThrow_deleteProduct() {
        Product product = productService.create("Iphone 12","Phone", BigDecimal.valueOf(28999),200,"128GB 6GB Ram");

        assertDoesNotThrow(
                () -> productService.deleteProduct(product.getId())
        );
    }

    @Test
    @DisplayName("assertThrows: Olmayan Ürünü Silme")
    void assertThrows_deleteProductNotFound() {
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(999L),
                "Olmayan id icin ProductNotFoundException firlat"
        );
        assertEquals("Product not found with id: 999",exception.getMessage());
    }

    @Test
    @DisplayName("assertEquals: Boş Liste")
    void assertEquals_emptyList() {
        List<Product> productList = productService.getAllProduct();

        assertEquals(0,productList.size());
        assertTrue(productList.isEmpty());
    }

    @Test
    @DisplayName("assertEquals: Dolu Liste")
    void assertEquals_fullList() {
        productService.create("Iphone 12", "Phone", BigDecimal.valueOf(28999), 200, "128GB 6GB Ram");
        productService.create("Samsung S24", "Phone", BigDecimal.valueOf(35999), 150, "256GB 8GB Ram");
        productService.create("MacBook Air M3", "Laptop", BigDecimal.valueOf(54999), 50, "512GB SSD 16GB RAM");

        List<Product> productList = productService.getAllProduct();

        assertEquals(3, productList.size(), "Dolu listenin boyutu 3 olmalıdır.");
    }


    @Test
    @DisplayName("assertEquals: Aktifleri Filtreleme")
    void assertEquals_filterActiveProduct() {
        Product p1 = productService.create("Iphone 12", "Phone", BigDecimal.valueOf(28999), 200, "128GB 6GB Ram");
        Product p2 = productService.create("Samsung S24", "Phone", BigDecimal.valueOf(35999), 150, "256GB 8GB Ram");

        productService.deactivateProduct(p1.getId());

        List<Product> activeProducts = productService.getActiveProducts();
        List<Product> expected = List.of(p2);

        assertIterableEquals(expected,activeProducts,"Sadece aktif ürünler bulunmalıdır.");
    }

    @Test
    @DisplayName("assertFalse: Başarılı Deaktif Etme")
    void assertFalse_validDeactiveteProduct(){
        Product p1 = productService.create("Iphone 12", "Phone", BigDecimal.valueOf(28999), 200, "128GB 6GB Ram");
        productService.deactivateProduct(p1.getId());
        assertFalse(p1.isActive());
    }

    @Test
    @DisplayName("assertThrows: Olmayan Ürünü Deaktif Etme")
    void assertThrows_invalidDeactiveteProduct(){
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deactivateProduct(999L),
                "Olmayan id deactivate edilemez -> ProductNotFoundException firlat"
        );
        assertEquals("Product not found with id: 999",exception.getMessage());
    }





    @Test
    @DisplayName("assertEquals: Doğru Kategori Bulma")
    void assertEquals_getProductsByCategory() {
        productService.create("TV", "Elektronik", BigDecimal.valueOf(15000), 10, "Smart TV");
        productService.create("Laptop", "Elektronik", BigDecimal.valueOf(30000), 5, "Gaming Laptop");
        productService.create("T-Shirt", "Giyim", BigDecimal.valueOf(500), 50, "Pamuklu");

        List<Product> elektronikUrunler = productService.getProductsByCategory("Elektronik");

        assertEquals(2, elektronikUrunler.size(), "Elektronik kategorisinde 2 ürün olmalıdır.");
    }

    @Test
    @DisplayName("assertEquals: Case-Insensitive Kategori Arama")
    void assertEquals_getProductsByCategoryCaseInsensitive() {
        productService.create("TV", "Elektronik", BigDecimal.valueOf(15000), 10, "Smart TV");
        productService.create("Laptop", "Elektronik", BigDecimal.valueOf(30000), 5, "Gaming Laptop");
        productService.create("T-Shirt", "Giyim", BigDecimal.valueOf(500), 50, "Pamuklu");

        List<Product> elektronikUrunler = productService.getProductsByCategory("eLEkTronik");

        assertEquals(2, elektronikUrunler.size(), "Büyük/küçük harf duyarsız aramada 2 ürün bulunmalıdır.");
    }

    @Test
    @DisplayName("assertTrue: Olmayan Kategori")
    void assertTrue_getProductsByInvalidCategory() {
        productService.create("TV", "Elektronik", BigDecimal.valueOf(15000), 10, "Smart TV");

        List<Product> mobilyaUrunler = productService.getProductsByCategory("Mobilya");

        assertTrue(mobilyaUrunler.isEmpty(), "Olmayan kategori arandığında boş liste dönmelidir.");
    }

}