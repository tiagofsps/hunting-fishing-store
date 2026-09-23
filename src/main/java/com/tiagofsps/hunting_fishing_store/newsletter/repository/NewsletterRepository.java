package com.tiagofsps.hunting_fishing_store.newsletter.repository;

import com.tiagofsps.hunting_fishing_store.newsletter.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterRepository extends JpaRepository<NewsletterSubscription, Long> {
    Optional<NewsletterSubscription> findByEmail(String email);
    boolean existsByEmailAndActiveTrue(String email);
}