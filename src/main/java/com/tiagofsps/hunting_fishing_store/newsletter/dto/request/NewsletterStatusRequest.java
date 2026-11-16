package com.tiagofsps.hunting_fishing_store.newsletter.dto.request;

import jakarta.validation.constraints.NotNull;

public record NewsletterStatusRequest(
        @NotNull
        Boolean active
) {
}
