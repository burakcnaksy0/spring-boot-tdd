package com.burakcanaksoy.springboottdd.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse createUser(UserCreateRequest request) {
        checkEmailExists(request.getEmail());
        checkPhoneExists(request.getPhone());

        User user = userMapper.toEntity(request);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private void checkEmailExists(String email){
        if (userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException("User already exists with email : "+ email);
        }
    }

    private void checkPhoneExists(String phone){
        if (userRepository.existsByPhone(phone)){
            throw new UserAlreadyExistsException("User already exists with email : "+ phone);
        }
    }
}
