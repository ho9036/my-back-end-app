package com.example.mybackendapp.controller;

import com.example.mybackendapp.entity.User;
import com.example.mybackendapp.entity.UserPermission;
import com.example.mybackendapp.model.LoginUserRequest;
import com.example.mybackendapp.model.LoginUserResponse;
import com.example.mybackendapp.repository.UserRepository;
import com.example.mybackendapp.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(HttpServletRequest request){

        if(request == null){
            return ResponseEntity.badRequest().build();
        }

        String headerValue  = request.getHeader("Authorization");

        if(headerValue == null){
            return ResponseEntity.badRequest().build();
        }else if(!headerValue.startsWith("Basic ")){
            return ResponseEntity.badRequest().build();
        }

        String userNamePasswordEncryptValue = headerValue.replace("Basic ", "");
        byte[] userNamePasswordByteValue = Base64.decodeBase64(userNamePasswordEncryptValue);
        String userNamePasswordValue = new String(userNamePasswordByteValue);
        if(!userNamePasswordValue.contains(";")){
            ResponseEntity.badRequest().build();
        }

        String[] userNameAndPassword = userNamePasswordValue.split(";");
        if(userNameAndPassword.length != 2){
            ResponseEntity.badRequest().build();
        }

        String email = userNameAndPassword[0];
        String password = userNameAndPassword[1];
        Optional<User> result = userRepository.getUserByEmail(email);

        if(result.isPresent()){
            User user = result.get();
            if(!passwordEncoder.matches(password, user.getPassword())){
                return ResponseEntity.notFound().build();
            }else{
                String accessToken = JwtUtils.createAccessToken(user.getEmail(), user.getPermissions());
                String refreshToken = JwtUtils.createRefreshToken(user.getEmail(), user.getPermissions());
                LoginUserResponse response = new LoginUserResponse();
                response.setAccessToken(accessToken);
                response.setRefreshToken(refreshToken);

                return ResponseEntity.ok(response);
            }
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/get-new-access-token")
    public ResponseEntity<String> getToken(HttpServletRequest request, @RequestBody String accessToken){

        if(accessToken == null){
            return ResponseEntity.badRequest().build();
        }

        if(!JwtUtils.isExpired(accessToken)){
            return ResponseEntity.badRequest().build();
        }

        String headerValue  = request.getHeader("Authorization");

        if(headerValue == null){
            return ResponseEntity.status(401).build();
        }

        String refreshToken = headerValue.split(" ")[1];

        if(JwtUtils.isExpired(refreshToken)){
            return ResponseEntity.badRequest().build();
        }

        List<String> permissions = JwtUtils.getPermissions(refreshToken);
        List<UserPermission> userPermissions = new ArrayList<>();
        for(String permission : permissions){
            UserPermission userPermission = new UserPermission();
            userPermission.setPermission(permission);
            userPermissions.add(userPermission);
        }

        return ResponseEntity
                .ok(JwtUtils.createAccessToken(JwtUtils.getEmail(refreshToken), userPermissions));
    }
}
