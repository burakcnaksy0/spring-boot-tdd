package com.burakcanaksoy.springboottdd.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UserApplicationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> psql = new PostgreSQLContainer<>("postgres:14.23")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
        dynamicPropertyRegistry.add("spring.datasource.url",psql::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username",psql::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password",psql::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name",() -> "org.postgresql.Driver");
    }

    @Test
    void shouldConnectToRealPostgreSqlDatabase(){
        assertTrue(psql.isRunning());
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
        ResponseEntity<UserResponse> response = testRestTemplate.postForEntity(
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

}
