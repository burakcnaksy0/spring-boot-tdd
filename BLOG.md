# Spring Boot ile TDD: Sıfırdan Üretime — Eksiksiz Rehber

## Giriş

Test-Driven Development (TDD), kodu yazmadan önce testi yazan ve ardından testi geçecek minimum kodu üreten bir yazılım geliştirme felsefesidir. Bu blog yazısında, gerçek bir Spring Boot projesinin yedi branch üzerinden nasıl adım adım ilerlediğini göreceğiz. Her branch, test piramidinin bir katmanına karşılık gelir.

### Proje Yapısı — Branch Haritası

| Branch | Kapsam | Anahtar Teknoloji |
|---|---|---|
| `master` | Proje iskeleti | Spring Boot 4, Lombok |
| `develop` | JUnit 5 Assertions | JUnit 5 (pure) |
| `assertj` | AssertJ fluent assertions | AssertJ |
| `mock` | Mockito ile unit test | Mockito, AssertJ |
| `webmvc` | Controller katmanı | MockMvc, @WebMvcTest |
| `data-jpa` | Repository katmanı | @DataJpaTest, H2 |
| `spring-boot-test` | Entegrasyon + Container | @SpringBootTest, Testcontainers |

---

## Branch 1 — `master`: Projenin İskeleti

`master` branch'i projenin başlangıç noktasıdır. Üretim kodu henüz yoktur; sadece Spring Boot uygulaması yapılandırılmıştır.

### `pom.xml` — Temel Bağımlılıklar

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
</parent>

<dependencies>
    <!-- Web katmanı -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- PostgreSQL sürücüsü -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Boilerplate kod azaltma -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Test çerçevesi (JUnit 5 + Mockito dahil) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Neden `webmvc-test`?** Spring Boot 4'te bu starter, `spring-boot-starter-test` yerine geçer ve JUnit 5, Mockito, AssertJ, MockMvc, TestRestTemplate'i otomatik getirir.

---

## Branch 2 — `develop`: JUnit 5 Assertions ile İlk Unit Testler

Bu branch, dış bağımlılık (veritabanı, framework) kullanmadan, saf Java servisleri üzerinde JUnit 5'in tüm assertion metodlarını öğretmek amacıyla tasarlanmıştır.

### Domain Modeli

```java
@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private int age;
    private boolean active;
}
```

> ⚡ Bu noktada `@Entity`, JPA yoktur. Veriler bellekte (in-memory list) tutulur.

### `UserService` — In-Memory Implementasyon

```java
@Service
public class UserService {
    private List<User> userList = new ArrayList<>();
    private long countId = 1;

    public User createUser(String name, String email, int age) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email address");
        if (age < 0 || age > 150)
            throw new IllegalArgumentException("Age must be between 0 and 150.");

        User user = new User(countId++, name, email, age, true);
        userList.add(user);
        return user;
    }

    public User findById(Long id) {
        return userList.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> findAll() { return new ArrayList<>(userList); }
    public List<User> findActiveUsers() { return userList.stream().filter(User::isActive).toList(); }

    public User deactiveUser(Long id) {
        User user = findById(id);
        user.setActive(false);
        return user;
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        userList.remove(user);
    }

    public Optional<User> findByEmail(String email) {
        return userList.stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(email))
            .findFirst();
    }

    public void clearAll() { userList.clear(); countId = 1; }
}
```

### Test Sınıfı — JUnit 5 Assertion Referansı

