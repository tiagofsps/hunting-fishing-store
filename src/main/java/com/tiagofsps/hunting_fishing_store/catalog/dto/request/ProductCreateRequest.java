package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record ProductCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        String slug,

        @Size(max = 2000)
        String description,

        @Valid
        Map<String, ProductTranslationRequest> translations,

        Boolean requiresLicense,

        LicenseType licenseType,

        @NotNull
        Long categoryId,

        @NotNull
        Long brandId,

        List<@Valid ProductItemRequest> items,

        List<@Valid ProductImageRequest> images
) {
}
