package com.tiagofsps.hunting_fishing_store.newsletter.service;

import com.tiagofsps.hunting_fishing_store.common.exception.NewsletterAlreadySubscribedException;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterSubscribeRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterSubscriptionResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.entity.NewsletterSubscription;
import com.tiagofsps.hunting_fishing_store.newsletter.repository.NewsletterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public NewsletterService(NewsletterRepository newsletterRepository) {
        this.newsletterRepository = newsletterRepository;
    }

    private NewsletterSubscriptionResponse toResponse(NewsletterSubscription subscription) {
        return new NewsletterSubscriptionResponse(
                subscription.getId(),
                subscription.getEmail(),
                subscription.isActive(),
                subscription.getSubscribedAt());
    }

    @Transactional
    public NewsletterSubscriptionResponse subscribe(NewsletterSubscribeRequest request) {
        String email = request.email().trim();
        NewsletterSubscription existing = newsletterRepository.findByEmail(email).orElse(null);

        if (existing != null && existing.isActive()) {
            throw new NewsletterAlreadySubscribedException(email);
        }
        if (existing != null) {
            existing.setActive(true);
            existing.setSubscribedAt(LocalDateTime.now());
            return toResponse(newsletterRepository.save(existing));
        }

        NewsletterSubscription subscription = new NewsletterSubscription();
        subscription.setEmail(email);
        subscription.setActive(true);
        subscription.setSubscribedAt(LocalDateTime.now());
        return toResponse(newsletterRepository.save(subscription));
    }

    @Transactional
    public void unsubscribe(NewsletterSubscribeRequest request) {
        String email = request.email().trim();
        NewsletterSubscription existing = newsletterRepository.findByEmail(email).orElse(null);
        if (existing == null || !existing.isActive()) {
            return;
        }
        existing.setActive(false);
        newsletterRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<NewsletterSubscriptionResponse> findAll(Pageable pageable) {
        return newsletterRepository.findAll(pageable).map(this::toResponse);
    }
}