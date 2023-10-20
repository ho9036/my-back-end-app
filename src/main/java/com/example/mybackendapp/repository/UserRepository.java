package com.example.mybackendapp.repository;

import com.example.mybackendapp.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"permissions"})
    @Query("select u from User u where u.email = :email")
    Optional<User> getUserByEmail(String email);

    List<User> getUsersByNameContains(String name);

    @Modifying
    @Query("UPDATE User u SET u.name=:newName WHERE u.id=:id")
    int updateName(Long id, String newName);

    @Modifying
    int deleteByNameContains(String name);
}