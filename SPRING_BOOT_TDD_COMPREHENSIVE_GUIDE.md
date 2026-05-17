# Spring Boot ile Sıfırdan Zirveye Test-Driven Development (TDD) Rehberi

Test-Driven Development (TDD - Test Güdümlü Geliştirme), modern yazılım mühendisliğinde kod kalitesini artırmak, hataları (bug) en aza indirmek ve refactoring (kod iyileştirme) süreçlerini korkusuzca yapabilmek için kabul görmüş en güçlü metodolojilerden biridir. TDD'nin temel felsefesi oldukça basittir: **"Önce testi yaz, testin başarısız olduğunu gör (Red), testi geçecek minimum kodu yaz (Green) ve ardından kodu temizle/iyileştir (Refactor)."**

Bu kapsamlı rehberde, bir Spring Boot uygulamasında test süreçlerini en alt birimden (Unit Test) en üst entegrasyon seviyesine (E2E / Testcontainers) kadar, projemizdeki gerçek kod örnekleriyle adım adım ele alacağız.

---

## 1. Test Piramidi (Testing Pyramid) Nedir?

Yazılım testlerini kurgularken referans aldığımız en temel model **Test Piramididir**. Piramit bize hangi test türünden ne kadar yazmamız gerektiği konusunda rehberlik eder:

```
                    ▲
                   /|\
                  / | \
                 /  |  \    @SpringBootTest + Testcontainers (E2E Entegrasyon)
                /   |   \   [Yavaş ama En Güvenilir]
               / Entegrasyon\
              /───────────────\
             /                 \
            /    Controller      \  @WebMvcTest + MockMvc (Dilim Testler)
           /     (MockMvc)        \  [Orta Hızlı, Web Katmanı Odaklı]
          /─────────────────────────\
         /                           \
        /       Repository Katmanı    \  @DataJpaTest (Veritabanı Dilim Testi)
       /         (H2 / Test DB)        \  [Orta Hızlı, SQL & JPQL Odaklı]
      /───────────────────────────────────\
     /                                     \
    /       Birim Testler (Unit Tests)      \  JUnit 5 + Mockito / AssertJ
   /         (Pure Java Logic)               \  [Işık Hızında, En Çok Yazılan]
  /───────────────────────────────────────────\
```

*   **Birim Testler (Unit Tests):** Piramidin tabanını oluşturur. Dış bağımlılıklardan (veritabanı, ağ, framework vb.) tamamen izole, sadece iş mantığına (business logic) odaklanan ve milisaniyeler içinde çalışan testlerdir.
*   **Dilim Testler (Slice Tests):** Spring Boot uygulamasının sadece belirli bir katmanını (örneğin sadece web katmanını `@WebMvcTest` ile ya da sadece veritabanı katmanını `@DataJpaTest` ile) ayağa kaldırarak yapılan odaklanmış entegrasyon testleridir.
*   **Entegrasyon ve E2E Testleri:** Tüm katmanların (Controller, Service, Repository, gerçek Veritabanı) bir arada ve uyum içinde çalışıp çalışmadığını doğrular. `@SpringBootTest` ve gerçek veritabanı Docker container'ları (Testcontainers) ile kurgulanır.

Şimdi bu katmanları sırasıyla, projemizin mimarisine uygun olarak inceleyelim.

---

## 2. JUnit 5 ile Temel Birim Testleri (Unit Testing)

TDD yolculuğuna başlarken ilk adımımız, Spring Boot framework'ünden bağımsız, saf Java sınıflarımızı test etmektir. Bunun için sektör standardı olan **JUnit 5** kütüphanesini kullanırız.

### Projemizden Örnek Senaryo: Bellek İçi `UserService`

Geliştirdiğimiz in-memory (bellek içi) `UserService` sınıfının iş mantığını test edelim. Öncelikle test edeceğimiz basit `User` modelimiz:

```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private int age;
    private boolean active;
}
```

Bu modeli yöneten ve içerisinde doğrulama (validation) kuralları barındıran `UserService` iş mantığı kodumuz:

```java
@Service
public class UserService {
    private List<User> userList = new ArrayList<>();
    private long countId = 1;

    public User createUser(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150.");
        }
        User user = new User(countId++, name, email, age, true);
        userList.add(user);
        return user;
    }

    public User findById(Long id) {
        return userList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
    
    public void clearAll() {
        userList.clear();
        countId = 1;
    }
}
```

