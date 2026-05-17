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

5. **`@Captor` ve `ArgumentCaptor` Kullanımı (Argüman Yakalama):**
   - Bir metodun parametresi olarak gönderilen nesneleri havada yakalayarak içindeki alanları detaylıca test etmek.
   - Test edilen metodun içinde dinamik olarak oluşturulan nesnelerin doğruluğunu denetlemek.

---

##  `@Captor` ve `ArgumentCaptor` Detaylı İncelemesi

Birim testlerinde bazı durumlarda sadece metotların çağrıldığını doğrulamak (`verify`) yetmez; çağrı sırasında iletilen nesnelerin içeriklerini de doğrulamak gerekir. İşte bu noktada **`ArgumentCaptor`** devreye girer.

### 💡 Temel Mantık: Ne Zaman ve Nerede Kullanılmalı?

`ArgumentCaptor`, test edilen metodun **içinde dinamik olarak oluşturulan (mapped)** veya **değişikliğe (mutasyon) uğrayan** nesneleri yakalamak için biçilmiş kaftandır.

| Durum | Tercih Edilen Yaklaşım | Örnek |
| :--- | :--- | :--- |
| **Metot içinde üretilen nesneler** (örn: mapper sonrası) | `ArgumentCaptor` ile yakala ve assert et 🌟 | `userRepository.save(userCaptor.capture())` |
| **Metot içinde alanı değişen nesneler** (örn: şifre şifreleme) | `ArgumentCaptor` ile yakala ve assert et 🌟 | `userRepository.save(userCaptor.capture())` |
| **Dışarıdan gelen ve değişmeyen nesneler** | Doğrudan referans ile `verify` et | `userMapper.toEntity(request)` |
| **Basit / İlkel parametreler** (`String`, `Long` vb.) | Doğrudan değerle `verify` et | `userRepository.existsByEmail("email@email.com")` |

---

### 🛠️ Kullanım Örneği

#### 1. Sınıf Seviyesinde Tanımlama
Sınıfın en üstüne `@Captor` anotasyonu ile eklenir:
```java
@Captor
private ArgumentCaptor<User> userCaptor;
```

#### 2. Test Metodu İçinde Kullanımı (Arrange-Act-Assert Akışı)
Testler her zaman **AAA (Arrange-Act-Assert)** sırasına uymalıdır. `verify` ve `capture` işlemleri **sadece Act (Eylem) aşamasından sonra** yapılabilir.

```java
@Test
void createUser_ShouldReturnUserResponse() {
    // 1. Arrange (Hazırlık)
    when(userRepository.save(any(User.class))).thenReturn(user);

    // 2. Act (Eylem)
    UserResponse result = userService.createUser(request);

    // 3. Assert & Verify (Doğrulama)
    // userRepository.save() metoduna giden nesneyi yakalıyoruz
    verify(userRepository, times(1)).save(userCaptor.capture());
    
    // Yakalanan nesneyi alıp içindeki alanları assert ediyoruz
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
    assertThat(savedUser.getFirstName()).isEqualTo(request.getFirstName());
}
```

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
