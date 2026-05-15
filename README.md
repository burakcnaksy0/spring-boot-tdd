# Spring Boot Test Driven Development (TDD) Yol Haritası

Bu proje, Spring Boot uygulamalarında uçtan uca test yazım pratiklerini (TDD - Test Driven Development) adım adım göstermek amacıyla oluşturulmuştur. 

Öğrenme sürecini kolaylaştırmak için konular **farklı branch'lere (dallara)** ayrılmıştır. Her branch, spesifik bir test konusunu ele alır ve o branch'e geçtiğinizde sizi o konuyu detaylıca anlatan özel bir `README.md` dosyası karşılar.

## Branch Rehberi (Hangi Branch'te Ne Var?)

Aşağıdaki listeden öğrenmek veya incelemek istediğiniz konuyu seçip ilgili branch'e geçiş yapabilirsiniz (Örn: `git checkout spring-boot-test`):

1. **`spring-boot-test` Branch'i**
   - **Konu:** Spring Boot test temelleri.
   - **İçerik:** `@SpringBootTest` anotasyonunun kullanımı, Spring Context'inin nasıl ayağa kaldırıldığı, "Context Loads" smoke testleri ve temel application-properties ayarları.

2. **`assertj` Branch'i**
   - **Konu:** Akıcı test doğrulamaları (Assertions).
   - **İçerik:** Klasik JUnit assertion'ları yerine **AssertJ** kütüphanesinin (Fluent API) kullanımı. Değer eşitliği, exception fırlatma (`assertThatThrownBy`) ve okunabilir list/string metot doğrulamaları.

3. **`mock` Branch'i**
   - **Konu:** Servis katmanı testleri ve Mockito kullanımı.
   - **İçerik:** Spring Context ayağa kaldırmadan, **Mockito** kütüphanesi yardımıyla dış bağımlılıkların izole edilmesi. `@Mock`, `@InjectMocks` kullanımı, davranış belirleme (Stubbing) ve çağrıların doğrulanması (Verification).

4. **`data-jpa` Branch'i**
   - **Konu:** Veri erişim katmanı (Repository) testleri.
   - **İçerik:** Sadece Repository bileşenlerini ayağa kaldıran `@DataJpaTest` anotasyonunun kullanımı. H2 in-memory veritabanı konfigürasyonu ve özel JPA sorgularının (Derived & Native Queries) test edilmesi.

5. **`webmvc` Branch'i**
   - **Konu:** Controller (API) testleri ve Entegrasyon Testleri.
   - **İçerik:** Sadece Web katmanını ayağa kaldıran `@WebMvcTest` ile MockMvc kullanımı. Ayrıca Testcontainers (PostgreSQL) ve TestRestTemplate kullanarak gerçek hayat senaryolu **Uçtan Uca (E2E) Entegrasyon Testleri**.

6. **`develop` Branch'i**
   - **Konu:** Aktif Geliştirme.
   - **İçerik:** Tüm bu test pratiklerinin, yeni eklentilerin ve hata düzeltmelerinin entegre edildiği ana geliştirme (development) ortamı.

7. **`master` Branch'i**
   - Şu an bulunduğunuz branch. Projenin ana özetini ve yönlendirmeleri içerir. 

---

### Nasıl Kullanılır?
Bir konuyu detaylıca incelemek için terminalinizden veya kullandığınız IDE üzerinden (örn. IntelliJ, VSCode) o branch'e geçiş yapın:
```bash
git checkout branch_adi
# Örnek: git checkout mock
```
Branch'e geçiş yaptıktan sonra o branch'teki kodları ve size özel hazırlanan `README.md` dosyasını okuyarak konuyu kavrayabilirsiniz. İyi çalışmalar!
