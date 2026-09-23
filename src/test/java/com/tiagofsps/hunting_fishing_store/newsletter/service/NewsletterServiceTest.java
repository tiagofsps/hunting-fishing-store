package com.tiagofsps.hunting_fishing_store.newsletter.service;

import com.tiagofsps.hunting_fishing_store.common.exception.NewsletterAlreadySubscribedException;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterSubscribeRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterSubscriptionResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.entity.NewsletterSubscription;
import com.tiagofsps.hunting_fishing_store.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private NewsletterRepository newsletterRepository;

    @InjectMocks
    private NewsletterService newsletterService;

    private NewsletterSubscribeRequest request(String email) {
        return new NewsletterSubscribeRequest(email);
    }

    private NewsletterSubscription subscription(Long id, String email, boolean active) {
        NewsletterSubscription subscription = new NewsletterSubscription();
        subscription.setId(id);
        subscription.setEmail(email);
        subscription.setActive(active);
        subscription.setSubscribedAt(LocalDateTime.now());
        return subscription;
    }

    @Test
    void shouldSubscribeNewEmail() {
        NewsletterSubscribeRequest request = request("new@example.com");
        when(newsletterRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(newsletterRepository.save(any(NewsletterSubscription.class)))
                .thenAnswer(i -> i.getArgument(0));

        NewsletterSubscriptionResponse response = newsletterService.subscribe(request);

        assertTrue(response.active());
        assertNotNull(response.subscribedAt());
        verify(newsletterRepository).save(any(NewsletterSubscription.class));
    }

    @Test
    void shouldThrow409WhenAlreadyActive() {
        NewsletterSubscribeRequest request = request("active@example.com");
        when(newsletterRepository.findByEmail("active@example.com"))
                .thenReturn(Optional.of(subscription(1L, "active@example.com", true)));

        assertThrows(NewsletterAlreadySubscribedException.class, () -> newsletterService.subscribe(request));
        verify(newsletterRepository, never()).save(any());
    }

    @Test
    void shouldReactivateInactiveSubscription() {
        NewsletterSubscribeRequest request = request("old@example.com");
        NewsletterSubscription inactive = subscription(1L, "old@example.com", false);
        when(newsletterRepository.findByEmail("old@example.com")).thenReturn(Optional.of(inactive));
        when(newsletterRepository.save(any(NewsletterSubscription.class))).thenReturn(inactive);

        NewsletterSubscriptionResponse response = newsletterService.subscribe(request);

        assertTrue(response.active());
        assertNotNull(response.subscribedAt());
    }

    @Test
    void shouldUnsubscribeActiveSubscription() {
        NewsletterSubscribeRequest request = request("active@example.com");
        NewsletterSubscription active = subscription(1L, "active@example.com", true);
        when(newsletterRepository.findByEmail("active@example.com")).thenReturn(Optional.of(active));
        when(newsletterRepository.save(any(NewsletterSubscription.class))).thenReturn(active);

        newsletterService.unsubscribe(request);

        assertFalse(active.isActive());
        verify(newsletterRepository).save(active);
    }

    @Test
    void shouldUnsubscribeIdempotentlyForUnknownEmail() {
        NewsletterSubscribeRequest request = request("unknown@example.com");
        when(newsletterRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        newsletterService.unsubscribe(request);

        verify(newsletterRepository, never()).save(any());
    }

    @Test
    void shouldUnsubscribeIdempotentlyForAlreadyInactiveEmail() {
        NewsletterSubscribeRequest request = request("old@example.com");
        when(newsletterRepository.findByEmail("old@example.com"))
                .thenReturn(Optional.of(subscription(1L, "old@example.com", false)));

        newsletterService.unsubscribe(request);

        verify(newsletterRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllSubscriptionsPaged() {
        NewsletterSubscription a = subscription(1L, "a@example.com", true);
        NewsletterSubscription b = subscription(2L, "b@example.com", false);
        Pageable pageable = PageRequest.of(0, 20);
        when(newsletterRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(a, b), pageable, 2));

        Page<NewsletterSubscriptionResponse> page = newsletterService.findAll(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals("a@example.com", page.getContent().get(0).email());
        assertFalse(page.getContent().get(1).active());
    }
}