package com.burakcanaksoy.springboottdd.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
otomatik olarak bir test işlemi sırasında transactional
davranış sağlar ve test sonunda otomatik olarak rollback yapar
 */
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;

    @BeforeEach
    void setUp() {
        user1 = new User(null,"mert","ceylan","mertceylan","mertceylan@example.com","05350682758",27,true);
        user2 = new User(null,"salih","dursun","salihdursun","salihdursun@example.com","05350886527",21,true);

        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        User newUser = new User(null, "ahmet", "yilmaz", "ahmetyilmaz", "ahmet@example.com", "05555555555", 30, true);

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("ahmetyilmaz");
        assertThat(savedUser.getEmail()).isEqualTo("ahmet@example.com");
    }

    @Test
    void findById_ShouldReturnUser(){
        Optional<User> user = userRepository.findById(user1.getId());

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("mertceylan");
    }

    @Test
    void findAll_ShouldReturnAllUsers(){
        List<User> userList = userRepository.findAll();

        assertThat(userList).hasSize(2);
        assertThat(userList).extracting(User::getPhone)
                .containsExactlyInAnyOrder("05350682758","05350886527");
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully(){
        user2.setPhone("05350482874");

        User updatedUser = userRepository.save(user2);
        testEntityManager.flush();
        testEntityManager.clear();

        Optional<User> foundUser = userRepository.findById(user2.getId());
        assertThat(foundUser.get().getPhone()).isEqualTo("05350482874");
    }

    @Test
    void deleteById_RemoveUser(){
        long userId = user1.getId();

        userRepository.deleteById(userId);
        testEntityManager.flush();

        Optional<User> foundUser = userRepository.findById(user1.getId());
        assertThat(foundUser).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void existByEmail_ShouldReturnExistUserWithEmail(){
        boolean isExists = userRepository.existsByEmail(user1.getEmail());
        assertThat(isExists).isTrue();
    }

    @Test
    void existByEmail_ShouldReturnNonExistUserWithEmail(){
        boolean isExists = userRepository.existsByEmail("burak@example.com");
        assertThat(isExists).isFalse();
    }

    @Test
    void countAllUsers_ShouldReturnAllUserCount(){
        long userCount = userRepository.countAllUsers();
        assertThat(userCount).isEqualTo(2);
    }

    @Test
    void findActiveUsersOlderThan_ShouldReturnActiveAndOlderUsers(){
        List<User> userList = userRepository.findActiveUsersOlderThan(25);

        assertThat(userList).hasSize(1);
        assertThat(userList.get(0).getUsername()).isEqualTo("mertceylan");
    }

    @Test
    void findByFullName_ShouldReturnMatchingFullname(){
        String firstName = "salih";
        String lastName = "dursun";

        List<User> userList = userRepository.findByFullName(firstName, lastName);

        assertThat(userList).hasSize(1);
        assertThat(userList.get(0).getFirstName()).isEqualTo("salih");
        assertThat(userList.get(0).getLastName()).isEqualTo("dursun");
    }

    @Test
    void findUsersByEmailDomain_ShouldReturnMatchingSpecificEmailDomain(){
        User newUser = new User(null,"başak","özcan","basakozcan","basakozcan@gmail.com","05457508682",34,true);
        testEntityManager.persistAndFlush(newUser);

        String domain = "gmail.com";

        List<User> userList = userRepository.findUsersByEmailDomain(domain);

        assertThat(userList).hasSize(1);
        assertThat(userList.get(0).getUsername()).isEqualTo("basakozcan");
    }

    @Test
    void existsByPhone_ShouldReturnTrueIfPhoneExists() {
        boolean isExists = userRepository.existsByPhone(user1.getPhone());
        assertThat(isExists).isTrue();
    }

    @Test
    void existsByPhone_ShouldReturnFalseIfPhoneDoesNotExist() {
        boolean isExists = userRepository.existsByPhone("05551112233");
        assertThat(isExists).isFalse();
    }

    @Test
    void deactivateUser_ShouldSetUserActiveToFalse() {
        int updatedCount = userRepository.deactivateUser(user1.getId());

        assertThat(updatedCount).isEqualTo(1);
        
        // Persistence context'i temizliyoruz ki findById ile çağırınca Hibernate önbellekten değil DB'den güncel veriyi okusun
        testEntityManager.clear();
        User updatedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(updatedUser.isActive()).isFalse();
    }

    @Test
    void deleteInactiveUsers_ShouldRemoveOnlyInactiveUsers() {
        user2.setActive(false);
        testEntityManager.persistAndFlush(user2);
        testEntityManager.clear();
        
        int deletedCount = userRepository.deleteInactiveUsers();
        
        assertThat(deletedCount).isEqualTo(1);
        assertThat(userRepository.findById(user2.getId())).isEmpty();
        assertThat(userRepository.findById(user1.getId())).isPresent();
    }
}
