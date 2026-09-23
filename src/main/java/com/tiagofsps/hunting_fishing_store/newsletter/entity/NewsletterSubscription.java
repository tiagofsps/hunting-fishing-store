package com.tiagofsps.hunting_fishing_store.newsletter.entity;

import com.tiagofsps.hunting_fishing_store.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "newsletter_subscription")
public class NewsletterSubscription extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime subscribedAt;
}