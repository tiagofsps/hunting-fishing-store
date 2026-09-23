package com.tiagofsps.hunting_fishing_store.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tiagofsps.hunting_fishing_store.auth.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}