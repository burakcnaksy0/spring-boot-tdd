package com.burakcanaksoy.springboottdd.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApplicationContainerTest extends BaseContainerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserAndPersistInPostgreSQL(){
        assertTrue(POSTGRES_CONTAINER.isRunning());
        // given
        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("burakcan")
                .lastName("aksoy")
                .username("burakcnaksy")
                .email("aksoyburak808@gmail.com")
                .phone("05350482740")
                .age(24)
                .build();

        // when
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "http://localhost:"+port+"/api/user",
                request,
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();

        Optional<User> savedUser = userRepository.findById(response.getBody().getId());
        assertThat(savedUser).isPresent();
    }

    @Test
    void shouldReturn400_whenCreateUserWithInvalidData() {
        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("burakcan")
                .lastName("aksoy")
                .username("burakcnaksy")
                .email("invalid-email-format")
                .phone("05350482740")
                .age(15)
                .build();

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:"+port+"/api/user",
                request,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Ensure no user was saved
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldGetUserById_whenUserExists() {
        // given
        User user = User.builder()
                .firstName("burakcan")
                .lastName("aksoy")
                .username("burakcnaksy")
                .email("aksoyburak808@gmail.com")
                .phone("05350482740")
                .age(24)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        // when
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/user/" + savedUser.getId(),
                UserResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("aksoyburak808@gmail.com");
    }

    @Test
    void shouldDeleteUser_whenUserExists() {
        // given
        User user = User.builder()
                .firstName("john")
                .lastName("doe")
                .username("johndoe")
                .email("john.doe@gmail.com")
                .phone("05351112233")
                .age(30)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/user/" + savedUser.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}