```java
@DisplayName("UserService - JUnit Assertions Demo")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() { userService = new UserService(); }

    @AfterEach
    void tearDown() { userService.clearAll(); }

    // 1. assertEquals & assertNotEquals
    @Test
    @DisplayName("assertEquals: Kullanici adi ve yasi dogru set edilmeli")
    void assertEquals_userName() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertEquals("Burak", user.getName(), "Kullanici adi 'Burak' olmali");
        assertEquals(25, user.getAge(), "Kullanici yasi 25 olmali");
    }

    @Test
    @DisplayName("assertNotEquals: Farkli kullanicilar farkli id almali")
    void assertNotEquals_userIds() {
        User user1 = userService.createUser("Burak", "burak@mail.com", 25);
        User user2 = userService.createUser("Ahmet", "ahmet@mail.com", 30);

        assertNotEquals(user1.getId(), user2.getId());
        assertNotEquals(user1.getEmail(), user2.getEmail());
    }

    // 2. assertTrue & assertFalse
    @Test
    @DisplayName("assertTrue: Yeni kullanici aktif olmali")
    void assertTrue_newUserIsActive() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertTrue(user.isActive());
        assertTrue(user.getId() > 0);
    }

    @Test
    @DisplayName("assertFalse: Deaktif edilen kullanici aktif olmamali")
    void assertFalse_deactivatedUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        userService.deactiveUser(user.getId());
        User deactivated = userService.findById(user.getId());
        assertFalse(deactivated.isActive());
    }

    // 3. assertNull & assertNotNull
    @Test
    @DisplayName("assertNotNull: Olusturulan kullanici null olmamali")
    void assertNotNull_createdUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getName());
    }

    @Test
    @DisplayName("assertNull: findByEmail bos Optional donmeli")
    void assertNull_findByEmailNotExists() {
        Optional<User> result = userService.findByEmail("yok@mail.com");
        assertNull(result.orElse(null));
    }

    // 4. assertThrows
    @Test
    @DisplayName("assertThrows: Gecersiz isim icin IllegalArgumentException")
    void assertThrows_blankName() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser("", "test@mail.com", 25)
        );
        assertEquals("Name cannot be blank.", ex.getMessage());
    }

    @Test
    @DisplayName("assertThrows: Olmayan kullanici icin UserNotFoundException")
    void assertThrows_userNotFound() {
        UserNotFoundException ex = assertThrows(
            UserNotFoundException.class,
            () -> userService.findById(999L)
        );
        assertEquals("User not found with id: 999", ex.getMessage());
    }

    // 5. assertAll — Birden fazla assertion'ı grupla
    @Test
    @DisplayName("assertAll: Kullanici alanlari toplu dogrulama")
    void assertAll_userFields() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertAll("kullanici alanlari",
            () -> assertNotNull(user.getId()),
            () -> assertEquals("Burak", user.getName()),
            () -> assertEquals("burak@mail.com", user.getEmail()),
            () -> assertEquals(25, user.getAge()),
            () -> assertTrue(user.isActive())
        );
    }

    // 6. assertSame & assertNotSame
    @Test
    @DisplayName("assertSame: findById ayni nesneyi donmeli")
    void assertSame_findById() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        User found = userService.findById(user.getId());
        assertSame(user, found); // referans ayni
    }

    @Test
    @DisplayName("assertNotSame: findAll kopya liste donmeli")
    void assertNotSame_findAll() {
        userService.createUser("Burak", "burak@mail.com", 25);
        List<User> list1 = userService.findAll();
        List<User> list2 = userService.findAll();
        assertNotSame(list1, list2); // farkli liste nesneleri
    }

    // 7. assertInstanceOf
    @Test
    @DisplayName("assertInstanceOf: Firlatilan exception dogru tipte olmali")
    void assertInstanceOf_exception() {
        Exception ex = assertThrows(Exception.class,
            () -> userService.findById(999L));
        assertInstanceOf(UserNotFoundException.class, ex);
    }

    // 8. assertDoesNotThrow
    @Test
    @DisplayName("assertDoesNotThrow: Gecerli kullanici basariyla silinmeli")
    void assertDoesNotThrow_deleteUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertDoesNotThrow(() -> userService.deleteUser(user.getId()));
    }

    // 9. assertIterableEquals
    @Test
    @DisplayName("assertIterableEquals: Aktif kullanicilari filtrele")
    void assertIterableEquals_activeUsers() {
        User u1 = userService.createUser("Burak", "burak@mail.com", 25);
        User u2 = userService.createUser("Ahmet", "ahmet@mail.com", 30);
        userService.deactiveUser(u1.getId());

        List<User> active = userService.findActiveUsers();
        assertIterableEquals(List.of(u2), active);
    }
}
```

### JUnit 5 Assertion Özet Tablosu

| Metod | Ne Zaman Kullanılır |
|---|---|
| `assertEquals(expected, actual)` | İki değerin eşit olup olmadığını doğrular |
| `assertNotEquals` | Farklı olduklarını doğrular |
| `assertTrue(condition)` | Koşulun `true` olduğunu doğrular |
| `assertFalse(condition)` | Koşulun `false` olduğunu doğrular |
| `assertNotNull(obj)` | Nesnenin null olmadığını doğrular |
| `assertNull(obj)` | Nesnenin null olduğunu doğrular |
| `assertThrows(Type, executable)` | Belirtilen exception'ın fırlatıldığını doğrular |
| `assertAll(executables...)` | Birden fazla assertion'ı gruplar; hepsi çalışır |
| `assertSame(expected, actual)` | Referans eşitliğini doğrular (`==`) |
| `assertNotSame` | Referans farklılığını doğrular |
| `assertInstanceOf(Type, obj)` | Nesnenin tipini doğrular |
| `assertDoesNotThrow(executable)` | Kod bloğunun exception fırlatmadığını doğrular |
| `assertIterableEquals` | İki iterable'ın sıra ve eleman bazında eşit olduğunu doğrular |
## Branch 3 — `assertj`: Fluent Assertion Stili

