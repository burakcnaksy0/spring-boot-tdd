# WebMvc ve Entegrasyon Testleri (webmvc Branch)

Bu branch'in odak noktası **Controller katmanının test edilmesi** ve **Uçtan Uca (End-to-End) Entegrasyon Testleri** yazılmasıdır.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:
1. **`@WebMvcTest` Kullanımı:**
   - Spring context'ini tamamen ayağa kaldırmadan sadece web katmanını (Controller'ları) test etme.
   - Bağımlılıkların (Service katmanı vb.) `@MockBean` ile mock'lanması.

2. **`MockMvc` ile HTTP İstekleri:**
   - GET, POST, PUT, DELETE isteklerinin simüle edilmesi.
   - HTTP response statülerinin (200 OK, 201 CREATED, 400 BAD REQUEST vb.) doğrulanması.
   - JSON request/response body'lerinin test edilmesi (Jackson ve JSON Path kullanımı).

3. **Uçtan Uca Entegrasyon Testleri (`@SpringBootTest`):**
   - `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` ile gerçek bir sunucu ayağa kaldırma.
   - `TestRestTemplate` kullanarak gerçek HTTP istekleri yapma.

4. **Testcontainers ile Gerçek Veri Tabanı Testi:**
   - H2 in-memory db yerine, Docker üzerinde çalışan gerçek bir PostgreSQL instance'ı ayağa kaldırma.
   - `@Testcontainers` ve `@Container` yapıları ile PostgreSQL entegrasyonu.

> 💡 **İpucu:** Diğer konuları incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
