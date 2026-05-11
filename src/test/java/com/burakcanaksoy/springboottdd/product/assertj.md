AssertJ Java dünyasında kullanılan bir **assertion library**’dir.

Önce assertion kavramını netleştirelim.

---

# Assertion nedir?

Testin sonucu doğrulama mekanizması.

Yani:

“Beklediğim şey gerçekten oldu mu?”

Örnek:

```java id="7g7k3g"
int result = 2 + 3;

assertEquals(5, result);
```

Burada assertion:

```java id="gh7j97"
assertEquals(5, result);
```

Eğer result 5 değilse test fail olur.

---

# AssertJ nedir?

AssertJ:

* assertion yazmayı kolaylaştırır
* daha okunabilir hale getirir
* fluent API sunar
* chaining destekler

Yani:

```java id="9n78xg"
assertThat(user.getName())
    .isEqualTo("Burak");
```

Şeklinde doğal okunur.

---

# JUnit5 mi?

Hayır.

Bu kritik ayrım.

| Teknoloji | Görev             |
| --------- | ----------------- |
| JUnit     | test framework    |
| AssertJ   | assertion library |

---

# JUnit5 ne yapar?

JUnit:

* test çalıştırır
* lifecycle yönetir
* annotation sağlar

Örnek:

```java id="zjlwmr"
@Test
void shouldReturnUser() {

}
```

`@Test` → JUnit

---

# AssertJ ne yapar?

Testin içindeki doğrulamaları yapar.

Örnek:

```java id="sxqgn4"
assertThat(user.getName())
    .isEqualTo("Burak");
```

---

# Peki JUnit’in kendi assertion’ı yok mu?

Var.

```java id="xcr8kc"
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
```

Ama AssertJ daha güçlü.

---

# Neden AssertJ kullanılıyor?

Çünkü:

## 1. Daha okunabilir

JUnit:

```java id="jq0r7m"
assertEquals("Burak", user.getName());
```

AssertJ:

```java id="1ek7h4"
assertThat(user.getName())
    .isEqualTo("Burak");
```

İkincisi daha doğal okunuyor.

---

# 2. Fluent API

Chain yapılabiliyor.

```java id="94rqj5"
assertThat(users)
    .hasSize(3)
    .contains(user)
    .doesNotContain(admin);
```

JUnit’de bu kadar temiz değil.

---

# 3. Daha güçlü collection testleri

Örnek:

```java id="2i3lwo"
assertThat(users)
    .extracting(User::getName)
    .containsExactly("Ali", "Veli");
```

Bu çok profesyonel kullanım.

---

# 4. Exception testleri çok temiz

JUnit:

```java id="sqfpny"
assertThrows(RuntimeException.class,
    () -> service.process());
```

AssertJ:

```java id="oqsfwk"
assertThatThrownBy(() -> service.process())
    .isInstanceOf(RuntimeException.class)
    .hasMessage("User not found");
```

Daha güçlü.

---

# assertThat nedir?

AssertJ’in giriş noktası.

Her assertion onunla başlar.

```java id="h2n7jl"
assertThat(value)
```

Sonra tipine göre method zinciri gelir.

---

# En önemli assertThat metodları

# 1. isEqualTo

Equality

```java id="5vjlwm"
assertThat(result)
    .isEqualTo(5);
```

---

# 2. isNotEqualTo

```java id="5m8i4w"
assertThat(result)
    .isNotEqualTo(10);
```

---

# 3. isNull / isNotNull

```java id="r6n0th"
assertThat(user)
    .isNotNull();
```

---

# 4. isTrue / isFalse

```java id="3t8i8f"
assertThat(active)
    .isTrue();
```

---

# 5. contains

String veya collection.

```java id="1j14ha"
assertThat(names)
    .contains("Burak");
```

String:

```java id="x7x3jn"
assertThat(message)
    .contains("success");
```

---

# 6. hasSize

Collection size.

```java id="b35a4x"
assertThat(users)
    .hasSize(3);
```

---

# 7. startsWith / endsWith

```java id="afn60w"
assertThat(email)
    .endsWith("@gmail.com");
```

---

# 8. containsExactly

Sıralı equality.

```java id="u1utn2"
assertThat(numbers)
    .containsExactly(1,2,3);
```

Order önemlidir.

---

# 9. containsExactlyInAnyOrder

Order önemli değil.

```java id="2tjlwm"
assertThat(numbers)
    .containsExactlyInAnyOrder(3,1,2);
```

---

# 10. extracting

Object property extraction.

Çok önemli.

```java id="pbjlwm"
assertThat(users)
    .extracting(User::getName)
    .containsExactly("Ali", "Veli");
```

Bu production-grade kullanım.

---

# 11. anyMatch

Predicate assertion.

```java id="guh01d"
assertThat(users)
    .anyMatch(user -> user.getAge() > 18);
```

---

# 12. allMatch

```java id="twnqhi"
assertThat(users)
    .allMatch(user -> user.isActive());
```

---

# 13. assertThatThrownBy

Exception testing.

```java id="o08ubv"
assertThatThrownBy(() -> service.findUser(1L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessage("User not found");
```

Bu çok kritik.

Backend’de sürekli kullanılır.

---

# 14. usingRecursiveComparison

Deep object compare.

```java id="w9o0zb"
assertThat(actualUser)
    .usingRecursiveComparison()
    .isEqualTo(expectedUser);
```

DTO testlerinde güçlüdür.

---

# Dependency nasıl eklenir?

Maven:

```xml id="kzhmx3"
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

Spring Boot Starter Test kullanıyorsan çoğu zaman zaten gelir.

---

# Profesyonel kullanım şekli

Modern Spring Boot projelerinde tipik stack:

| Amaç           | Tool    |
| -------------- | ------- |
| Test framework | JUnit   |
| Mocking        | Mockito |
| Assertion      | AssertJ |

Birlikte kullanılırlar.

---

# Gerçek örnek

```java id="isah8h"
@Test
void shouldReturnUser() {

    User user = service.getUser(1L);

    assertThat(user)
            .isNotNull();

    assertThat(user.getName())
            .isEqualTo("Burak");

    assertThat(user.getEmail())
            .contains("@gmail.com");
}
```

---

# AssertJ neden senior-level tool sayılır?

Çünkü:

* expressive
* maintainable
* readable
* güçlü collection assertion’ları var
* failure message’ları daha iyi

Özellikle büyük projelerde okunabilirlik kritik.

---

# Sana önemli mühendislik tavsiyesi

Junior yaklaşım:

```java id="v8n52e"
assertEquals(...)
assertEquals(...)
assertEquals(...)
```

Senior yaklaşım:

* expressive assertions
* domain-oriented test
* readable test language

AssertJ bunu sağlar.

---

Senin sıradaki hedefin şu olmalı:

1. JUnit lifecycle öğren
2. AssertJ assertions öğren
3. Mockito mocking öğren
4. SpringBootTest katmanına geç

Ve özellikle:

* collection assertions
* exception assertions
* recursive comparison

bunları iyi öğren.

Çünkü gerçek backend projelerinde en çok bunlar kullanılır.
