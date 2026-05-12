package com.burakcanaksoy.springboottdd.user;

import org.springframework.context.annotation.Configuration;
import java.util.*;

@Configuration
public class UserMapper {
    public User toEntity(UserRequest request){
        if (request == null){
            return null;
        }
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .age(request.getAge())
                .active(true)
                .build();
    }

    public UserResponse toResponse(User user){
        if (user == null){
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .age(user.getAge())
                .active(user.isActive())
                .build();
    }

    public List<UserResponse> toResponseList(List<User> userList){
        if (userList == null || userList.isEmpty()){
            return List.of();
        }
        return userList.stream()
                .map(this::toResponse)
                .toList();
    }
}
