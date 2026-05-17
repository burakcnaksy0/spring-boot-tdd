package com.burakcanaksoy.springboottdd.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    UserResponse response;
    UserCreateRequest request;
    User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Burak")
                .lastName("Can")
                .username("burakcan")
                .email("burak@can.com")
                .phone("05554443322")
                .age(25)
                .active(true)
                .build();

        request = UserCreateRequest.builder()
                .firstName("Burak")
                .lastName("Can")
                .username("burakcan")
                .email("burak@can.com")
                .phone("05554443322")
                .age(25)
                .build();

        response = UserResponse.builder()
                .id(1L)
                .firstName("Burak")
                .lastName("Can")
                .username("burakcan")
                .email("burak@can.com")
                .phone("05554443322")
                .age(25)
                .active(true)
                .build();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(response.getId());
        assertThat(result.getUsername()).isEqualTo(response.getUsername());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 1");
        
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void createUser_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(response.getUsername());

        // userRepository.save metodu ile kaydedilen veriyi kontrol ediyoruz
        verify(userRepository,times(1)).save(userCaptor.capture());
        // yakalanan user verisini assertion ile kontrol ediyoruz
        User savedUser = userCaptor.getValue();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getPhone()).isEqualTo(request.getPhone());
        assertThat(savedUser.getAge()).isEqualTo(request.getAge());
        assertThat(savedUser.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(request.getLastName());
        assertThat(savedUser.getUsername()).isEqualTo(request.getUsername());

        // doğru kullanım
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).existsByPhone(request.getPhone());
        verify(userMapper, times(1)).toEntity(request);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already exists");

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenPhoneExists_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(request.getPhone())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Phone already exists");

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).existsByPhone(request.getPhone());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_ShouldReturnUserResponseList() {
        // Arrange
        List<User> users = List.of(user);
        List<UserResponse> responses = List.of(response);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseList(users)).thenReturn(responses);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(response.getUsername());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toResponseList(users);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 1");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).delete(any());
    }
}