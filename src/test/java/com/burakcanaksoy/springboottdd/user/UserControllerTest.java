package com.burakcanaksoy.springboottdd.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest extends BaseContainerTest {

    UserResponse response, response2, response3, response4;
    UserCreateRequest request, request2, request3, request4;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/api/user";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        request = UserCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phone("05321234567")
                .age(25)
                .build();

        request2 = UserCreateRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alicesmith")
                .email("alice@example.com")
                .phone("05431234567")
                .age(28)
                .build();

        request3 = UserCreateRequest.builder()
                .firstName("Michael")
                .lastName("Brown")
                .username("michaelbrown")
                .email("michael@example.com")
                .phone("05541234567")
                .age(31)
                .build();

        request4 = UserCreateRequest.builder()
                .firstName("Emma")
                .lastName("Wilson")
                .username("emmawilson")
                .email("emma@example.com")
                .phone("05051234567")
                .age(22)
                .build();

        response = userService.createUser(request);
        response2 = userService.createUser(request2);
        response3 = userService.createUser(request3);
        response4 = userService.createUser(request4);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = this.port;
    }

    @Test
    void shouldFindUsers(){
        given()
                .when()
                .get(BASE_URL+"/all")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4));
    }

    @Test
    void shouldFindUserById(){
        when()
                .get(BASE_URL+"/{id}", response4.getId())
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Emma"))
                .body("username", equalTo("emmawilson"))
                .body("phone", equalTo("05051234567"));
    }

    @Test
    void shouldAddNewUser(){
        UserCreateRequest newUserRequest = UserCreateRequest.builder()
                .firstName("Zeynep")
                .lastName("Kara")
                .username("zeynepkara")
                .email("zeynep@test.com")
                .phone("05071234567")
                .age(31)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(newUserRequest)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Zeynep"))
                .body("username", equalTo("zeynepkara"))
                .body("phone", equalTo("05071234567"));
    }

    @Test
    void shouldDeleteUser(){
        when()
                .delete(BASE_URL+"/{id}", response4.getId())
                .then()
                .statusCode(204);

        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(
                        () -> userService.getUserById(response4.getId()))
                .withMessage("User not found with id: " + response4.getId());
    }

    @Test
    void shouldNotCreateUserWhenValidationFails() {
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .firstName("") 
                .lastName("Kara")
                .username("zk") 
                .email("invalid-email") 
                .phone("12345") 
                .age(15) 
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("validationErrors", hasKey("firstName"))
                .body("validationErrors", hasKey("username"))
                .body("validationErrors", hasKey("email"))
                .body("validationErrors", hasKey("phone"))
                .body("validationErrors", hasKey("age"));
    }

    @Test
    void shouldNotCreateUserWhenEmailOrPhoneAlreadyExists() {
        UserCreateRequest duplicateRequest = UserCreateRequest.builder()
                .firstName("JohnDuplicate")
                .lastName("Doe")
                .username("john_dup")
                .email("john@example.com") 
                .phone("05399999999")
                .age(30)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(duplicateRequest)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(409)
                .body("error", equalTo("Conflict"))
                .body("message", containsString("User already exists with email"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        Long nonExistentId = 999999L;

        given()
                .when()
                .get(BASE_URL + "/{id}", nonExistentId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"))
                .body("message", equalTo("User not found with id: " + nonExistentId))
                .body("path", equalTo(BASE_URL + "/" + nonExistentId));
    }
}