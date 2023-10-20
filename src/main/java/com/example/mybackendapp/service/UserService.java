package com.example.mybackendapp.service;

import com.example.mybackendapp.entity.User;
import com.example.mybackendapp.entity.UserPermission;
import com.example.mybackendapp.repository.UserPermissionRepository;
import com.example.mybackendapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(rollbackOn = Exception.class)
    public User create(String name, Integer age, String email, String encodedPassword) {

        boolean exist = userRepository.existsByEmail(email);

        if(!exist){
            User user = new User();
            user.setName(name);
            user.setAge(age);
            user.setEmail(email);
            user.setPassword(encodedPassword);

            UserPermission userPermission = new UserPermission();
            userPermission.setPermission("USER");
            user.getPermissions().add(userPermission);

            return userRepository.save(user);
        }else{
            throw new RuntimeException("Email already exist");
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean updateUser(Long id, String newName) {
        return userRepository.updateName(id, newName) == 1;
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean deleteUser(String name) {
        return userRepository.deleteByNameContains(name) > 0;
    }
}
