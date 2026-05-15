# Geliştirme (Develop) Branch'i

Bu branch, ana geliştirme (development) ortamıdır. Tüm yeni özellikler, test kodları (birim testler, entegrasyon testleri) ve hata düzeltmeleri önce bu branch üzerinde birleştirilir. Uygulamanın en güncel ancak tam olarak "prod" ortamına çıkmaya hazır olmayan halini içerir.

## Bu Branch'in Amacı:
1. **Entegrasyon Noktası:** Farklı feature (özellik) branch'lerinde yapılan geliştirmeler (örneğin test altyapısı, mock kütüphaneleri eklemeleri vb.) bu branch'te toplanır.
2. **Sürekli Geliştirme (Continuous Development):** Uygulamanın son kod blokları ve yeni test dosyaları sürekli olarak buraya aktarılır.
3. **Master Öncesi Hazırlık:** Bu branch'te kod derlenir, tüm testler koşulur ve her şey stabil çalışıyorsa kod `master` branch'ine gönderilir (merge).

## Neler Bulunuyor?
Bu branch, projedeki tüm test pratiklerinin (`spring-boot-test`, `assertj`, `mock`, `data-jpa`, `webmvc`) güncel kodlarını bir arada barındırabilir.

> 💡 **İpucu:** Projenin içerdiği konseptlerin detaylı listesini, hangi branch'te nelerin işlendiğini görmek için `master` branch'indeki ana README.md dosyasına göz atabilirsiniz.
