package com.tiagofsps.hunting_fishing_store.catalog.seeding;

import com.tiagofsps.hunting_fishing_store.catalog.entity.*;
import com.tiagofsps.hunting_fishing_store.catalog.repository.*;
import com.tiagofsps.hunting_fishing_store.common.seeding.Seeder;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CatalogSeeder implements Seeder {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductImageRepository productImageRepository;
    private final Faker faker = new Faker();

    public CatalogSeeder(BrandRepository brandRepository, 
                         CategoryRepository categoryRepository,
                         ProductRepository productRepository, 
                         ProductItemRepository productItemRepository,
                         ProductImageRepository productImageRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productItemRepository = productItemRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    @Transactional
    public void seed() {
        if (categoryRepository.count() > 0) return;

        List<Category> categories = seedCategories();
        List<Brand> brands = seedBrands();
        seedProducts(categories, brands);
    }

    private List<Category> seedCategories() {
        List<Category> categories = new ArrayList<>();
        
        Category fishing = getOrCreateCategory("Fishing", "fishing", "Equipment for all types of fishing.");
        Category hunting = getOrCreateCategory("Hunting", "hunting", "Gear for hunting enthusiasts.");
        Category camping = getOrCreateCategory("Camping", "camping", "Tents, sleeping bags, and more.");
        
        categories.add(fishing);
        categories.add(hunting);
        categories.add(camping);

        // Subcategories
        categories.add(getOrCreateSubCategory("Rods", "rods", fishing));
        categories.add(getOrCreateSubCategory("Reels", "reels", fishing));
        categories.add(getOrCreateSubCategory("Apparel", "hunting-apparel", hunting));
        
        return categories;
    }

    private List<Brand> seedBrands() {
        List<Brand> brands = new ArrayList<>();
        brands.add(getOrCreateBrand("Shimano", "shimano"));
        brands.add(getOrCreateBrand("Browning", "browning"));
        brands.add(getOrCreateBrand("Patagonia", "patagonia"));
        brands.add(getOrCreateBrand("Abu Garcia", "abu-garcia"));
        return brands;
    }

    private void seedProducts(List<Category> categories, List<Brand> brands) {
        for (int i = 0; i < 15; i++) {
            Category category = categories.get(faker.random().nextInt(categories.size()));
            Brand brand = brands.get(faker.random().nextInt(brands.size()));
            
            Product product = new Product();
            String name = faker.commerce().productName();
            product.setName(name);
            product.setSlug(name.toLowerCase().replace(" ", "-") + "-" + i);
            product.setDescription(faker.lorem().paragraph());
            product.setCategory(category);
            product.setBrand(brand);
            
            product = productRepository.save(product);

            seedImages(product);

            // Seed ProductItems (Variants)
            for (int j = 0; j < faker.random().nextInt(1, 3); j++) {
                ProductItem item = new ProductItem();
                item.setSku(faker.code().asin() + "-" + i + "-" + j);
                item.setPrice(new BigDecimal(faker.commerce().price(10.0, 500.0)));
                item.setStockQuantity(faker.random().nextInt(0, 50));
                item.setProduct(product);
                item.setAttributes(Map.of("color", faker.color().name(), "size", "Medium"));
                productItemRepository.save(item);
            }
        }
    }

    private Category getOrCreateCategory(String name, String slug, String description) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setSlug(slug);
                    category.setDescription(description);
                    return categoryRepository.save(category);
                });
    }

    private Category getOrCreateSubCategory(String name, String slug, Category parent) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setSlug(slug);
                    category.setParentCategory(parent);
                    return categoryRepository.save(category);
                });
    }

    private Brand getOrCreateBrand(String name, String slug) {
        return brandRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Brand brand = new Brand();
                    brand.setName(name);
                    brand.setSlug(slug);
                    return brandRepository.save(brand);
                });
    }

    private void seedImages(Product product) {
        String keyword = getKeywordForCategory(product.getCategory());
        
        // Primary Image
        ProductImage primary = new ProductImage();
        primary.setImageUrl("/images/products/" + keyword + "-primary.svg");
        primary.setAltText(product.getName());
        primary.setIsPrimary(true);
        primary.setDisplayOrder(0);
        primary.setProduct(product);
        productImageRepository.save(primary);

        // Secondary Image
        ProductImage secondary = new ProductImage();
        secondary.setImageUrl("/images/products/" + keyword + "-detail.svg");
        secondary.setAltText(product.getName() + " details");
        secondary.setIsPrimary(false);
        secondary.setDisplayOrder(1);
        secondary.setProduct(product);
        productImageRepository.save(secondary);
    }

    private String getKeywordForCategory(Category category) {
        String slug = category.getSlug().toLowerCase();
        if (slug.contains("fish")) return "fishing";
        if (slug.contains("hunt")) return "hunting";
        if (slug.contains("camp")) return "camping";
        
        if (category.getParentCategory() != null) {
            return getKeywordForCategory(category.getParentCategory());
        }
        
        return "nature";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
