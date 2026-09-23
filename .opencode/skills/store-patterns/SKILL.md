---
name: store-patterns
description: Reference code patterns for Controllers, Services, Repositories, Entities, and DTOs in the Hunting & Fishing Store project
---

## Controller Pattern

File: `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/controller/<Feature>Controller.java`

```java
package com.tiagofsps.hunting_fishing_store.feature.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tiagofsps.hunting_fishing_store.feature.dto.request.FeatureCreateRequest;
import com.tiagofsps.hunting_fishing_store.feature.dto.response.FeatureResponse;
import com.tiagofsps.hunting_fishing_store.feature.service.FeatureService;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @GetMapping
    public List<FeatureResponse> findAll() {
        return featureService.findAll();
    }

    @GetMapping("/{id}")
    public FeatureResponse findById(@PathVariable Long id) {
        return featureService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureResponse create(@Valid @RequestBody FeatureCreateRequest request) {
        return featureService.create(request);
    }

    @PutMapping("/{id}")
    public FeatureResponse update(@PathVariable Long id, @Valid @RequestBody FeatureCreateRequest request) {
        return featureService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        featureService.delete(id);
    }
}
```

## Service Pattern

File: `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/service/<Feature>Service.java`

```java
package com.tiagofsps.hunting_fishing_store.feature.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.tiagofsps.hunting_fishing_store.feature.dto.request.FeatureCreateRequest;
import com.tiagofsps.hunting_fishing_store.feature.dto.response.FeatureResponse;
import com.tiagofsps.hunting_fishing_store.feature.entity.FeatureEntity;
import com.tiagofsps.hunting_fishing_store.feature.repository.FeatureRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.FeatureNotFoundException;

@Service
public class FeatureService {

    private final FeatureRepository featureRepository;

    public FeatureService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    private FeatureResponse toResponse(FeatureEntity entity) {
        return new FeatureResponse(entity.getId(), entity.getName(), entity.getSlug());
    }

    public List<FeatureResponse> findAll() {
        return featureRepository.findAll().stream().map(this::toResponse).toList();
    }

    public FeatureResponse findById(Long id) {
        FeatureEntity entity = featureRepository.findById(id)
                .orElseThrow(() -> new FeatureNotFoundException(id));
        return toResponse(entity);
    }

    public FeatureResponse create(FeatureCreateRequest request) {
        FeatureEntity entity = new FeatureEntity();
        entity.setName(request.name());
        entity.setSlug(request.slug());
        FeatureEntity saved = featureRepository.save(entity);
        return toResponse(saved);
    }

    public FeatureResponse update(Long id, FeatureCreateRequest request) {
        FeatureEntity entity = featureRepository.findById(id)
                .orElseThrow(() -> new FeatureNotFoundException(id));
        entity.setName(request.name());
        entity.setSlug(request.slug());
        FeatureEntity updated = featureRepository.save(entity);
        return toResponse(updated);
    }

    public void delete(Long id) {
        FeatureEntity entity = featureRepository.findById(id)
                .orElseThrow(() -> new FeatureNotFoundException(id));
        featureRepository.delete(entity);
    }
}
```

## Repository Pattern

File: `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/repository/<Feature>Repository.java`

```java
package com.tiagofsps.hunting_fishing_store.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.tiagofsps.hunting_fishing_store.feature.entity.FeatureEntity;

public interface FeatureRepository extends JpaRepository<FeatureEntity, Long> {
    Optional<FeatureEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}
```

## Entity Pattern

File: `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/entity/<Feature>Entity.java`

```java
package com.tiagofsps.hunting_fishing_store.feature.entity;

import com.tiagofsps.hunting_fishing_store.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "features")
public class FeatureEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;
}
```

## DTO Pattern

Files: under `src/main/java/com/tiagofsps/hunting_fishing_store/<feature>/dto/request/` and `dto/response/`

```java
// Request DTO record
package com.tiagofsps.hunting_fishing_store.feature.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeatureCreateRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 100) String slug
) {}

// Response DTO record
package com.tiagofsps.hunting_fishing_store.feature.dto.response;

public record FeatureResponse(
    Long id,
    String name,
    String slug
) {}
```
