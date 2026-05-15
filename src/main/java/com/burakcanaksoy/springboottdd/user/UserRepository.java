package com.burakcanaksoy.springboottdd.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT COUNT(*) FROM User u")
    long countAllUsers();

    @Query("SELECT u FROM User u WHERE u.age >= :age AND u.active = true")
    List<User> findActiveUsersOlderThan(@Param("age") int age);

    @Query("SELECT u FROM User u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    List<User> findByFullName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    // JPQL ile UPDATE işlemi (Veri güncellerken @Modifying kullanılması zorunludur)
    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    int deactivateUser(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM users WHERE email LIKE CONCAT('%', :domain, '%')", nativeQuery = true)
    List<User> findUsersByEmailDomain(@Param("domain") String domain);

    // Native SQL ile DELETE işlemi (Yine @Modifying gerekir)
    @Modifying
    @Query(value = "DELETE FROM users WHERE active = false", nativeQuery = true)
    int deleteInactiveUsers();
}
