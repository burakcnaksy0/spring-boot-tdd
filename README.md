# Spring Boot TDD - JUnit 5 Assertions

Bu proje, Spring Boot ortamında **Test Driven Development (TDD)** prensiplerini ve **JUnit 5** kütüphanesinin sunduğu assertion (doğrulama) yöntemlerini detaylıca öğrenmek için hazırlanmış bir örnek uygulamadır.

## Proje İçeriği

Proje, basit bir kullanıcı yönetim sistemini simüle eder:
- **`User.java`**: Kullanıcı bilgilerini (`id`, `name`, `email`, `age`, `active`) tutan model sınıfı. (Lombok kullanılmıştır).
- **`UserService.java`**: In-memory (bellek içi) bir liste üzerinde kullanıcı oluşturma, silme, deaktif etme ve bulma işlemlerini yürüten servis katmanı.
- **`UserServiceTest.java`**: JUnit 5 kullanılarak servisin çeşitli metodlarını test eden ve tüm önemli assertion metotlarını örneklendiren test sınıfı.

## JUnit 5 Assertions (Doğrulamalar)

`UserServiceTest` sınıfı içinde aşağıdaki JUnit 5 assertion metotlarının kullanımı detaylıca örneklendirilmiştir:

### 1. Eşitlik Kontrolleri (`assertEquals` / `assertNotEquals`)
- **`assertEquals(expected, actual, message)`**: Beklenen değerin gerçek değere eşit olup olmadığını kontrol eder.
  *Örnek*: Yeni oluşturulan kullanıcının yaşının 25 olup olmadığı.
- **`assertNotEquals(unexpected, actual, message)`**: Beklenmeyen değer ile gerçek değerin birbirinden farklı olup olmadığını kontrol eder.
  *Örnek*: İki farklı kullanıcının ID'lerinin veya e-posta adreslerinin aynı olmaması durumu.

### 2. Doğruluk Kontrolleri (`assertTrue` / `assertFalse`)
- **`assertTrue(condition, message)`**: Verilen koşulun `true` olup olmadığını kontrol eder.
  *Örnek*: Yeni oluşturulan bir kullanıcının varsayılan olarak aktif (`user.isActive()`) olması.
- **`assertFalse(condition, message)`**: Verilen koşulun `false` olup olmadığını kontrol eder.
  *Örnek*: Deaktif edilmiş bir kullanıcının aktiflik durumunun `false` olması.

### 3. Null Kontrolleri (`assertNull` / `assertNotNull`)
- **`assertNotNull(actual, message)`**: Nesnenin veya değerin `null` olmadığını doğrular.
  *Örnek*: Oluşturulan kullanıcı nesnesinin kendisinin ve atanan ID'sinin null olmaması.
- **`assertNull(actual, message)`**: Nesnenin `null` olduğunu doğrular.
  *Örnek*: Olmayan bir email adresi arandığında servisin döndürdüğü değerin null olması.

### 4. Hata (Exception) Kontrolleri (`assertThrows` / `assertDoesNotThrow`)
- **`assertThrows(expectedType, executable, message)`**: Belirtilen kod bloğunun (lambda ifadesi), beklenen hata tipini fırlatıp fırlatmadığını kontrol eder.
  *Örnek*: Olmayan bir ID ile kullanıcı arandığında `UserNotFoundException` fırlatılması veya geçersiz bir email girildiğinde `IllegalArgumentException` alınması.
- **`assertDoesNotThrow(executable, message)`**: Kod bloğunun hiçbir hata fırlatmadan (başarıyla) çalışıp çalışmadığını doğrular.
  *Örnek*: Geçerli parametrelerle kullanıcı oluşturma işleminin exception üretmemesi.

### 5. Toplu Doğrulama (`assertAll`)
- **`assertAll(heading, executables...)`**: Birden fazla assertion'ı aynı anda çalıştırır. Normalde bir testte ilk hata veren assertion, altındaki kodların çalışmasını durdurur. Ancak `assertAll` tüm doğrulamaları çalıştırır ve en sonunda tüm hataları raporlar.
  *Örnek*: Kullanıcı adının, yaşının, e-postasının ve statüsünün topluca doğrulanması.

### 6. Referans Kontrolleri (`assertSame` / `assertNotSame`)
- **`assertSame(expected, actual, message)`**: İki nesnenin bellekte **aynı referansa** sahip olup olmadığını kontrol eder (Yani tıpatıp aynı objeler mi?).
  *Örnek*: In-memory listeden çekilen kullanıcının, oluşturulan kullanıcı objesi ile aynı referansta olması.
- **`assertNotSame(unexpected, actual, message)`**: İki nesnenin bellek referanslarının farklı olduğunu doğrular.

### 7. Tip Kontrolü (`assertInstanceOf`)
- **`assertInstanceOf(expectedType, actual, message)`**: Nesnenin beklenen sınıftan veya o sınıfın alt tipinden bir instance (örnek) olup olmadığını kontrol eder.
  *Örnek*: Hata fırlatma durumunda dönen nesnenin `UserNotFoundException` tipinde olması veya oluşturulan nesnenin `User` tipinde olması.

### 8. Koleksiyon Kontrolü (`assertIterableEquals`)
- **`assertIterableEquals(expectedIterable, actualIterable, message)`**: İki iterable (örneğin List) koleksiyonunun eleman sayısı ve sırasıyla beraber birbiriyle aynı olup olmadığını doğrular.
  *Örnek*: Aktif kullanıcıların listesinin sadece beklediğimiz aktif kullanıcıyı içermesi.

## JUnit Yaşam Döngüsü ve Ek Notasyonlar
Projeyi incelerken dikkatinizi çekebilecek diğer özellikler:
- `@BeforeEach` & `@AfterEach`: Her test çalışmadan önce (örn. UserService init) ve çalıştıktan sonra (örn. listenin temizlenmesi `clearAll()`) tetiklenir. Her testin birbirinden bağımsız (isolated) çalışmasını sağlar.
- `@DisplayName`: Testlerin çalışırken IDE'de ve konsolda daha okunaklı isimlerle görünmesini sağlar.
- `@Nested`: Testleri mantıksal gruplara ayırmayı sağlar (Örn: `Kullanici silme islemleri` grubu), böylece okunabilirlik ve organizasyon artar.
