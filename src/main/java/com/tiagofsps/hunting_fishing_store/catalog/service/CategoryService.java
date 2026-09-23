package com.tiagofsps.hunting_fishing_store.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.CategoryResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

@Service
public class CategoryService {

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
                category.getParentCategory() != null ? category.getParentCategory().getId() : null,
                productCountsByCategory.getOrDefault(category.getId(), 0L)
        );
    }

    private Map<Long, Long> productCountsByCategory() {
        return productRepository.countProductsByCategory().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return toResponse(category, productCountsByCategory());
    }

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
}
