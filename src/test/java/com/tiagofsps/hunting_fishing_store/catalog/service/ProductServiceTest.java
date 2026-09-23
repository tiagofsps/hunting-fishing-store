package com.tiagofsps.hunting_fishing_store.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductItemRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidSortFieldException;
import com.tiagofsps.hunting_fishing_store.common.exception.LicenseTypeRequiredException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Brand brand;
    private Category category;

    @BeforeEach
    void setup() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Shimano");

        category = new Category();
        category.setId(1L);
        category.setName("Fishing");

        product = new Product();
        product.setId(1L);
        product.setName("Fishing Rod");
        product.setSlug("fishing-rod");
        product.setBrand(brand);
        product.setCategory(category);
        product.setItems(new ArrayList<>());
        product.setImages(new ArrayList<>());
    }

    @Test
    @DisplayName("Should return product when id exists")
    void shouldFindProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Fishing Rod", response.name());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldThrowWhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(1L));
    }

    @Test
    @DisplayName("Should return paginated products")
    void shouldReturnAllProducts() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(productRepository.search(null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.findAll(null, null, null, "createdAt", "desc", 0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("Fishing Rod", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Should forward filters and sort to the repository search")
    void shouldForwardFiltersAndSort() {
        PageRequest expected = PageRequest.of(1, 50, Sort.by(Sort.Direction.ASC, "name"));
        when(productRepository.search(2L, 3L, "rod", expected)).thenReturn(new PageImpl<>(List.of(product)));

        productService.findAll(2L, 3L, "rod", "name", "asc", 1, 50);

        ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(productRepository).search(eq(2L), eq(3L), eq("rod"), pageableCaptor.capture());
        assertEquals("name", pageableCaptor.getValue().getSort().getOrderFor("name").getProperty());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
    }

    @Test
    @DisplayName("Should reject a sort field outside the whitelist")
    void shouldRejectInvalidSortField() {
        assertThrows(InvalidSortFieldException.class,
                () -> productService.findAll(null, null, null, "price", "asc", 0, 20));
        verify(productRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject an invalid sort direction")
    void shouldRejectInvalidSortDirection() {
        assertThrows(InvalidSortFieldException.class,
                () -> productService.findAll(null, null, null, "name", "sideways", 0, 20));
        verify(productRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should create product with items")
    void shouldCreateProductWithItems() {
        ProductItemRequest itemReq = new ProductItemRequest("SKU001", new BigDecimal("100.00"), 10, 0.5, true, null);
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(itemReq), null);

        when(productRepository.existsBySlug("fishing-rod")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.create(request);

        assertEquals("Fishing Rod", response.name());
        assertEquals(1, response.items().size());
        assertEquals("SKU001", response.items().getFirst().sku());
    }

@Test
    @DisplayName("Should throw when creating duplicated slug")
    void shouldThrowWhenCreatingDuplicatedSlug() {
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(), null);
        when(productRepository.existsBySlug("fishing-rod")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.create(request));
    }

    @Test
    @DisplayName("Should throw when requiresLicense is true and licenseType is null")
    void shouldThrowWhenRequiresLicenseWithoutLicenseType() {
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", true, null, 1L, 1L, List.of(), null);

        assertThrows(LicenseTypeRequiredException.class, () -> productService.create(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should map licenseType through create")
    void shouldCreateProductWithLicenseType() {
        ProductItemRequest itemReq = new ProductItemRequest("SKU001", new BigDecimal("100.00"), 10, 0.5, true, null);
        ProductCreateRequest request = new ProductCreateRequest("Hunting Rifle", "hunting-rifle", "Desc", true, LicenseType.HUNTING, 1L, 1L, List.of(itemReq), null);

        when(productRepository.existsBySlug("hunting-rifle")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.create(request);

        assertEquals(LicenseType.HUNTING, response.licenseType());
    }

    @Test
    @DisplayName("Should throw when brand not found during create")
    void shouldThrowWhenBrandNotFound() {
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, null, 1L, 1L, List.of(), null);
        when(productRepository.existsBySlug("fishing-rod")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class, () -> productService.create(request));
    }

    @Test
    @DisplayName("Should update existing product")
    void shouldUpdateProduct() {
        ProductCreateRequest request = new ProductCreateRequest("Updated", "updated", "Desc", false, null, 1L, 1L, List.of(), null);
        
        when(productRepository.existsBySlugAndIdNot("updated", 1L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.update(1L, request);

        assertEquals("Updated", response.name());
        assertEquals("updated", response.slug());
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent product")
    void shouldThrowWhenDeletingNonExistent() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.delete(1L));
    }
}