AssertJ, JUnit'in yerleşik assertion'larına kıyasla çok daha okunabilir, zincirleme (fluent) bir API sunar. Bir assertion başarısız olduğunda hata mesajları da çok daha açıklayıcıdır.

### Neden AssertJ?

```java
// JUnit 5 stili — daha az okunabilir
assertEquals(2, list.size());
assertTrue(list.contains("P1"));

// AssertJ stili — insan dili gibi okunur
assertThat(list).hasSize(2).contains("P1");
```

### AssertJ ile Test Örnekleri (`ProductServiceTest`)

```java
class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() { productService = new ProductService(); }

    @AfterEach
    void tearDown() { productService.clearAll(); }

    @Test
    void shouldCreateProduct() {
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
        assertThat(product.getPrice()).isEqualByComparingTo("1500.00"); // BigDecimal safe comparison
        assertThat(product.isActive()).isTrue();

        // Alan bazlı kontrol
        assertThat(product).hasFieldOrPropertyWithValue("categoryName", category);
        assertThat(product).hasFieldOrPropertyWithValue("numberOfStock", stock);
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        // Birincil sözdizimi
        assertThatThrownBy(() -> productService.create("", "Category", BigDecimal.TEN, 5, "Desc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Name cannot be blank.");

        // Alternatif sözdizimi
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> productService.create(null, "Category", BigDecimal.TEN, 5, "Desc"))
            .withMessage("Name cannot be blank.");
    }

    @Test
    void shouldThrowExceptionWhenPriceIsNegative() {
        assertThatThrownBy(() -> productService.create("Name", "Category", new BigDecimal("-1"), 5, "Desc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Price"); // tam mesajı bilmeden kısmi kontrol
    }

    @Test
    void shouldGetProductById() {
        Product created = productService.create("Phone", "Electronics", BigDecimal.valueOf(800), 20, "Smartphone");

        Product found = productService.getProductById(created.getId());

        assertThat(found)
            .isNotNull()
            .isEqualTo(created)   // equals() ile karşılaştırır
            .isSameAs(created);   // == ile referans karşılaştırır

        assertThat(found.getName()).startsWith("Pho").endsWith("ne");
    }

    @Test
    void shouldGetAllProducts() {
        productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        productService.create("P2", "C2", BigDecimal.TEN, 5, "D2");

        List<Product> products = productService.getAllProduct();

        assertThat(products)
            .isNotEmpty()
            .hasSize(2)
            .extracting(Product::getName)     // belirli bir alana odaklan
            .containsExactly("P1", "P2");     // sıra önemli

        assertThat(products)
            .extracting("categoryName")
            .contains("C1", "C2");
    }

    @Test
    void shouldGetActiveProducts() {
        Product p1 = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        Product p2 = productService.create("P2", "C1", BigDecimal.TEN, 5, "D2");
        productService.deactivateProduct(p1.getId());

        List<Product> activeProducts = productService.getActiveProducts();

        assertThat(activeProducts)
            .hasSize(1)
            .containsOnly(p2)     // sadece p2 içermeli
            .doesNotContain(p1);  // p1 içermemeli

        // Her eleman için lambda doğrulama
        assertThat(activeProducts).allSatisfy(product -> {
            assertThat(product.isActive()).isTrue();
            assertThat(product.getName()).isEqualTo("P2");
        });
    }

    @Test
    void shouldGetProductsByCategory() {
        productService.create("Laptop", "Electronics", BigDecimal.TEN, 5, "D1");
        productService.create("Phone", "Electronics", BigDecimal.TEN, 5, "D2");
        productService.create("Book", "Books", BigDecimal.TEN, 5, "D3");

        List<Product> electronics = productService.getProductsByCategory("electronics"); // case-insensitive

        assertThat(electronics)
            .hasSize(2)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("Laptop", "Phone"); // sıra önemsiz
    }

    @Test
    void shouldDeleteProduct() {
        Product product = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        assertThat(productService.getAllProduct()).hasSize(1);

        productService.deleteProduct(product.getId());

        assertThat(productService.getAllProduct()).isEmpty();

        assertThatThrownBy(() -> productService.getProductById(product.getId()))
            .isInstanceOf(ProductNotFoundException.class);
    }
}
```

### En Sık Kullanılan AssertJ Metodları

