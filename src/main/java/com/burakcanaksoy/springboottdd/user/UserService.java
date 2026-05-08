package com.burakcanaksoy.springboottdd.user;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    // In-memory
    private List<User> userList = new ArrayList<>();
    private long countId = 1;

    public User createUser(String name , String email, int age){
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        if (email == null || !email.contains("@")){
            throw new IllegalArgumentException("Invalid email address");
        }
        if (age < 0 || age > 150){
            throw new IllegalArgumentException("Age must be between 0 and 150.");
        }
        User user = new User(countId++,name,email,age,true);
        userList.add(user);
        return user;
    }

    public User findById(Long id){
        return userList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> findAll(){
        return new ArrayList<>(userList);
    }

    public List<User> findActiveUsers(){
        return userList.stream()
                .filter(User::isActive)
                .toList();
    }

    public User deactiveUser(Long id){
        User user = findById(id);
        user.setActive(false);
        return user;
    }

    public void deleteUser(Long id){
        User user = findById(id);
        userList.remove(user);
    }

    public int getUserCount(){
        return userList.size();
    }

    public Optional<User> findByEmail(String email){
        return userList.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public void clearAll(){
        userList.clear();
        countId = 1;
    }
}