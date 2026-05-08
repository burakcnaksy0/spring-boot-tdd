package com.burakcanaksoy.springboottdd.user;


import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Assertions Örnek Test Sınıfı
 *
 * Kapsanan assertion'lar:
 *  - assertEquals / assertNotEquals
 *  - assertTrue / assertFalse
 *  - assertNull / assertNotNull
 *  - assertThrows
 *  - assertAll
 *  - assertSame / assertNotSame
 *  - assertInstanceOf
 *  - assertDoesNotThrow
 *  - assertIterableEquals
 */
@DisplayName("UserService - JUnit Assertions Demo")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @AfterEach
    void tearDown() {
        userService.clearAll();
    }

    // =========================================================
    // 1. assertEquals & assertNotEquals
    // =========================================================

    @Test
    @DisplayName("assertEquals: Kullanici adi ve kullanici yası dogru set edilmeli")
    void assertEquals_userName() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertEquals("Burak", user.getName(),
                "Kullanici adi 'Burak' olmali");

        assertEquals(25, user.getAge(),
                "Kullanici yasi 25 olmali");
    }

    @Test
    @DisplayName("assertNotEquals: Farkli kullanicilar farkli id almali")
    void assertNotEquals_userIds() {
        User user1 = userService.createUser("Burak", "burak@mail.com", 25);
        User user2 = userService.createUser("Ahmet", "ahmet@mail.com", 30);

        assertNotEquals(user1.getId(), user2.getId(),
                "Iki kullanicinin id'si farkli olmali");

        assertNotEquals(user1.getEmail(), user2.getEmail(),
                "Iki kullanicinin emaili farkli olmali");
    }

    // =========================================================
    // 2. assertTrue & assertFalse
    // =========================================================

    @Test
    @DisplayName("assertTrue: Yeni kullanici aktif olmali")
    void assertTrue_newUserIsActive() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertTrue(user.isActive(),
                "Yeni olusturulan kullanici aktif olmali");

        assertTrue(user.getId() > 0,
                "Kullanici id'si pozitif olmali");
    }

    @Test
    @DisplayName("assertFalse: Deaktif edilen kullanici aktif olmamali")
    void assertFalse_deactivatedUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        userService.deactiveUser(user.getId());

        User deactivatedUser = userService.findById(user.getId());

        assertFalse(deactivatedUser.isActive(),
                "Deaktif edilen kullanici aktif olmamali");
    }

    // =========================================================
    // 3. assertNull & assertNotNull
    // =========================================================

    @Test
    @DisplayName("assertNotNull: Olusturulan kullanici null olmamali")
    void assertNotNull_createdUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertNotNull(user, "Kullanici null olmamali");
        assertNotNull(user.getId(), "Kullanici id'si null olmamali");
        assertNotNull(user.getEmail(), "Kullanici emaili null olmamali");
    }

    @Test
    @DisplayName("assertNull: Bulunmayan email icin Optional bos olmali")
    void assertNull_notFoundByEmail() {
        Optional<User> result = userService.findByEmail("yok@mail.com");

        // Optional bos oldugunda null donduruyor (orElse ile)
        User user = result.orElse(null);

        assertNull(user, "Bulunamayan kullanici null olmali");
    }

    // =========================================================
    // 4. assertThrows
    // =========================================================

    @Test
    @DisplayName("assertThrows: Olmayan kullanici icin UserNotFoundException firlat")
    void assertThrows_userNotFound() {
        // Exception firlatilmali
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(999L),
                "Olmayan id icin UserNotFoundException firlat"
        );

        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    @DisplayName("assertThrows: Gecersiz email ile kullanici olusturma")
    void assertThrows_invalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Burak", "gecersiz-email", 25),
                "Gecersiz email icin IllegalArgumentException firlat"
        );
    }

    @Test
    @DisplayName("assertThrows: Bos isim ile kullanici olusturma")
    void assertThrows_blankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("", "burak@mail.com", 25)
        );
    }

    // =========================================================
    // 5. assertAll — gruplanmis assertions
    // =========================================================

    @Test
    @DisplayName("assertAll: Kullanici alanlari toplu dogrulama")
    void assertAll_userFields() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        // Hepsi calisir; bir tanesi basarisiz olsa bile diger assertion'lar da calisir
        assertAll("Kullanici alanlari",
                () -> assertNotNull(user.getId()),
                () -> assertEquals("Burak", user.getName()),
                () -> assertEquals("burak@mail.com", user.getEmail()),
                () -> assertEquals(25, user.getAge()),
                () -> assertTrue(user.isActive())
        );
    }

    // =========================================================
    // 6. assertDoesNotThrow
    // =========================================================

    @Test
    @DisplayName("assertDoesNotThrow: Gecerli veri ile kullanici olusturma exception firlatmaz")
    void assertDoesNotThrow_validUser() {
        assertDoesNotThrow(
                () -> userService.createUser("Burak", "burak@mail.com", 25),
                "Gecerli veri ile exception firlatilmamali"
        );
    }

    @Test
    @DisplayName("assertDoesNotThrow: Var olan kullanici silinebilmeli")
    void assertDoesNotThrow_deleteUser() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);

        assertDoesNotThrow(
                () -> userService.deleteUser(user.getId())
        );
    }

    // =========================================================
    // 7. assertSame & assertNotSame
    // =========================================================

    @Test
    @DisplayName("assertSame: Ayni obje referansi kontrol")
    void assertSame_sameObject() {
        User user = userService.createUser("Burak", "burak@mail.com", 25);
        User foundUser = userService.findById(user.getId());

        // Ayni obje referansini donduruyor mu?
        assertSame(user, foundUser, "findById ayni objeyi dondurmeli");
    }

    @Test
    @DisplayName("assertNotSame: Farkli kullanicilar farkli referansa sahip olmali")
    void assertNotSame_differentObjects() {
        User user1 = userService.createUser("Burak", "burak@mail.com", 25);
        User user2 = userService.createUser("Ahmet", "ahmet@mail.com", 30);

        assertNotSame(user1, user2, "Farkli kullanicilar farkli referans olmali");
    }

    // =========================================================
    // 8. assertInstanceOf
    // =========================================================

    @Test
    @DisplayName("assertInstanceOf: Servis dogru tip dondurmeli")
    void assertInstanceOf_returnType() {
        Object user = userService.createUser("Burak", "burak@mail.com", 25);

        assertInstanceOf(User.class, user,
                "createUser metodu User tipinde dondurmeli");
    }

    @Test
    @DisplayName("assertInstanceOf: Exception dogru tipte olmali")
    void assertInstanceOf_exceptionType() {
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> userService.findById(999L)
        );

        assertInstanceOf(UserNotFoundException.class, exception,
                "Exception UserNotFoundException olmali");
    }

    // =========================================================
    // 9. assertIterableEquals
    // =========================================================

    @Test
    @DisplayName("assertIterableEquals: Aktif kullaniciler listesi dogrulama")
    void assertIterableEquals_activeUsers() {
        User user1 = userService.createUser("Burak", "burak@mail.com", 25);
        User user2 = userService.createUser("Ahmet", "ahmet@mail.com", 30);

        // user2'yi deaktif et
        userService.deactiveUser(user2.getId());

        List<User> activeUsers = userService.findActiveUsers();
        List<User> expected = List.of(user1);

        assertIterableEquals(expected, activeUsers,
                "Sadece aktif kullanicilar listede olmali");
    }

    // =========================================================
    // Nested Test Ornegi
    // =========================================================

    @Nested
    @DisplayName("Kullanici silme islemleri")
    class DeleteUserTests {

        @Test
        @DisplayName("Silinen kullanici findById ile bulunamaz")
        void deletedUser_notFoundById() {
            User user = userService.createUser("Burak", "burak@mail.com", 25);
            userService.deleteUser(user.getId());

            assertThrows(UserNotFoundException.class,
                    () -> userService.findById(user.getId()));
        }

        @Test
        @DisplayName("Silindikten sonra toplam kullanici sayisi azalir")
        void deletedUser_countDecreases() {
            User user1 = userService.createUser("Burak", "burak@mail.com", 25);
            userService.createUser("Ahmet", "ahmet@mail.com", 30);

            assertEquals(2, userService.getUserCount());

            userService.deleteUser(user1.getId());

            assertEquals(1, userService.getUserCount(),
                    "Silme sonrasi kullanici sayisi 1 olmali");
        }
    }
}