### JUnit 5 Assertion (Doğrulama) Kütüphanesi

JUnit 5, test ettiğimiz metodun çıktısının beklediğimiz değerle eşleşip eşleşmediğini kontrol etmek için zengin bir `Assertions` sınıfı sunar. İşte projemizdeki en yaygın JUnit 5 assertion kullanımları:

```java
@DisplayName("UserService - JUnit Assertions Demo")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(); // Her testten önce taze bir servis nesnesi oluşturulur
    }

    @AfterEach
    void tearDown() {
        userService.clearAll(); // Her testten sonra veriler temizlenir
    }

    // 1. assertEquals & assertNotEquals
    @Test
    @DisplayName("assertEquals: Kullanıcı adı ve yaşı doğru set edilmeli")
    void assertEquals_userName() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertEquals("Burak", user.getName(), "Kullanıcı adı 'Burak' olmalı");
        assertEquals(25, user.getAge(), "Kullanıcı yaşı 25 olmalı");
    }

    // 2. assertTrue & assertFalse
    @Test
    @DisplayName("assertTrue: Yeni kullanıcı aktif olmalı")
    void assertTrue_newUserIsActive() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertTrue(user.isActive(), "Yeni oluşturulan kullanıcı aktif olmalı");
    }

    // 3. assertNull & assertNotNull
    @Test
    @DisplayName("assertNotNull: Oluşturulan kullanıcı nesnesi null olmamalı")
    void assertNotNull_createdUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        assertNotNull(user);
        assertNotNull(user.getId());
    }

    // 4. assertThrows (Hata Fırlatma Doğrulaması)
    @Test
    @DisplayName("assertThrows: Geçersiz yaş girildiğinde IllegalArgumentException fırlatılmalı")
    void assertThrows_invalidAge() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Burak", "burak@mail.com", -5),
                "Negatif yaş için hata fırlatılmalı"
        );
        assertEquals("Age must be between 0 and 150.", exception.getMessage());
    }

    // 5. assertAll (Toplu Gruplandırılmış Doğrulamalar)
    // assertAll içindeki testlerden biri başarısız olsa bile diğer testler de koşulmaya devam eder.
    @Test
    @DisplayName("assertAll: Kullanıcı alanlarının toplu doğrulanması")
    void assertAll_userFields() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertAll("Kullanıcı Alanları",
                () -> assertEquals("Burak", user.getName()),
                () -> assertEquals("burak@mail.com", user.getEmail()),
                () -> assertEquals(25, user.getAge()),
                () -> assertTrue(user.isActive())
        );
    }
}
```

---

## 3. AssertJ ile Daha Akıcı (Fluent) ve Okunabilir Doğrulamalar

JUnit 5 assertion'ları işimizi görse de, yazması ve okuması bazen hantal olabilir. Ayrıca test hata verdiğinde üretilen loglar her zaman yeterince açıklayıcı değildir. İşte bu noktada devreye **AssertJ** girer. AssertJ, *"fluent"* (akıcı) bir API sunarak test doğrulamalarınızı neredeyse İngilizce bir cümle okur gibi yazmanızı sağlar.

### Neden AssertJ? (Karşılaştırma)

```java
// JUnit 5 Stili
assertEquals(3, activeProducts.size());
assertTrue(activeProducts.contains(expectedProduct));

// AssertJ Stili
assertThat(activeProducts)
    .hasSize(3)
    .contains(expectedProduct)
    .doesNotContain(inactiveProduct);
```

### Projemizden Örnek Senaryo: `ProductServiceTest` ile AssertJ Gücü

AssertJ'nin ileri düzey özelliklerini (BigDecimal karşılaştırmaları, liste içi filtreleme ve alan bazlı doğrulamalar) projemizin `ProductServiceTest` sınıfı üzerinden inceleyelim:

