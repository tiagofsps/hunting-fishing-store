package com.tiagofsps.hunting_fishing_store.cart.repository;

import com.tiagofsps.hunting_fishing_store.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
