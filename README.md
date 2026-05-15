# Spring Boot Temel Test Kavramları (spring-boot-test Branch)

Bu branch, Spring Boot uygulamanızda test yazmaya giriş için hazırlanmıştır. Spring Boot'un sunduğu güçlü test altyapısının genel bir resmini bu branch'te bulabilirsiniz.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:
1. **Spring Context Yüklenmesi (`@SpringBootTest`):**
   - `@SpringBootTest` anotasyonunun nasıl çalıştığı.
   - Uygulama context'inin (bağımlılıkların ve bean'lerin) test ortamı için nasıl tamamen ayağa kaldırıldığı.
   
2. **Uygulamanın Ayakta Olduğunun Doğrulanması:**
   - Spring context'inin başarıyla yüklendiğini doğrulayan basit "Context Loads" (Smoke) testi.

3. **Application Properties Ayarları:**
   - Testler çalışırken `application-test.properties` gibi test ortamına özel yapılandırma dosyalarının kullanımı (`@ActiveProfiles`).

4. **Component Injection (`@Autowired`):**
   - Ayağa kalkan context içindeki fasulyelerin (bean) test sınıflarına `@Autowired` ile nasıl enjekte edildiği.

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