```java
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
    void shouldCreateProductWithFluentAssertions() {
        // Given
        String name = "Laptop";
        String category = "Electronics";
        BigDecimal price = new BigDecimal("1500.00");
        Integer stock = 10;
        String description = "High performance laptop";

        // When
        Product product = productService.create(name, category, price, stock, description);

        // Then (AssertJ Zincirleme Doğrulama)
        assertThat(product)
                .isNotNull()
                .hasFieldOrPropertyWithValue("categoryName", category)
                .hasFieldOrPropertyWithValue("numberOfStock", stock);

        assertThat(product.getId()).isPositive();
        assertThat(product.getName()).isEqualTo(name);
        
        // BigDecimal karşılaştırmalarında .isEqualTo yerine .isEqualByComparingTo kullanmak
        // scale farklılıklarını (örn: 1500.00 ile 1500) göz ardı ederek güvenli karşılaştırma sağlar.
        assertThat(product.getPrice()).isEqualByComparingTo("1500.00");
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        // AssertJ ile Hata Doğrulama stili
        assertThatThrownBy(() -> productService.create("", "Category", BigDecimal.TEN, 5, "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be blank.");

        // Alternatif Fluent Hata Doğrulama stili
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.create(null, "Category", BigDecimal.TEN, 5, "Desc"))
                .withMessage("Name cannot be blank.");
    }

    @Test
    void shouldGetAllProductsAndExtractFields() {
        // Given
        productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        productService.create("P2", "C2", BigDecimal.TEN, 5, "D2");

        // When
        List<Product> products = productService.getAllProduct();

        // Then (Liste ve Eleman Odaklı Güçlü AssertJ Metodları)
        assertThat(products)
                .isNotEmpty()
                .hasSize(2)
                .extracting(Product::getName) // Listeyi sadece Name alanlarından oluşan bir listeye dönüştürür
                .containsExactly("P1", "P2"); // Sırasıyla tam olarak bu elemanları içermeli
                
        assertThat(products)
                .extracting("categoryName") // Yansıma (reflection) ile alan çıkarma
                .contains("C1", "C2");
    }

    @Test
    void shouldVerifyAllActiveProductsSatisfyCondition() {
        // Given
        Product p1 = productService.create("P1", "C1", BigDecimal.TEN, 5, "D1");
        Product p2 = productService.create("P2", "C1", BigDecimal.TEN, 5, "D2");
        productService.deactivateProduct(p1.getId());

        // When
        List<Product> activeProducts = productService.getActiveProducts();

        // Then
        assertThat(activeProducts)
                .hasSize(1)
                .containsOnly(p2);

        // Listedeki tüm elemanların belirli bir şarta uyup uymadığını test etme
        assertThat(activeProducts).allSatisfy(product -> {
            assertThat(product.isActive()).isTrue();
            assertThat(product.getName()).startsWith("P");
        });
    }
}
```

---

## 4. Mockito ile Bağımlılıkları İzole Etme (Mocking)

Gerçek dünya uygulamalarında sınıflarımız tek başına çalışmaz. Katmanlı mimaride bir `UserService` sınıfı, veritabanına erişmek için `UserRepository`'ye ve nesne dönüşümleri için `UserMapper`'a bağımlıdır. 

Birim test yazarken hedefimiz **sadece test ettiğimiz sınıfın mantığını doğrulamaktır**. Bağımlı olunan sınıfların (Repository, harici servisler vb.) davranışlarını taklit etmek ve izole etmek için **Mockito** kütüphanesini kullanırız.

### Mock Nedir?
**Mock (Sahte Nesne):** Gerçek bir nesnenin arayüzünü (interface) taklit eden, bizim önceden belirlediğimiz senaryolara göre cevap veren boş bir kabuktur.

### Projemizden Örnek Senaryo: Katmanlı Mimari ve `UserServiceTest`

