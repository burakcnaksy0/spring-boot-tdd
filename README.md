# Spring Boot Entegrasyon ve Konteyner Testleri (spring-boot-test Branch) 🚀

Bu branch, Spring Boot uygulamanızda test piramidinin en tepesinde yer alan **Uçtan Uca (E2E) Entegrasyon Testleri** ve **Konteyner Mimarisi** için hazırlanmıştır. Gerçek bir üretim (production) ortamını simüle etmek amacıyla **REST-Assured**, **Docker Testcontainers (PostgreSQL)** ve gelişmiş yerel test optimizasyonları kullanılmıştır.

---

## 🛠️ Bu Branch'te Neler Öğreneceksiniz?

### 1. REST-Assured ile BDD Tarzı API Doğrulama
* **Given-When-Then** DSL (Domain Specific Language) yapısı ile insan dili gibi okunabilen API testleri yazımı.
* JSON Path sorguları ve Hamcrest Matcher kütüphaneleri ile akıcı (fluent) gövde ve durum kodu doğrulamaları.
* İstek gövdelerinde raw JSON String'ler yerine tip güvenliği (Type-safety) sağlayan **Lombok Builder** ve otomatik **Jackson Serialization** kullanımı.

### 2. Testcontainers ile Gerçek PostgreSQL Mimarisi
* H2 gibi in-memory veritabanlarının kısıtlamalarından kurtulup, Docker üzerinde gerçek **PostgreSQL 14** ayağa kaldırma.
* **Singleton Container Pattern (`BaseContainerTest`):** Her test sınıfı için ayrı konteyner başlatma maliyetini önleyerek, tüm test paketi için tek bir `static` konteyner paylaşımı.
* `@DynamicPropertySource` anotasyonu ile konteynerın dinamik port ve bağlantı bilgilerinin Spring Boot'a çalışma zamanında (runtime) aktarılması.

### 3. Gelişmiş Yerel Test Hızlandırma & DataGrip Entegrasyonu (Reuse)
* **Konteyner Yeniden Kullanımı (`withReuse(true)`):** Testler bittiğinde veritabanının yok edilmesini önleyerek bir sonraki çalıştırmada milisaniyeler içinde başlamasını sağlama.
* **Ryuk Temizleyicisini Kapatma (`ryuk.disabled=true`):** JVM kapandığında veritabanının kapatılmasını önleme ve böylece testler açık olmasa bile **DataGrip** gibi araçlarla veritabanına bağlanabilme.
* Konteynerı yerel makinede sabit bir porta (**`15432`**) eşleme (port binding).

### 4. Sağlam (Robust) Test Stratejileri
* **Dinamik ID Yönetimi:** Veritabanındaki sayaçların (sequences) değişmesinden etkilenmeyen dinamik ID ve hata mesajı doğrulamaları.
* **Veri İzolasyonu:** Her testten önce `@BeforeEach` içinde `userRepository.deleteAll()` yardımıyla temiz bir veritabanı durumu sağlama.
* **Negatif & Validasyon Testleri:** `@Valid` anotasyon kısıtlamalarını (`@NotBlank`, `@Min`, vb.) test eden ve hata yönetimi `validationErrors` eşleşmelerini doğrulayan negatif akış testleri.

---

## 💻 Entegrasyon Testi Yapısı

### 1. Singleton Konteyner & Sabit Port Yapılandırması (`BaseContainerTest.java`)
[BaseContainerTest.java](file:///home/burakcan/Desktop/backend-roadmap/spring-boot-tdd/src/test/java/com/burakcanaksoy/springboottdd/user/BaseContainerTest.java) sınıfı, Testcontainers bağımlılığını yönetir:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContainerTest {
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:14.23")
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_password")
                .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(15432), new ExposedPort(5432))
                ))
                .withReuse(true);
        POSTGRES_CONTAINER.start();
    }
    // ... @DynamicPropertySource yapılandırması
}
```

### 2. REST-Assured ile API Test Sınıfı (`UserControllerTest.java`)
[UserControllerTest.java](file:///home/burakcan/Desktop/backend-roadmap/spring-boot-tdd/src/test/java/com/burakcanaksoy/springboottdd/user/UserControllerTest.java) sınıfında hem Happy-Path hem de olumsuz durumları test eden kurumsal seviye testler yer alır:

```java
@Test
void shouldAddNewUser() {
    UserCreateRequest newUserRequest = UserCreateRequest.builder()
            .firstName("Zeynep").lastName("Kara").username("zeynepkara")
            .email("zeynep@test.com").phone("05071234567").age(31).build();

    given()
            .contentType(ContentType.JSON)
            .body(newUserRequest)
            .when()
            .post("/api/user")
            .then()
            .statusCode(201)
            .body("firstName", equalTo("Zeynep"));
}

@Test
void shouldNotCreateUserWhenValidationFails() {
    UserCreateRequest invalidRequest = UserCreateRequest.builder()
            .firstName("").username("zk").email("invalid-email").phone("123").age(15).build();

    given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
            .when()
            .post("/api/user")
            .then()
            .statusCode(400)
            .body("message", equalTo("Validation failed"))
            .body("validationErrors", hasKey("firstName"))
            .body("validationErrors", hasKey("username"));
}
```

---

## ⚡ Yerel Makinede Çalıştırma ve Veritabanına Bağlanma

1. Bilgisayarınızda Docker'ın çalıştığından emin olun.
2. Projeyi maven wrapper ile test edin:
   ```bash
   ./mvnw test
   ```
3. Test bittikten sonra bile veritabanına DataGrip üzerinden şu bilgilerle anında bağlanın:
   * **Host:** `localhost`
   * **Port:** `15432`
   * **Database:** `test_db`
   * **User:** `test_user`
   * **Password:** `test_password`

> 💡 **Not:** Diğer test aşamalarını ve projenin genel ilerleyişini incelemek için `master` branch'indeki ana README.md dosyasına ve tüm test aşamalarını sıfırdan anlatan [BLOG.md](file:///home/burakcan/Desktop/backend-roadmap/spring-boot-tdd/BLOG.md) dosyasına göz atabilirsiniz.
