package com.example.mybackendapp.security;

import org.springframework.security.core.GrantedAuthority;

public class PermissionAuthority implements GrantedAuthority {

    private final String permission;

    public PermissionAuthority(String permission) {
        this.permission = permission;
    }

    @Override
    public String getAuthority() {
        return permission;
    }
}