*   **`UserService`** -> Test edeceğimiz asıl hedef sınıf (SUT - System Under Test).
*   **`UserRepository`** -> Veritabanı katmanı (Mock'lanacak bağımlılık).
*   **`UserMapper`** -> DTO dönüşüm katmanı (Mock'lanacak bağımlılık).

```java
@ExtendWith(MockitoExtension.class) // Mockito annotations kullanımını etkinleştirir
class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Sahte repo oluşturulur

    @Mock
    private UserMapper userMapper; // Sahte mapper oluşturulur

    @InjectMocks
    private UserService userService; // Mock'lar otomatik olarak bu servise enjekte edilir

    private User user;
    private UserCreateRequest request;
    private UserResponse response;

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

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        // 1. Arrange (Davranışları Ayarla / Stubbing)
        // userRepository.findById(1L) çağrıldığında içi dolu sahte user nesnesini dön
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // userMapper.toResponse(user) çağrıldığında sahte response nesnesini dön
        when(userMapper.toResponse(user)).thenReturn(response);

        // 2. Act (Çalıştır)
        UserResponse result = userService.getUserById(1L);

        // 3. Assert (Doğrula)
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(response.getId());
        assertThat(result.getUsername()).isEqualTo(response.getUsername());

        // 4. Verify (Davranış Doğrulaması)
        // Bu metodların tam olarak 1 kez çağrılıp çağrılmadığını doğrularız.
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        // Arrange (Kullanıcı veritabanında yoksa sahte repo boş dönsün)
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 1");
        
        verify(userRepository, times(1)).findById(1L);
        // mapper metodunun HİÇ çağrılmadığını doğrularız (Çünkü hata fırladı ve akış kesildi)
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowUserAlreadyExistsException() {
        // Arrange (E-posta adresi sistemde zaten kayıtlı olsun)
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already exists");

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        // E-posta zaten varsa, telefon numarası kontrolüne ve kaydetme işlemine HİÇ geçilmemeli
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any());
    }
}
```

---

## 5. Web Katmanı Testleri: `@WebMvcTest` ve MockMvc

Spring Boot'ta API uç noktalarını (Endpoints) test ederken tüm uygulamayı ve veritabanını ayağa kaldırmak yavaştır ve gereksizdir. Sadece Web katmanını (REST Controller, validation'lar, serialization/deserialization ve exception handler'lar) izole bir şekilde test etmek için Spring'in sunduğu **`@WebMvcTest`** dilim testini kullanırız.

### `@WebMvcTest` Çalışma Prensibi
Bu anotasyon sadece Spring MVC altyapısını yükler. Servis katmanı (`UserService`) Spring Context'e yüklenmez. Bu bağımlılığı taklit etmek için Spring Boot 4 / Spring Framework 6 ile birlikte gelen **`@MockitoBean`** anotasyonunu kullanırız.

### Projemizden Örnek Senaryo: `UserControllerTest`

```java
@WebMvcTest(value = UserController.class) // Sadece UserController'ı test et
@AutoConfigureMockMvc(addFilters = false) // Güvenlik / Security filtrelerini kapat
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP isteklerini simüle eden araç

    @Autowired
    private ObjectMapper objectMapper; // Java nesnelerini JSON'a çevirir

    @MockitoBean
    private UserService userService; // Servis bağımlılığını mock'la

    private UserCreateRequest request;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        request = UserCreateRequest.builder()
                .firstName("John").lastName("Doe").username("johndoe")
                .email("john@example.com").phone("05321234567").age(25).build();

        response = UserResponse.builder()
                .id(1L).firstName("John").lastName("Doe").username("johndoe")
                .email("john@example.com").phone("05321234567").age(25).active(true).build();
    }

    @Test
    void shouldCreateUserAndReturn201Created() throws Exception {
        // Arrange
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        // Act & Assert (HTTP POST İsteği)
        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Nesneyi JSON'a çevirip gövdeye (body) ekle
                .andExpect(status().isCreated()) // HTTP 201 doğrulaması
                .andExpect(jsonPath("$.id").value(1)) // JSON cevabının içeriğini kontrol et
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    void shouldReturnUserByIdAndReturn200Ok() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(response);

        // Act & Assert (HTTP GET İsteği)
        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk()) // HTTP 200 doğrulaması
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldDeleteUserAndReturn204NoContent() throws Exception {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert (HTTP DELETE İsteği)
        mockMvc.perform(delete("/api/user/{id}", userId))
                .andExpect(status().isNoContent()); // HTTP 204 doğrulaması

        verify(userService).deleteUser(userId);
    }
}
```

---

## 6. Veri Erişim Katmanı Testleri: `@DataJpaTest`

Veritabanı işlemleri (sorgular, özel JPQL veya Native SQL'ler) yazılımın en kritik yerleridir. Yazdığımız SQL sorgularının doğru çalışıp çalışmadığını test etmek için Spring'in sunduğu **`@DataJpaTest`** dilim testini kullanırız.

### `@DataJpaTest` Özellikleri:
*   Varsayılan olarak hafif ve hızlı bir **In-Memory (bellek içi) H2 veritabanı** yapılandırır.
*   Sadece `@Entity` ve Spring Data JPA repository'lerini ayağa kaldırır, servisleri veya denetleyicileri yüklemez.
*   **Transactional Rolback:** Her test metodu bittiğinde veritabanına yapılan tüm işlemler otomatik olarak geri alınır (rollback). Böylece testler birbirinden izole kalır.

### Projemizden Örnek Senaryo: `UserRepositoryTest`

Geliştirdiğimiz karmaşık JPQL ve Native SQL sorgularını test edelim. İlk olarak repository arayüzümüz:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // JPQL sorgusu
    @Query("SELECT u FROM User u WHERE u.age >= :age AND u.active = true")
    List<User> findActiveUsersOlderThan(@Param("age") int age);

    // JPQL UPDATE sorgusu (Veri yazma işlemi için @Modifying zorunludur)
    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    int deactivateUser(@Param("userId") Long userId);

    // Native SQL sorgusu
    @Query(value = "SELECT * FROM users WHERE email LIKE CONCAT('%', :domain, '%')", nativeQuery = true)
    List<User> findUsersByEmailDomain(@Param("domain") String domain);
}
```

Repository'yi test eden `@DataJpaTest` sınıfımız:

```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager; // Testler için veritabanına kayıt atmayı kolaylaştıran yardımcı araç

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;

    @BeforeEach
    void setUp() {
        user1 = new User(null, "mert", "ceylan", "mertceylan", "mertceylan@example.com", "05350682758", 27, true);
        user2 = new User(null, "salih", "dursun", "salihdursun", "salihdursun@example.com", "05350886527", 21, true);

        // Verileri H2 veritabanına kaydet ve persistence context'i temizle
        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);
    }

    @Test
    void shouldFindActiveUsersOlderThanSpecificAge() {
        // When (JPQL Sorgusunun Çalıştırılması)
        List<User> result = userRepository.findActiveUsersOlderThan(25);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("mertceylan"); // Sadece 27 yaşındaki mertceylan dönmeli
    }

    @Test
    void shouldFindUsersByEmailDomainWithNativeQuery() {
        // When (Native SQL Sorgusunun Çalıştırılması)
        List<User> result = userRepository.findUsersByEmailDomain("example.com");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getUsername).containsExactlyInAnyOrder("mertceylan", "salihdursun");
    }

    @Test
    @DisplayName("CRITICAL: DeactiveUser metodu kullanıcının active bayrağını false yapmalı")
    void deactivateUser_ShouldSetUserActiveToFalse() {
        // When
        int updatedCount = userRepository.deactivateUser(user1.getId());

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        /* 
         * ⚠️ CRITICAL KURAL: testEntityManager.clear() Neden Zorunludur?
         * Hibernate birinci seviye önbellek (first-level cache / persistence context) kullanır.
         * @Modifying ile doğrudan veritabanında UPDATE çalıştırdığımızda bu durum persistence context'teki
         * nesnelerin güncellenmesine neden olmaz. Eğer clear() yapmazsak, findById çağırdığımızda Hibernate
         * veritabanına gitmek yerine önbellekteki aktif olan eski nesneyi döner ve testimiz hatalı geçer!
         * clear() yaparak önbelleği sıfırlarız ve güncel halini doğrudan veritabanından çekmeye zorlarız.
         */
        testEntityManager.clear(); 
        
        User updatedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(updatedUser.isActive()).isFalse(); // Artık pasif olduğunu başarıyla doğrulayabiliriz
    }
}
```

---

## 7. Uçtan Uca Entegrasyon Testleri (End-to-End Integration Testing)

Piramidin zirvesine ulaştık! Artık uygulamanın tüm parçalarını (HTTP istek katmanı, iş mantığı katmanı, veri erişim katmanı ve gerçek veritabanı) bir araya getirerek gerçek bir istemci (client) gibi testler gerçekleştireceğiz. 

Entegrasyon testlerinde Spring Boot uygulamasının tamamını ayağa kaldırmak için **`@SpringBootTest`** anotasyonunu kullanırız. 

Bu seviyede test veritabanını yönetmek için iki popüler yaklaşım vardır. Bunları detaylıca inceleyelim.

---

### Yaklaşım A: H2 In-Memory Veritabanı ve `@Sql` Anotasyonu

Bu yaklaşımda, entegrasyon testleri H2 in-memory veritabanında koşulur. Testlerin ihtiyaç duyduğu başlangıç verileri (seed data) ise `.sql` dosyaları aracılığıyla test metotlarından önce ve sonra çalıştırılır.

#### 1. SQL Scriptlerinin Hazırlanması

*   **`src/test/resources/setup-test-users.sql`:**
    ```sql
    INSERT INTO users (id, first_name, last_name, username, email, phone, age, active)
    VALUES (1, 'TestFirstName', 'TestLastName', 'testuser', 'testuser@gmail.com', '05360623971', 30, true);
    ```
*   **`src/test/resources/cleanup-test-users.sql`:**
    ```sql
    DELETE FROM users;
    ```

#### 2. Test Sınıfının Yazılması

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Uygulamayı rastgele boş bir portta ayağa kaldır
@ActiveProfiles("test") // application-test.properties dosyasını aktif et (H2 veritabanı ayarları)
public class UserApplicationIntegrationTest {

    @LocalServerPort
    private int port; // Ayağa kalkan rastgele portu enjekte et

    @Autowired
    private TestRestTemplate restTemplate; // Gerçek HTTP istekleri atmamızı sağlayan Spring aracı

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = "/setup-test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Testten ÖNCE veriyi ekle
    @Sql(scripts = "/cleanup-test-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // Testten SONRA temizle
    void shouldGetUserById_whenUserExists() {
        // Given
        String url = "http://localhost:" + port + "/api/user/1";

        // When (Gerçek HTTP GET İsteği)
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    @Sql(scripts = "/cleanup-test-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldCreateUser_andSaveToDatabase() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("John").lastName("Doe").username("johndoe")
                .email("john.doe@test.com").phone("05359702361").age(25).build();
        String url = "http://localhost:" + port + "/api/user";

        // When (Gerçek HTTP POST İsteği)
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(url, request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();

        // Entegrasyon kanıtı: Veritabanına gerçekten kaydedildi mi?
        Optional<User> foundUser = userRepository.findById(response.getBody().getId());
        assertThat(foundUser).isPresent();
    }
}
```

