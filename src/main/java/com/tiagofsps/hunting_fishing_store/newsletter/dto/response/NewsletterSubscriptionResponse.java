package com.tiagofsps.hunting_fishing_store.newsletter.dto.response;

import java.time.LocalDateTime;

public record NewsletterSubscriptionResponse(
        Long id,
        String email,
        boolean active,
        LocalDateTime subscribedAt
) {
}