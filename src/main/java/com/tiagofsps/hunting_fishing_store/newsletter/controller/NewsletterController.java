package com.tiagofsps.hunting_fishing_store.newsletter.controller;

import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterSubscribeRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterSubscriptionResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
@Tag(name = "Newsletter", description = "Newsletter subscriptions")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Subscribe to the newsletter (public)")
    public NewsletterSubscriptionResponse subscribe(@Valid @RequestBody NewsletterSubscribeRequest request) {
        return newsletterService.subscribe(request);
    }

    @DeleteMapping("/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unsubscribe from the newsletter (public)")
    public void unsubscribe(@Valid @RequestBody NewsletterSubscribeRequest request) {
        newsletterService.unsubscribe(request);
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Paginated subscription list")
    public Page<NewsletterSubscriptionResponse> findSubscriptions(@PageableDefault(size = 20) Pageable pageable) {
        return newsletterService.findAll(pageable);
    }
}