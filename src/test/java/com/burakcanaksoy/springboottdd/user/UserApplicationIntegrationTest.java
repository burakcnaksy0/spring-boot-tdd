package com.burakcanaksoy.springboottdd.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = "/setup-test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup-test-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetUserById_whenUserExists() {
        // given - @Sql annotation already inserted user with id 1
        String url = "http://localhost:" + port + "/api/user/1";

        // when
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("testuser@gmail.com");
    }

    @Test
    @Sql(scripts = "/cleanup-test-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldCreateUser_andSaveToDatabase() {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john.doe@test.com")
                .phone("05359702361")
                .age(25)
                .build();
        
        String url = "http://localhost:" + port + "/api/user";

        // when
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(url, request, UserResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("johndoe");

        // Verify it was actually saved in the database
        Optional<User> foundUser = userRepository.findById(response.getBody().getId());
        assertThat(foundUser).isPresent();
    }
}
