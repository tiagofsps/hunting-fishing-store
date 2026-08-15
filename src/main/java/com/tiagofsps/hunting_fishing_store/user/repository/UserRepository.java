package com.tiagofsps.hunting_fishing_store.user.repository;

import com.tiagofsps.hunting_fishing_store.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
