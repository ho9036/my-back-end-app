package com.example.mybackendapp.controller;

import com.example.mybackendapp.entity.User;
import com.example.mybackendapp.model.CreateUserRequest;
import com.example.mybackendapp.repository.UserRepository;
import com.example.mybackendapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<User> create(@RequestBody CreateUserRequest request){
        try{
            if(request != null && request.getName() != null && !request.getName().isBlank()
                    && request.getAge() != null && request.getAge() > 0
                    && request.getEmail() != null && !request.getEmail().isBlank()
                    && request.getPassword() != null && !request.getPassword().isBlank()){
                User newUser = userService.create(request.getName(), request.getAge(), request.getEmail(),
                        passwordEncoder.encode(request.getPassword()));
                return ResponseEntity.ok(newUser);
            }else{
                return ResponseEntity.badRequest().build();
            }
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get-user-by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email){
        if(email == null || email.isBlank()){
            return ResponseEntity.badRequest().build();
        }else{
            Optional<User> result = userRepository.getUserByEmail(email);
            if(result.isPresent()){
                User user = result.get();
                return ResponseEntity.ok(user);
            }else{
                return ResponseEntity.noContent().build();
            }
        }
    }

    @GetMapping("/get-user-by-id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id){
        if(id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }else{
            Optional<User> result = userRepository.findById(id);
            if(result.isPresent()){
                User user = result.get();
                return ResponseEntity.ok(user);
            }else{
                return ResponseEntity.noContent().build();
            }
        }
    }

    @GetMapping("/get-user-by-name")
    public ResponseEntity<List<User>> getUsersByName(@RequestParam("keyword") String keyword){
        if(keyword == null || keyword.isBlank()){
            return ResponseEntity.badRequest().build();
        }else{
            List<User> users = userRepository.getUsersByNameContains(keyword);
            if(users.isEmpty()){
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.ok(users);
            }
        }
    }

    @PutMapping("/update-user-name-by-id/{id}")
    public ResponseEntity<Void> updateUserNameById(@PathVariable("id") Long id,
                                                   @RequestParam("newName") String newName){
        if(id == null || id <= 0 || newName == null || newName.isBlank()){
            return ResponseEntity.badRequest().build();
        }else{
            boolean updated = userService.updateUser(id, newName);
            if(updated){
                return ResponseEntity.ok().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }
    }

    @DeleteMapping("/delete-user-by-name")
    public ResponseEntity<Void> deleteUserByName(@RequestParam("name") String name){
        if(name == null || name.isBlank()){
            return ResponseEntity.badRequest().build();
        }else{
            boolean deleted = userService.deleteUser(name);
            if(deleted){
                return ResponseEntity.ok().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }
    }
}
