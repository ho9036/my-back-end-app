package com.example.mybackendapp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserResponse {
    private  String accessToken;

    private String refreshToken;
}
