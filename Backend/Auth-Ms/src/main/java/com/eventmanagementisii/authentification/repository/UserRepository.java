package com.eventmanagementisii.authentification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventmanagementisii.authentification.entity.User;

public interface UserRepository extends JpaRepository<User, Long> { //User : the entity to manage , Long : @Id  in the entity
    User findByUsername(String username);
    User findByemail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);  // unique !
}