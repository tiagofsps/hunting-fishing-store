package com.tiagofsps.hunting_fishing_store.catalog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductResponse;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductItemResponse;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductImageResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductImage;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidSortFieldException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.LicenseTypeRequiredException;

@Service
public class ProductService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "createdAt");
    private static final List<String> ALLOWED_SORT_DIRECTIONS = List.of("asc", "desc");

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, 
                          BrandRepository brandRepository, 
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    private ProductResponse toResponse(Product product) {
        List<ProductItemResponse> items = product.getItems().stream()
                .map(item -> new ProductItemResponse(
                        item.getId(),
                        item.getSku(),
                        item.getPrice(),
                        item.getStockQuantity(),
                        item.getWeight(),
                        item.getActive(),
                        item.getAttributes()
                )).toList();

        List<ProductImageResponse> images = product.getImages().stream()
                .map(img -> new ProductImageResponse(
                        img.getId(),
                        img.getImageUrl(),
                        img.getAltText(),
                        img.getIsPrimary(),
                        img.getDisplayOrder()
                )).toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getRequiresLicense(),
                product.getLicenseType(),
                product.getCategory().getId(),
                product.getBrand().getId(),
                items,
                images
        );
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    /**
     * Paginated, filterable product search. Returns a Spring Data Page (a
     * deliberate contract change from the previous plain list so the catalog
     * can scale beyond a single page).
     *
     * @param sortField whitelisted field; must be one of ALLOWED_SORT_FIELDS.
     * @param sortDirection "asc" or "desc".
     */
    public Page<ProductResponse> findAll(Long categoryId, Long brandId, String query, String sortField,
                                         String sortDirection, int page, int size) {
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new InvalidSortFieldException(sortField, ALLOWED_SORT_FIELDS);
        }
        if (!ALLOWED_SORT_DIRECTIONS.contains(sortDirection)) {
            throw new InvalidSortFieldException(sortDirection, ALLOWED_SORT_DIRECTIONS);
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return productRepository.search(categoryId, brandId, query, pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        validateLicenseType(request);

        if (productRepository.existsBySlug(request.slug())) {
            throw new ProductAlreadyExistsException(request.slug());
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new BrandNotFoundException(request.brandId()));
        
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setRequiresLicense(request.requiresLicense() != null ? request.requiresLicense() : false);
        product.setLicenseType(request.licenseType());
        product.setBrand(brand);
        product.setCategory(category);

        if (request.items() != null) {
            request.items().forEach(itemReq -> {
                ProductItem item = new ProductItem();
                item.setSku(itemReq.sku());
                item.setPrice(itemReq.price());
                item.setStockQuantity(itemReq.stockQuantity());
                item.setWeight(itemReq.weight());
                item.setActive(itemReq.active() != null ? itemReq.active() : true);
                item.setAttributes(itemReq.attributes());
                item.setProduct(product);
                product.getItems().add(item);
            });
        }

        if (request.images() != null) {
            request.images().forEach(imgReq -> {
                ProductImage image = new ProductImage();
                image.setImageUrl(imgReq.imageUrl());
                image.setAltText(imgReq.altText());
                image.setIsPrimary(imgReq.isPrimary() != null ? imgReq.isPrimary() : false);
                image.setDisplayOrder(imgReq.displayOrder());
                image.setProduct(product);
                product.getImages().add(image);
            });
        }

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductCreateRequest request) {
        validateLicenseType(request);

        if (productRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ProductAlreadyExistsException(request.slug());
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new BrandNotFoundException(request.brandId()));
        
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setRequiresLicense(request.requiresLicense() != null ? request.requiresLicense() : false);
        product.setLicenseType(request.licenseType());
        product.setBrand(brand);
        product.setCategory(category);

        // Simple item replacement for now (or could be improved to update specific items)
        product.getItems().clear();
        if (request.items() != null) {
            request.items().forEach(itemReq -> {
                ProductItem item = new ProductItem();
                item.setSku(itemReq.sku());
                item.setPrice(itemReq.price());
                item.setStockQuantity(itemReq.stockQuantity());
                item.setWeight(itemReq.weight());
                item.setActive(itemReq.active() != null ? itemReq.active() : true);
                item.setAttributes(itemReq.attributes());
                item.setProduct(product);
                product.getItems().add(item);
            });
        }

        product.getImages().clear();
        if (request.images() != null) {
            request.images().forEach(imgReq -> {
                ProductImage image = new ProductImage();
                image.setImageUrl(imgReq.imageUrl());
                image.setAltText(imgReq.altText());
                image.setIsPrimary(imgReq.isPrimary() != null ? imgReq.isPrimary() : false);
                image.setDisplayOrder(imgReq.displayOrder());
                image.setProduct(product);
                product.getImages().add(image);
            });
        }

        Product updatedProduct = productRepository.save(product);
        return toResponse(updatedProduct);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
    }

    private void validateLicenseType(ProductCreateRequest request) {
        if (Boolean.TRUE.equals(request.requiresLicense()) && request.licenseType() == null) {
            throw new LicenseTypeRequiredException();
        }
    }
}
