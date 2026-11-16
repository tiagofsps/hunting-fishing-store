package com.tiagofsps.hunting_fishing_store.catalog.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryTranslationRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.CategoryResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.CategoryTranslation;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.common.dto.LocalizedTextResponse;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

@Service
public class CategoryService {

    private static final String DEFAULT_LOCALE = "en";
    private static final Set<String> SUPPORTED_LOCALES = Set.of("en", "pt");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    private CategoryResponse toResponse(Category category, Map<Long, Long> productCountsByCategory) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                translationsOf(category),
                category.getParentCategory() != null ? category.getParentCategory().getId() : null,
                productCountsByCategory.getOrDefault(category.getId(), 0L)
        );
    }

    private Map<String, LocalizedTextResponse> translationsOf(Category category) {
        Map<String, LocalizedTextResponse> translations = new LinkedHashMap<>();
        category.getTranslations().forEach(translation ->
                translations.put(
                        translation.getLocale(),
                        new LocalizedTextResponse(translation.getName(), translation.getDescription())));
        translations.putIfAbsent(
                DEFAULT_LOCALE,
                new LocalizedTextResponse(category.getName(), category.getDescription()));
        return translations;
    }

    private Map<Long, Long> productCountsByCategory() {
        return productRepository.countProductsByCategory().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return toResponse(category, productCountsByCategory());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        Map<Long, Long> productCounts = productCountsByCategory();
        return categoryRepository.findAll().stream()
                .map(category -> toResponse(category, productCounts))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new CategoryAlreadyExistsException(request.slug());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        applyTranslations(category, request.translations());

        if (request.parentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.parentCategoryId()));
            category.setParentCategory(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return toResponse(savedCategory, productCountsByCategory());
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryCreateRequest request) {
        if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new CategoryAlreadyExistsException(request.slug());
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        applyTranslations(category, request.translations());

        if (request.parentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.parentCategoryId()));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return toResponse(updatedCategory, productCountsByCategory());
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }

    private void applyTranslations(Category category, Map<String, CategoryTranslationRequest> translations) {
        if (translations == null || translations.isEmpty()) {
            upsertTranslation(category, DEFAULT_LOCALE, category.getName(), category.getDescription());
            return;
        }

        CategoryTranslationRequest english = translations.get(DEFAULT_LOCALE);
        if (english != null) {
            category.setName(english.name());
            category.setDescription(normalizeOptional(english.description()));
        }

        Set<String> desiredLocales = translations.keySet().stream()
                .map(this::normalizeLocale)
                .filter(locale -> locale != null)
                .collect(Collectors.toSet());
        desiredLocales.add(DEFAULT_LOCALE);

        // Update existing rows in place; deleting and recreating the same
        // (category_id, locale) pair would violate the unique constraint.
        category.getTranslations().removeIf(translation ->
                !desiredLocales.contains(translation.getLocale()));

        translations.forEach((locale, text) -> {
            String normalizedLocale = normalizeLocale(locale);
            if (normalizedLocale == null) return;
            upsertTranslation(category, normalizedLocale, text.name(), text.description());
        });

        if (category.getTranslations().stream().noneMatch(t -> DEFAULT_LOCALE.equals(t.getLocale()))) {
            addTranslation(category, DEFAULT_LOCALE, category.getName(), category.getDescription());
        }
    }

    private void upsertTranslation(Category category, String locale, String name, String description) {
        CategoryTranslation existing = category.getTranslations().stream()
                .filter(translation -> locale.equals(translation.getLocale()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setName(name);
            existing.setDescription(normalizeOptional(description));
            return;
        }

        addTranslation(category, locale, name, description);
    }

    private void addTranslation(Category category, String locale, String name, String description) {
        CategoryTranslation translation = new CategoryTranslation();
        translation.setCategory(category);
        translation.setLocale(locale);
        translation.setName(name);
        translation.setDescription(normalizeOptional(description));
        category.getTranslations().add(translation);
    }

    private String normalizeLocale(String locale) {
        if (locale == null) return null;
        String normalized = locale.toLowerCase();
        return SUPPORTED_LOCALES.contains(normalized) ? normalized : null;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