| Metod | Açıklama |
|---|---|
| `assertThat(x).isNotNull()` | null kontrolü |
| `assertThat(x).isEqualTo(y)` | değer eşitliği |
| `assertThat(x).isSameAs(y)` | referans eşitliği |
| `assertThat(x).isInstanceOf(Type.class)` | tip kontrolü |
| `assertThat(x).isPositive()` | sayısal pozitiflik |
| `assertThat(str).startsWith("abc")` | string ön ek |
| `assertThat(str).hasMessageContaining("xyz")` | kısmi mesaj |
| `assertThat(list).hasSize(n)` | liste boyutu |
| `assertThat(list).contains(elem)` | eleman varlığı |
| `assertThat(list).containsExactly(...)` | tam sıralı eşleşme |
| `assertThat(list).containsExactlyInAnyOrder(...)` | sırasız eşleşme |
| `assertThat(list).extracting(Field::getter)` | alanları çıkar |
| `assertThat(list).allSatisfy(lambda)` | her eleman için koşul |
| `assertThatThrownBy(exec).isInstanceOf(...)` | exception tipi |
| `assertThatThrownBy(exec).hasMessage(...)` | exception mesajı |
| `assertThat(bd).isEqualByComparingTo("1.0")` | BigDecimal güvenli karşılaştırma |

---

## Branch 4 — `mock`: Mockito ile Bağımlılık Yönetimi

`mock` branch'i, gerçek bir Spring Boot uygulamasının katmanlı mimarisini kurar. Artık PostgreSQL, Spring Data JPA, Controller, Request/Response DTO'ları ve bir `UserMapper` vardır. Bu katmanda unit test yazmak için Mockito kullanılır.

### Mimari

```
UserController
      │
      ▼
UserService ──► UserRepository (JpaRepository)
      │
      ▼
UserMapper (Entity ↔ DTO dönüşümü)
```

### Temel Sınıflar

**`User` (Entity):**
```java
@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String phone;
    private int age;
    private boolean active;
}
```

**`UserRepository`:**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
```

**`UserService`:**
```java
@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new UserAlreadyExistsException("Email already exists");
        if (userRepository.existsByPhone(request.getPhone()))
            throw new UserAlreadyExistsException("Phone already exists");

        User user = userMapper.toEntity(request);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}
```

### Mockito Unit Testi — `UserServiceTest`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;  // Gerçek DB yok, sahte nesne

    @Mock
    UserMapper userMapper;           // Gerçek mapper yok, sahte nesne

    @InjectMocks
    UserService userService;         // Mock'lar otomatik inject edilir

    User user;
    UserCreateRequest request;
    UserResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L).firstName("Burak").lastName("Can")
            .username("burakcan").email("burak@can.com")
            .phone("05554443322").age(25).active(true).build();

        request = UserCreateRequest.builder()
            .firstName("Burak").lastName("Can")
            .username("burakcan").email("burak@can.com")
            .phone("05554443322").age(25).build();

        response = UserResponse.builder()
            .id(1L).firstName("Burak").lastName("Can")
            .username("burakcan").email("burak@can.com")
            .phone("05554443322").age(25).active(true).build();
    }

    // --- Happy Path ---

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange: mock davranışlarını tanımla
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(response.getId());
        assertThat(result.getUsername()).isEqualTo(response.getUsername());

        // Verify: metodların kaç kez çağrıldığını doğrula
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void createUser_ShouldReturnUserResponse() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("burakcan");

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByPhone(request.getPhone());
        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    // --- Exception Senaryoları ---

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found with id: 1");

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, never()).toResponse(any()); // hiç çağrılmamalı
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("Email already exists");

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenPhoneExists_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("Phone already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void getAllUsers_ShouldReturnUserResponseList() {
        List<User> users = List.of(user);
        List<UserResponse> responses = List.of(response);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseList(users)).thenReturn(responses);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("burakcan");
    }
}
```

### Mockito Kavramları Özeti

| Kavram | Açıklama |
|---|---|
| `@Mock` | Sahte (fake) nesne oluşturur; gerçek metod çağrılmaz |
| `@InjectMocks` | `@Mock` ile işaretlenmiş nesneleri constructor/field injection ile enjekte eder |
| `@ExtendWith(MockitoExtension.class)` | JUnit 5'e Mockito desteği ekler |
| `when(x).thenReturn(y)` | x çağrıldığında y döndür |
| `when(x).thenThrow(ex)` | x çağrıldığında exception fırlat |
| `doNothing().when(mock).method()` | void metodlar için mock tanımı |
| `verify(mock).method()` | metodun çağrıldığını doğrular |
| `verify(mock, times(n))` | metodun tam n kez çağrıldığını doğrular |
| `verify(mock, never())` | metodun hiç çağrılmadığını doğrular |
| `any()`, `anyString()`, `eq(val)` | Argument matcher'lar |
## Branch 5 — `webmvc`: `@WebMvcTest` ile Controller Testleri

