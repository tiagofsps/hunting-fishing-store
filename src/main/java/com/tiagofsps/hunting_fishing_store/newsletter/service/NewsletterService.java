package com.tiagofsps.hunting_fishing_store.newsletter.service;

import com.tiagofsps.hunting_fishing_store.common.exception.NewsletterAlreadySubscribedException;
import com.tiagofsps.hunting_fishing_store.common.exception.NewsletterSubscriptionNotFoundException;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterSubscribeRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterSubscriptionResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.entity.NewsletterSubscription;
import com.tiagofsps.hunting_fishing_store.newsletter.repository.NewsletterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return findAll(null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NewsletterSubscriptionResponse> findAll(Boolean active, String query, Pageable pageable) {
        String trimmedQuery = query == null ? null : query.trim();
        boolean hasQuery = trimmedQuery != null && !trimmedQuery.isBlank();

        Page<NewsletterSubscription> page;
        if (active == null && !hasQuery) {
            page = newsletterRepository.findAll(pageable);
        } else if (active == null) {
            page = newsletterRepository.findByEmailContainingIgnoreCase(trimmedQuery, pageable);
        } else if (!hasQuery) {
            page = newsletterRepository.findByActive(active, pageable);
        } else {
            page = newsletterRepository.findByActiveAndEmailContainingIgnoreCase(active, trimmedQuery, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public NewsletterSubscriptionResponse updateStatus(Long id, boolean active) {
        NewsletterSubscription subscription = newsletterRepository.findById(id)
                .orElseThrow(() -> new NewsletterSubscriptionNotFoundException(id));
        subscription.setActive(active);
        return toResponse(newsletterRepository.save(subscription));
    }

    @Transactional
    public void delete(Long id) {
        NewsletterSubscription subscription = newsletterRepository.findById(id)
                .orElseThrow(() -> new NewsletterSubscriptionNotFoundException(id));
        newsletterRepository.delete(subscription);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return newsletterRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        StringBuilder csv = new StringBuilder("id,email,active,subscribedAt\n");
        newsletterRepository.findAll(Sort.by(Sort.Direction.DESC, "subscribedAt")).forEach(subscription ->
                csv.append(subscription.getId())
                        .append(',')
                        .append(csvValue(subscription.getEmail()))
                        .append(',')
                        .append(subscription.isActive())
                        .append(',')
                        .append(subscription.getSubscribedAt())
                        .append('\n'));
        return csv.toString();
    }

    private String csvValue(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
