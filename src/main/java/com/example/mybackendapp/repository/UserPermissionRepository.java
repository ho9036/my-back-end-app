package com.example.mybackendapp.repository;

import com.example.mybackendapp.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long>{
}