Controller katmanını test etmek için gerçek bir Spring Boot uygulaması ayağa kaldırmaya gerek yoktur. `@WebMvcTest` sadece web katmanını (Controller, Filter, ExceptionHandler) yükler; veritabanı veya servis katmanı yoktur.

### `@WebMvcTest` Nasıl Çalışır?

```
@WebMvcTest(UserController.class)
    │
    ├── UserController ✓ (gerçek)
    ├── MockMvc ✓ (HTTP simülasyonu)
    ├── ObjectMapper ✓ (JSON dönüşümü)
    ├── UserService ✗ (yüklenmez → @MockitoBean ile taklit edilir)
    └── Veritabanı ✗ (yüklenmez)
```

### `UserControllerTest`

```java
@AutoConfigureMockMvc(addFilters = false) // Security filtrelerini devre dışı bırak
@WebMvcTest(value = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Java ↔ JSON dönüşümü

    @MockitoBean
    private UserService userService; // Servis katmanını mock'la

    UserResponse response, response2, response3, response4;
    UserCreateRequest request;

    @BeforeEach
    void setUp() {
        request = UserCreateRequest.builder()
            .firstName("John").lastName("Doe").username("johndoe")
            .email("john@example.com").phone("05321234567").age(25).build();

        response = UserResponse.builder()
            .id(1L).firstName("John").lastName("Doe").username("johndoe")
            .email("john@example.com").phone("05321234567").age(25).active(true).build();

        response2 = UserResponse.builder()
            .id(2L).firstName("Alice").lastName("Smith").username("alicesmith")
            .email("alice@example.com").phone("05431234567").age(28).active(true).build();

        // response3 ve response4 de benzer şekilde...
    }

    // GET /api/user/all
    @Test
    void shouldReturnAllUsers() throws Exception {
        List<UserResponse> userList = List.of(response, response2);
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/user/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Alice"));
    }

    // GET /api/user/{id}
    @Test
    void shouldReturnUserById() throws Exception {
        when(userService.getUserById(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/user/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.phone").value("05321234567"))
            .andExpect(jsonPath("$.active").value(true));
    }

    // POST /api/user
    @Test
    void shouldCreateUser() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.age").value(25));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    // DELETE /api/user/{id}
    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L));

        mockMvc.perform(delete("/api/user/{id}", 1L))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
```

### MockMvc Yöntemler Tablosu

| Yöntem | HTTP Karşılığı |
|---|---|
| `get("/path")` | GET |
| `post("/path")` | POST |
| `put("/path")` | PUT |
| `delete("/path")` | DELETE |
| `patch("/path")` | PATCH |

### MockMvc Result Matcher'lar

| Matcher | Açıklama |
|---|---|
| `status().isOk()` | HTTP 200 |
| `status().isCreated()` | HTTP 201 |
| `status().isNoContent()` | HTTP 204 |
| `status().isBadRequest()` | HTTP 400 |
| `status().isNotFound()` | HTTP 404 |
| `jsonPath("$.field").value(val)` | JSON alanını doğrular |
| `jsonPath("$.size()").value(n)` | JSON dizi boyutunu doğrular |
| `content().contentType(...)` | Content-Type doğrular |

---

## Branch 6 — `data-jpa`: `@DataJpaTest` ile Repository Testleri

`@DataJpaTest`, sadece JPA katmanını (Entity'ler, Repository'ler) yükler. Varsayılan olarak H2 in-memory veritabanı kullanır ve her test otomatik `@Transactional` ile rollback yapılır.

