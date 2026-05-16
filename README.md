# Spring Boot Temel ve İleri Seviye Test Kavramları (spring-boot-test Branch)

Bu branch, Spring Boot uygulamanızda test yazmaya giriş ve ileri seviye entegrasyon testleri için hazırlanmıştır. Spring Boot'un sunduğu güçlü test altyapısının (H2 in-memory DB ve Testcontainers) genel bir resmini bu branch'te bulabilirsiniz.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:

### 1. Spring Context Yüklenmesi (`@SpringBootTest`)
   - `@SpringBootTest` anotasyonunun nasıl çalıştığı.
   - Uygulama context'inin (bağımlılıkların ve bean'lerin) test ortamı için nasıl tamamen ayağa kaldırıldığı.
   - `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT` kullanımı ile çakışmaların önlenmesi ve gerçek bir web sunucusu (Tomcat) simülasyonu.

### 2. Uygulamanın Ayakta Olduğunun Doğrulanması
   - Spring context'inin başarıyla yüklendiğini doğrulayan basit "Context Loads" (Smoke) testi.
   - Component Injection (`@Autowired`) ile ayağa kalkan bean'lerin test sınıflarına enjekte edilmesi.

### 3. H2 In-Memory Veritabanı ve Profil Yönetimi
   - Testler çalışırken ana veritabanından bağımsız olarak `application-test.properties` gibi test ortamına özel yapılandırma dosyalarının kullanımı (`@ActiveProfiles("test")`).
   - Uçtan uca HTTP testleri için `TestRestTemplate` kullanımı.

### 4. `@Sql` ile Test Verisi Yönetimi
   - `@Sql` anotasyonu sayesinde testlerden önce (`executionPhase = BEFORE_TEST_METHOD`) `setup-test-users.sql` çalıştırılarak hazır verilerin eklenmesi.
   - Testlerden sonra (`executionPhase = AFTER_TEST_METHOD`) `cleanup-test-users.sql` çalıştırılarak veritabanı state'inin sıfırlanması.

### 5. Testcontainers ile Gerçek Veritabanı (PostgreSQL) Entegrasyonu
   - H2 (In-memory) yerine tamamen gerçek bir PostgreSQL veritabanını Docker üzerinde ayağa kaldıran **Testcontainers** kütüphanesinin kullanımı (`UserApplicationContainerTest`).
   - `@DynamicPropertySource` ile rastgele port alan Docker container'ının bağlantı bilgilerinin Spring Boot'a (properties içine) anlık olarak (runtime'da) ezilerek verilmesi.

### 6. Singleton Container Mimarisi (Optimizasyon)
   - Test sürelerini kısaltmak ve kaynak israfını önlemek amacıyla **`BaseContainerTest`** adında abstract bir sınıf oluşturulması.
   - Veritabanı (Postgres) container'ının sadece bir kez `static` blok içerisinde ayağa kaldırılıp tüm test sınıflarında paylaşılması (Singleton Pattern).
   - Veri izolasyonunu sağlamak için `@AfterEach` içerisinde repoların temizlenmesi (`userRepository.deleteAll()`).

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
