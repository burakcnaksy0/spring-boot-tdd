# Veri Erişim Katmanı Testleri (data-jpa Branch)

Bu branch'in odak noktası, Spring Data JPA Repository'lerinin test edilmesidir. Uygulamanızın veri tabanı ile doğru konuştuğundan emin olmak için bu branch'teki pratikleri inceleyebilirsiniz.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:
1. **`@DataJpaTest` Kullanımı:**
   - Spring Boot'ta sadece Repository katmanına ait fasulyeleri (bean) ayağa kaldıran spesifik bir test anotasyonudur.
   - Gereksiz servis veya controller nesnelerini yüklemediği için testler daha hızlı çalışır.

2. **In-Memory Database (H2) Testleri:**
   - Testleri çalıştırırken asıl veritabanını bozmamak için geçici bir in-memory veritabanı kullanımının konfigüre edilmesi.

3. **Repository CRUD Operasyonları Testi:**
   - Kayıt oluşturma (Save)
   - ID'ye göre sorgulama (FindById)
   - Tüm kayıtları çekme (FindAll)
   - Güncelleme ve Silme (Update & Delete)

4. **Özel JPA Sorgularının Test Edilmesi:**
   - `@Query` ile yazılmış JPQL veya Native sorguların doğruluğunun test edilmesi.
   - Derived Query metotlarının (ör. `findByEmail`) test edilmesi.

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