### Custom JPQL ve Native SQL Sorguları

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data'nın method ismi çözümlemesi
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // JPQL ile custom COUNT
    @Query("SELECT COUNT(*) FROM User u")
    long countAllUsers();

    // JPQL ile filtreli sorgu
    @Query("SELECT u FROM User u WHERE u.age >= :age AND u.active = true")
    List<User> findActiveUsersOlderThan(@Param("age") int age);

    // JPQL ile çoklu parametre
    @Query("SELECT u FROM User u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    List<User> findByFullName(@Param("firstName") String firstName,
                               @Param("lastName") String lastName);

    // JPQL ile UPDATE (zorunlu @Modifying)
    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    int deactivateUser(@Param("userId") Long userId);

    // Native SQL sorgusu
    @Query(value = "SELECT * FROM users WHERE email LIKE CONCAT('%', :domain, '%')",
           nativeQuery = true)
    List<User> findUsersByEmailDomain(@Param("domain") String domain);

    // Native SQL ile DELETE
    @Modifying
    @Query(value = "DELETE FROM users WHERE active = false", nativeQuery = true)
    int deleteInactiveUsers();
}
```

### `@DataJpaTest` Test Sınıfı

```java
@DataJpaTest // Sadece JPA katmanını yükler, H2 kullanır, her test rollback
class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager; // JPA işlemleri için yardımcı

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;

    @BeforeEach
    void setUp() {
        user1 = new User(null, "mert", "ceylan", "mertceylan",
                         "mertceylan@example.com", "05350682758", 27, true);
        user2 = new User(null, "salih", "dursun", "salihdursun",
                         "salihdursun@example.com", "05350886527", 21, true);

        // persistAndFlush: veriyi veritabanına yaz ve persistence context'i temizle
        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        User newUser = new User(null, "ahmet", "yilmaz", "ahmetyilmaz",
                                "ahmet@example.com", "05555555555", 30, true);
        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("ahmetyilmaz");
    }

    @Test
    void findById_ShouldReturnUser() {
        Optional<User> user = userRepository.findById(user1.getId());

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("mertceylan");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        List<User> userList = userRepository.findAll();

        assertThat(userList).hasSize(2);
        assertThat(userList).extracting(User::getPhone)
            .containsExactlyInAnyOrder("05350682758", "05350886527");
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully() {
        user2.setPhone("05350482874");
        userRepository.save(user2);
        testEntityManager.flush();
        testEntityManager.clear(); // Önbellekten değil DB'den oku

        Optional<User> foundUser = userRepository.findById(user2.getId());
        assertThat(foundUser.get().getPhone()).isEqualTo("05350482874");
    }

    @Test
    void deleteById_RemoveUser() {
        long userId = user1.getId();
        userRepository.deleteById(userId);
        testEntityManager.flush();

        Optional<User> foundUser = userRepository.findById(user1.getId());
        assertThat(foundUser).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void existByEmail_ShouldReturnTrueWhenExists() {
        boolean isExists = userRepository.existsByEmail(user1.getEmail());
        assertThat(isExists).isTrue();
    }

    @Test
    void existByEmail_ShouldReturnFalseWhenNotExists() {
        boolean isExists = userRepository.existsByEmail("nobody@example.com");
        assertThat(isExists).isFalse();
    }

    @Test
    void countAllUsers_ShouldReturn2() {
        long userCount = userRepository.countAllUsers();
        assertThat(userCount).isEqualTo(2);
    }

    @Test
    void findActiveUsersOlderThan_ShouldFilterCorrectly() {
        List<User> userList = userRepository.findActiveUsersOlderThan(25);

        assertThat(userList).hasSize(1);
        assertThat(userList.get(0).getUsername()).isEqualTo("mertceylan"); // 27 yaşında
    }

    @Test
    void findByFullName_ShouldReturnMatchingUser() {
        List<User> userList = userRepository.findByFullName("salih", "dursun");

        assertThat(userList).hasSize(1);
        assertThat(userList.get(0).getFirstName()).isEqualTo("salih");
    }

    @Test
    void findUsersByEmailDomain_ShouldFilterByDomain() {
        User newUser = new User(null, "başak", "özcan", "basakozcan",
                                "basakozcan@gmail.com", "05457508682", 34, true);
        testEntityManager.persistAndFlush(newUser);

        List<User> gmailUsers = userRepository.findUsersByEmailDomain("gmail.com");

        assertThat(gmailUsers).hasSize(1);
        assertThat(gmailUsers.get(0).getUsername()).isEqualTo("basakozcan");
    }

    @Test
    void deactivateUser_ShouldSetActiveFalse() {
        int updatedCount = userRepository.deactivateUser(user1.getId());

        assertThat(updatedCount).isEqualTo(1);

        // Persistence context temizlenmeden findById önbellekten okur!
        testEntityManager.clear();

        User updatedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(updatedUser.isActive()).isFalse();
    }

    @Test
    void deleteInactiveUsers_ShouldRemoveOnlyInactiveUsers() {
        user2.setActive(false);
        testEntityManager.persistAndFlush(user2);
        testEntityManager.clear();

        int deletedCount = userRepository.deleteInactiveUsers();

        assertThat(deletedCount).isEqualTo(1);
        assertThat(userRepository.findById(user2.getId())).isEmpty();
        assertThat(userRepository.findById(user1.getId())).isPresent();
    }
}
```

### `@DataJpaTest` Kritik Notlar

> **`testEntityManager.clear()` neden gereklidir?**
>
> Hibernate bir `first-level cache` (persistence context) tutar. `@Modifying` ile bir UPDATE ya da DELETE yaptıktan sonra aynı transaction içinde `findById` çağırırsanız, Hibernate veritabanına gitmez ve önbellekteki (eski) nesneyi döner. `testEntityManager.clear()` bu önbelleği temizler ve Hibernate'i veritabanından taze veri okumaya zorlar.

| `TestEntityManager` Metodu | Açıklama |
|---|---|
| `persist(entity)` | Entity'yi persistence context'e ekler |
| `persistAndFlush(entity)` | Ekler ve anında DB'ye yazar |
| `flush()` | Bekleyen tüm değişiklikleri DB'ye yazar |
| `clear()` | Persistence context önbelleğini temizler |
| `find(Class, id)` | DB'den taze okuma yapar |
## Branch 7 — `spring-boot-test`: Entegrasyon Testleri

Bu branch, test piramidinin en üst noktasıdır. Gerçek (veya gerçeğe yakın) bir ortamda tüm katmanların birlikte doğru çalıştığını test eder. İki farklı yaklaşım uygulanmıştır:

1. **`UserApplicationIntegrationTest`** — H2 in-memory ile `@Sql` kullanımı
2. **`UserApplicationContainerTest`** — Testcontainers ile gerçek PostgreSQL

---

### pom.xml — Ek Bağımlılıklar

```xml
<!-- Testcontainers core -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.21.3</version>
    <scope>test</scope>
</dependency>

<!-- PostgreSQL container -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.21.3</version>
    <scope>test</scope>
</dependency>

<!-- JUnit 5 entegrasyonu -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.21.3</version>
    <scope>test</scope>
</dependency>

<!-- H2 (test profili için) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

### Yaklaşım 1: `@SpringBootTest` + `@ActiveProfiles("test")` + `@Sql`

#### `application-test.properties`

```properties
# H2 in-memory veritabanı — gerçek PostgreSQL gerekmez
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=test
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
```

#### SQL Script'leri

**`setup-test-users.sql`:**
```sql
INSERT INTO users (id, first_name, last_name, username, email, phone, age, active)
VALUES (1, 'TestFirstName', 'TestLastName', 'testuser', 'testuser@gmail.com', '05360623971', 30, true);
```

**`cleanup-test-users.sql`:**
```sql
DELETE FROM users;
```

#### `UserApplicationIntegrationTest`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // application-test.properties'i yükle
public class UserApplicationIntegrationTest {

    @LocalServerPort
    private int port; // Rastgele port — paralel testlerde çakışma olmaz

    @Autowired
    private TestRestTemplate restTemplate; // Gerçek HTTP isteği gönderir

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = "/setup-test-users.sql",
         executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup-test-users.sql",
         executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetUserById_whenUserExists() {
        // @Sql ile önceden id=1 kullanıcı eklendi
        String url = "http://localhost:" + port + "/api/user/1";

        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("testuser@gmail.com");
    }

    @Test
    @Sql(scripts = "/cleanup-test-users.sql",
         executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldCreateUser_andSaveToDatabase() {
        UserCreateRequest request = UserCreateRequest.builder()
            .firstName("John").lastName("Doe").username("johndoe")
            .email("john.doe@test.com").phone("05359702361").age(25).build();

        String url = "http://localhost:" + port + "/api/user";

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(url, request, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("johndoe");

        // Gerçekten veritabanına kaydedildi mi?
        Optional<User> foundUser = userRepository.findById(response.getBody().getId());
        assertThat(foundUser).isPresent();
    }
}
```

---

### Yaklaşım 2: Testcontainers ile Gerçek PostgreSQL

#### `BaseContainerTest` — Shared Container Pattern

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContainerTest {

    // static final → tüm test sınıflarında tek bir container (singleton)
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:14.23")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");
        POSTGRES_CONTAINER.start(); // Container bir kez başlar
    }

    // @DynamicPropertySource: Container'ın dinamik port/URL'sini Spring'e bildir
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
```

> **Neden `static`?** Container'ı her test metodu için yeniden başlatmak çok maliyetlidir. `static final` ile singleton pattern uygulanır: container bir kez başlar, tüm testler biter, bir kez durur.

#### `UserApplicationContainerTest`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApplicationContainerTest extends BaseContainerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll(); // Her testten sonra temizle
    }

    @Test
    void shouldCreateUserAndPersistInPostgreSQL() {
        assertTrue(POSTGRES_CONTAINER.isRunning()); // Container çalışıyor mu?

        UserCreateRequest request = UserCreateRequest.builder()
            .firstName("burakcan").lastName("aksoy").username("burakcnaksy")
            .email("aksoyburak808@gmail.com").phone("05350482740").age(24).build();

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/user",
            request,
            UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();

        // PostgreSQL'e gerçekten kaydedildi mi?
        Optional<User> savedUser = userRepository.findById(response.getBody().getId());
        assertThat(savedUser).isPresent();
    }

    @Test
    void shouldReturn400_whenCreateUserWithInvalidData() {
        UserCreateRequest request = UserCreateRequest.builder()
            .firstName("burakcan").lastName("aksoy").username("burakcnaksy")
            .email("invalid-email-format") // Geçersiz email
            .phone("05350482740").age(15).build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/user",
            request,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(userRepository.count()).isEqualTo(0); // Hiçbir şey kaydedilmemeli
    }

    @Test
    void shouldGetUserById_whenUserExists() {
        // Doğrudan repository üzerinden kaydet
        User user = User.builder()
            .firstName("burakcan").lastName("aksoy").username("burakcnaksy")
            .email("aksoyburak808@gmail.com").phone("05350482740").age(24).active(true).build();
        User savedUser = userRepository.save(user);

        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user/" + savedUser.getId(),
            UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("aksoyburak808@gmail.com");
    }

    @Test
    void shouldDeleteUser_whenUserExists() {
        User user = User.builder()
            .firstName("john").lastName("doe").username("johndoe")
            .email("john.doe@gmail.com").phone("05351112233").age(30).active(true).build();
        User savedUser = userRepository.save(user);

        ResponseEntity<Void> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/user/" + savedUser.getId(),
            HttpMethod.DELETE,
            null,
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}
```

---

### İki Yaklaşımın Karşılaştırması

| Özellik | `@Sql` + H2 | Testcontainers + PostgreSQL |
|---|---|---|
| Hız | ⚡ Çok hızlı | 🐢 Container başlatma süresi |
| Gerçekçilik | ⚠️ Orta (H2 ≠ PostgreSQL) | ✅ Yüksek (gerçek veritabanı) |
| Kurulum | Basit | Docker gerekir |
| Native SQL | Sınırlı | Tam destek |
| CI/CD uyumu | Kolay | Docker daemon gerekir |
| Önerilen kullanım | Hızlı geliştirme döngüsü | Pre-merge, CI pipeline |

---

## Test Piramidi — Genel Bakış

```
                    ▲
                   /|\
                  / | \
                 /  |  \    @SpringBootTest
                /   |   \   Testcontainers
               / Entegrasyon\
              /───────────────\
             /                 \
            /    Controller      \  @WebMvcTest
           /     (MockMvc)        \  MockitoBean
          /─────────────────────────\
         /                           \
        /       Service Katmanı       \  @ExtendWith(MockitoExtension)
       /         (Mockito)             \  @Mock / @InjectMocks
      /───────────────────────────────────\
     /                                     \
    /       Repository Katmanı              \  @DataJpaTest
   /         (@DataJpaTest)                  \  TestEntityManager
  /───────────────────────────────────────────\
 /                                             \
/          Unit Testler (Pure JUnit/AssertJ)    \
/─────────────────────────────────────────────────\
```

| Katman | Annotation | Hız | Kapsam |
|---|---|---|---|
| Unit (servis) | `@ExtendWith(MockitoExtension)` | ⚡⚡⚡ | Tek sınıf |
| Unit (repo) | `@DataJpaTest` | ⚡⚡ | JPA katmanı |
| Controller | `@WebMvcTest` | ⚡⚡ | Web katmanı |
| Entegrasyon | `@SpringBootTest` + H2 | ⚡ | Tüm katmanlar |
| Container | `@SpringBootTest` + Testcontainers | 🐢 | Gerçek ortam |

---

## Sonuç

Bu proje, Spring Boot'ta test yazmayı sıfırdan öğrenmek için mükemmel bir yol haritası sunar:

1. **`develop`** → JUnit 5'in tüm assertion'larını öğren
2. **`assertj`** → Fluent, okunabilir assertion stili benimse
3. **`mock`** → Mockito ile bağımlılıkları izole et
4. **`webmvc`** → HTTP katmanını MockMvc ile test et
5. **`data-jpa`** → Repository sorgularını H2 ile doğrula
6. **`spring-boot-test`** → Uçtan uca entegrasyon ve container testleri yaz

Her adım, gerçek dünya uygulamalarında kullanılan production-grade bir pattern'i temsil eder. Bu yol haritasını takip ederek, hem test yazma becerisini hem de Spring Boot'un derinliklerini kavramış olursun.

---

*Bu blog yazısı, [`spring-boot-tdd`](https://github.com/burakcnaksy0/spring-boot-tdd) reposunun tüm branch'leri incelenerek hazırlanmıştır. Yazıdaki tüm kod örnekleri gerçek proje kaynak kodundan alınmıştır.*
