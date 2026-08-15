package com.tiagofsps.hunting_fishing_store.user.repository;

import com.tiagofsps.hunting_fishing_store.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
