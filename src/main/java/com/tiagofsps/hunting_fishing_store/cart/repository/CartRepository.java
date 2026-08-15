package com.tiagofsps.hunting_fishing_store.cart.repository;

import com.tiagofsps.hunting_fishing_store.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
