package com.burakcanaksoy.springboottdd.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(value = UserController.class,excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(value = UserController.class)
class UserControllerTest {
    UserResponse response, response2, response3, response4;
    UserCreateRequest request, request2, request3, request4;

    @BeforeEach
    void setUp() {
        request = UserCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phone("05321234567")
                .age(25)
                .build();

        response = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .phone("05321234567")
                .age(25)
                .active(true)
                .build();

        request2 = UserCreateRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alicesmith")
                .email("alice@example.com")
                .phone("05431234567")
                .age(28)
                .build();

        response2 = UserResponse.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .username("alicesmith")
                .email("alice@example.com")
                .phone("05431234567")
                .age(28)
                .active(true)
                .build();


        request3 = UserCreateRequest.builder()
                .firstName("Michael")
                .lastName("Brown")
                .username("michaelbrown")
                .email("michael@example.com")
                .phone("05541234567")
                .age(31)
                .build();

        response3 = UserResponse.builder()
                .id(3L)
                .firstName("Michael")
                .lastName("Brown")
                .username("michaelbrown")
                .email("michael@example.com")
                .phone("05541234567")
                .age(31)
                .active(true)
                .build();


        request4 = UserCreateRequest.builder()
                .firstName("Emma")
                .lastName("Wilson")
                .username("emmawilson")
                .email("emma@example.com")
                .phone("05051234567")
                .age(22)
                .build();

        response4 = UserResponse.builder()
                .id(4L)
                .firstName("Emma")
                .lastName("Wilson")
                .username("emmawilson")
                .email("emma@example.com")
                .phone("05051234567")
                .age(22)
                .active(false)
                .build();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnAllUsers() throws Exception {
        // Arrange
        List<UserResponse> userList = List.of(response, response2, response3, response4);

        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(userList.size()))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Alice"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].firstName").value("Michael"))
                .andExpect(jsonPath("$[3].id").value(4))
                .andExpect(jsonPath("$[3].firstName").value("Emma"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        // Arrange
        Long userId = 1L;
        
        when(userService.getUserById(eq(userId))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("05321234567"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldCreateUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("05321234567"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(eq(userId));

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }
}