---

### Yaklaşım B: Testcontainers ile Gerçek PostgreSQL Veritabanı

H2 veritabanı hızlı ve pratik olsa da, gerçek üretim ortamında kullandığınız PostgreSQL, MySQL gibi veritabanlarıyla birebir aynı özellikleri taşımaz (Örn: PostgreSQL'e özgü JSONB alanları, native fonksiyonlar, diyalekt farkları).

Bu sorunu çözmek için endüstri standardı **Testcontainers** teknolojisidir. Testcontainers, testleriniz koşarken arka planda Docker üzerinden geçici, gerçek bir PostgreSQL container'ı başlatır ve testleriniz bittiğinde bu container'ı otomatik imha eder.

#### 1. Paylaşımlı Konteyner Yapısı (Shared Container Pattern / Singleton)

Container başlatmak maliyetli bir işlemdir (genellikle 3-8 saniye sürer). Her test sınıfı için yeni bir container başlatıp durdurmak yerine, tüm testlerimizin **tek bir ortak container** üzerinde koşmasını sağlayan *Shared Container Pattern* yapısını kurgularız:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContainerTest {

    // static final olarak tanımlandığı için JVM lifecycle boyunca tek bir kere başlatılır (Singleton)
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:14.23")
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_password");
        
        POSTGRES_CONTAINER.start(); // Docker üzerinde PostgreSQL container'ını başlatır
    }

    // @DynamicPropertySource: Container ayağa kalktığında Docker'ın atadığı dinamik rastgele portu
    // otomatik olarak okur ve Spring Boot'un veri kaynağı (datasource) ayarlarına dinamik enjekte eder.
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
```

#### 2. `UserApplicationContainerTest` Yazılması

Artık entegrasyon test sınıfımızı hazırladığımız `BaseContainerTest` sınıfından türeterek gerçek PostgreSQL üzerinde sıfır hata payı ile koşturabiliriz:

```java
public class UserApplicationContainerTest extends BaseContainerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll(); // Her entegrasyon testinden sonra veritabanını sıfırlıyoruz
    }

    @Test
    void shouldCreateUserAndPersistInPostgreSQL() {
        // Given (Konteynerin çalıştığını doğrula)
        assertTrue(POSTGRES_CONTAINER.isRunning());

        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("burakcan").lastName("aksoy").username("burakcnaksy")
                .email("aksoyburak808@gmail.com").phone("05350482740").age(24).build();

        // When (Gerçek HTTP İsteği)
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/user",
                request,
                UserResponse.class
        );

        // Then (İstemci katmanını doğrula)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();

        // Then (Gerçek PostgreSQL veritabanını doğrula - Tam Entegrasyon)
        Optional<User> savedUser = userRepository.findById(response.getBody().getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("aksoyburak808@gmail.com");
    }

    @Test
    void shouldReturn400_whenCreateUserWithInvalidEmailFormat() {
        // Given (Geçersiz e-posta formatı ve doğrulama hatası beklentisi)
        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("burakcan").lastName("aksoy").username("burakcnaksy")
                .email("invalid-email-format") // Hatalı email formatı
                .phone("05350482740").age(15).build();

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/user",
                request,
                String.class
        );

        // Then (Spring Validation'ın 400 Bad Request fırlattığını doğrula)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Veritabanına hiçbir şey kaydedilmemiş olmalı
        assertThat(userRepository.count()).isEqualTo(0);
    }
}
```

---

## 8. Hangi Testi Ne Zaman Kullanmalıyız? (Karşılaştırma Tablosu)

| Özellik | Unit Test (JUnit/AssertJ) | Slice Test (`@DataJpaTest` / `@WebMvcTest`) | Integration Test (`@SpringBootTest` + H2) | E2E Test (`@SpringBootTest` + Testcontainers) |
|---|---|---|---|---|
| **Çalışma Hızı** | ⚡ Milisaniyeler | 🏎️ 1 - 3 Saniye | 🐢 5 - 10 Saniye | 🐌 10 - 20 Saniye |
| **Spring Boot Context** | Yüklenmez | Kısmen Yüklenir (Sadece ilgili dilim) | Tamamen Yüklenir | Tamamen Yüklenir |
| **Veritabanı İhtiyacı** | Yok (Mock) | H2 (In-memory) | H2 (In-memory) | Gerçek Veritabanı (Docker PostgreSQL) |
| **Güvenilirlik Seviyesi** | Düşük-Orta (Sadece kod lojiği) | Orta (Sorgular / Web Kuralları) | Yüksek | **En Yüksek (Üretime En Yakın)** |
| **Birincil Hedef** | Metot içi algoritmalar ve doğrulamalar | SQL sorguları / JSON Validation / HTTP Status | Farklı katmanların uyumu | Gerçek dış dünya entegrasyonu |

---

## Sonuç: Test-Driven Development (TDD) Kültürü

TDD uygulamak sadece test yazmakla ilgili değildir; **tasarım kararlarınızı test edilebilirlik üzerine kurgulama** sanatıdır. Kodunuzu TDD prensipleriyle geliştirdiğinizde:
1.  **Daha Esnek Tasarımlar:** Sıkı sıkıya bağlı olmayan, gevşek bağlı (loosely coupled) ve tek sorumluluk prensibine (Single Responsibility) uygun sınıflar tasarlarsınız.
2.  **Korkusuz Refactoring:** Aylar önce yazdığınız karmaşık bir kodu yeniden yazarken, testleriniz size anlık geri bildirim sağlayarak hiçbir şeyi bozmadığınızdan emin olmanızı sağlar.
3.  **Canlı Dokümantasyon:** Test metotlarının isimleri (`shouldReturn400_whenCreateUserWithInvalidEmailFormat` gibi), uygulamanızın neyi yapıp neyi yapamayacağını gösteren en güncel ve en güvenilir canlı dokümantasyondur.

Spring Boot ekosisteminin sunduğu zengin test araçlarıyla donatılmış bu TDD serüveni, sizi çok daha profesyonel, özgüvenli ve hatasız kod üreten bir kıdemli geliştirici haline getirecektir. 

*Unutmayın: **"Test edilmeyen kod, yazılmamış koddur!"***
