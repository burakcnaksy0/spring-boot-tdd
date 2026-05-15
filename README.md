# Servis Katmanı Testleri ve Mockito (mock Branch)

Bu branch'in odak noktası, Service katmanındaki iş kurallarının (Business Logic) dış bağımlılıklardan (örn. veritabanı, repository) izole edilerek test edilmesidir. Bunu başarmak için **Mockito** kütüphanesi kullanılmaktadır.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:
1. **İzolasyon ve Unit Test Kavramı:**
   - Spring Context ayağa kaldırmadan (`@ExtendWith(MockitoExtension.class)`) tamamen izole ve ultra hızlı birim testleri (unit test) yazmak.

2. **`@Mock` ve `@InjectMocks` Kullanımı:**
   - Dış bağımlılıkların (örneğin `UserRepository`) `@Mock` ile sahtelerinin (mock nesnelerin) oluşturulması.
   - Test edilecek asıl sınıfın (`UserService`) içerisine `@InjectMocks` ile bu sahte nesnelerin enjekte edilmesi.

3. **Mockito Davranış Belirleme (Stubbing):**
   - `when(...).thenReturn(...)` metotlarıyla mock nesnelerin nasıl davranacağının belirlenmesi.
   - Hata (Exception) fırlatma senaryolarının (`when(...).thenThrow(...)`) simüle edilmesi.

4. **Metot Çağrılarının Doğrulanması (Verification):**
   - `verify(...)` kullanarak bağımlılıklardaki bir metodun doğru parametrelerle ve doğru sayıda çağrılıp çağrılmadığının kontrol edilmesi.

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
