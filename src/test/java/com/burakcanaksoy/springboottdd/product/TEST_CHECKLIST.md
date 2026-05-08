# ProductService Test Senaryoları (Checklist)

Aşağıdaki listeyi testlerinizi yazdıkça işaretleyebilirsiniz (Örn: `[x]`).

### 🛠️ 1. `create()` Metodu Testleri
Bu metot bolca validasyon içerdiği için test edilecek çok senaryosu var.
- [ ] **Başarılı Ürün Ekleme:** Geçerli tüm veriler (isim, kategori, fiyat, stok, açıklama) girildiğinde nesnenin başarıyla oluşturulduğunu doğrula. (`assertNotNull`, `assertEquals`)
- [ ] **Aktiflik Durumu:** Yeni eklenen bir ürünün `isActive()` değerinin varsayılan olarak `true` olduğunu doğrula. (`assertTrue`)
- [ ] **Geçersiz İsim:** İsim null veya boş `""` gönderildiğinde `IllegalArgumentException` fırlatıldığını doğrula. (`assertThrows`)
- [ ] **Geçersiz Kategori:** Kategori ismi null veya boş `""` gönderildiğinde aynı hatayı fırlattığını doğrula. (`assertThrows`)
- [ ] **Negatif Fiyat:** Fiyat `-10` veya `null` gönderildiğinde exception fırlattığını doğrula. (`assertThrows`)
- [ ] **Negatif Stok:** Stok `-5` veya `null` gönderildiğinde exception fırlattığını doğrula. (`assertThrows`)
- [ ] **Geçersiz Açıklama:** Açıklama kısmı boş veya null ise exception fırlattığını doğrula. (`assertThrows`)

### 🔍 2. `getProductById()` Metodu Testleri
- [ ] **Başarılı Bulma:** Önce bir ürün oluştur, sonra o ürünün `id`'si ile arat ve dönen nesnenin eklediğin nesneyle aynı olduğunu doğrula. (`assertEquals`, `assertNotNull`)
- [ ] **Hata Fırlatma:** Olmayan bir ID (örneğin `999L`) ile arama yapıldığında `ProductNotFoundException` fırlatıldığını doğrula. (`assertThrows`)

### 🗑️ 3. `deleteProduct()` Metodu Testleri
- [ ] **Başarılı Silme:** Bir ürün oluştur, `id`'sini alarak `deleteProduct` metoduna gönder. Hata fırlatmadan çalıştığını doğrula (`assertDoesNotThrow`). Ardından `getAllProduct().size()` değerinin azaldığını veya ürünü ID ile aratınca exception aldığını kontrol et.
- [ ] **Olmayan Ürünü Silme:** Sistemde olmayan bir ürün silinmek istendiğinde `ProductNotFoundException` fırlattığını doğrula. (`assertThrows`)

### 📋 4. `getAllProduct()` Metodu Testleri
- [ ] **Boş Liste:** Hiç ürün eklenmeden bu metot çağırıldığında boş bir liste (`[]`) dönmeli. Boyutunun 0 olduğunu doğrula. (`assertEquals(0, list.size())` veya `assertTrue(list.isEmpty())`)
- [ ] **Dolu Liste:** 3 farklı ürün ekle ve boyutun 3 olduğunu doğrula. (`assertEquals(3, list.size())`)

### ✅ 5. `getActiveProducts()` Metodu Testleri
- [ ] **Aktifleri Filtreleme:** 2 ürün oluştur. Bir tanesini `deactivateProduct` ile deaktif et. Ardından bu metodu çağır ve dönen listenin boyutunun 1 olduğunu ve dönen ürünün aktif (`isActive() == true`) olduğunu doğrula.

### 🚫 6. `deactivateProduct()` Metodu Testleri
- [ ] **Başarılı Deaktif Etme:** Oluşturulan aktif bir ürünü bu metoda gönder ve işlemin ardından dönen nesnenin `isActive()` değerinin `false` olduğunu doğrula. (`assertFalse`)
- [ ] **Olmayan Ürünü Deaktif Etme:** Geçersiz bir ID gönderildiğinde `ProductNotFoundException` fırlattığını doğrula. (`assertThrows`)

### 🏷️ 7. `getProductsByCategory()` Metodu Testleri
- [ ] **Doğru Kategori Bulma:** "Elektronik" kategorisinde 2 ürün, "Giyim" kategorisinde 1 ürün oluştur. "Elektronik" diye arattığında listende sadece o 2 ürünün geldiğini doğrula. (`assertEquals`)
- [ ] **Büyük/Küçük Harf Duyarsızlık (Case-Insensitive):** Kategoriyi "eLEkTronik" olarak arattığında da aynı 2 ürünü bulabildiğini doğrula (çünkü kodda `equalsIgnoreCase` kullandınız).
- [ ] **Olmayan Kategori:** Sistemde hiç olmayan bir kategori (Örn: "Mobilya") aratıldığında boş liste döndüğünü doğrula.
