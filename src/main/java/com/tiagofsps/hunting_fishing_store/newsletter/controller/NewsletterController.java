package com.tiagofsps.hunting_fishing_store.newsletter.controller;

import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterStatusRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.request.NewsletterSubscribeRequest;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterStatsResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.dto.response.NewsletterSubscriptionResponse;
import com.tiagofsps.hunting_fishing_store.newsletter.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "[ADMIN] Paginated subscription list, optionally filtered by status or email")
    public Page<NewsletterSubscriptionResponse> findSubscriptions(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return newsletterService.findAll(active, q, pageable);
    }

    @GetMapping("/subscriptions/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Newsletter subscriber counts")
    public NewsletterStatsResponse stats() {
        return new NewsletterStatsResponse(newsletterService.countActive());
    }

    @PatchMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Activate or deactivate a subscription")
    public NewsletterSubscriptionResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody NewsletterStatusRequest request) {
        return newsletterService.updateStatus(id, request.active());
    }

    @DeleteMapping("/subscriptions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Permanently delete a subscription")
    public void deleteSubscription(@PathVariable Long id) {
        newsletterService.delete(id);
    }

    @GetMapping(value = "/subscriptions/export", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Export all subscriptions as CSV")
    public ResponseEntity<String> exportSubscriptions() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=newsletter-subscriptions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(newsletterService.exportCsv());
    }
}
