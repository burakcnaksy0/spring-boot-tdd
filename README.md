# AssertJ ile Akıcı Test Doğrulamaları (assertj Branch)

Bu branch'in odak noktası, test sonuçlarını doğrulamak için (assertions) kullanılan güçlü ve akıcı bir kütüphane olan **AssertJ**'nin öğrenilmesi ve uygulanmasıdır. Klasik JUnit assertion'ları yerine neden AssertJ tercih edildiğini bu branch'teki örneklerle görebilirsiniz.

## Bu Branch'te Öğrenilecek ve Uygulanacak Konular:
1. **Fluent API (Akıcı Yazım):**
   - Okunabilirliği yüksek, İngilizce cümle gibi okunabilen test doğrulamaları yazmak.
   - Örn: `assertThat(kullanici.Yasi()).isGreaterThan(18)`

2. **Kapsamlı Doğrulama Metotları:**
   - Nesneler arası değer eşitliği (`isEqualTo`), null durumları (`isNotNull`, `isNull`).
   - Listeler/Koleksiyonlar için özel metotlar (`hasSize`, `contains`, `isEmpty`).
   - String'ler için özel metotlar (`startsWith`, `containsIgnoringCase`).

3. **Hata (Exception) Doğrulamaları:**
   - Belirli bir işlemin istenen exception'ı fırlattığını doğrulamak (`assertThatThrownBy`).
   - Fırlatılan exception'ın mesajını kontrol etmek (`hasMessageContaining`).

4. **Kendi Özel Assertion'larınızı Yazmak:**
   - İhtiyaç dahilinde kendi assertion mantığınızı çıkartmak ve test dosyalarının temiz (clean code) kalmasını sağlamak.

> 💡 **İpucu:** Diğer test aşamalarını ve branch'leri incelemek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
