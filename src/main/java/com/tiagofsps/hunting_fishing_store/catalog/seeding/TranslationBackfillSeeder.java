package com.tiagofsps.hunting_fishing_store.catalog.seeding;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.entity.CategoryTranslation;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductTranslation;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.common.seeding.Seeder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Keeps the translation tables populated for existing development databases
 * where Flyway is not used (schema is managed by Hibernate ddl-auto). Missing
 * English rows are created from the canonical columns; seeded categories also
 * get their Portuguese name. Admin-managed rows are never overwritten.
 */
@Component
public class TranslationBackfillSeeder implements Seeder {

    private static final Map<String, String> SEEDED_CATEGORY_NAMES_PT = Map.of(
            "fishing", "Pesca",
            "hunting", "Caça",
            "camping", "Campismo",
            "rods", "Caniços",
            "reels", "Carretos",
            "hunting-apparel", "Vestuário"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public TranslationBackfillSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void seed() {
        for (Category category : categoryRepository.findAll()) {
            boolean changed = ensureTranslation(category, "en", category.getName(), category.getDescription());
            String portugueseName = SEEDED_CATEGORY_NAMES_PT.get(category.getSlug());
            if (portugueseName != null) {
                changed |= ensureTranslation(category, "pt", portugueseName, null);
            }
            if (changed) {
                categoryRepository.save(category);
            }
        }

        for (Product product : productRepository.findAll()) {
            if (ensureTranslation(product, product.getName(), product.getDescription())) {
                productRepository.save(product);
            }
        }
    }

    private boolean ensureTranslation(Category category, String locale, String name, String description) {
        boolean exists = category.getTranslations().stream()
                .anyMatch(translation -> locale.equals(translation.getLocale()));
        if (exists) return false;

        CategoryTranslation translation = new CategoryTranslation();
        translation.setCategory(category);
        translation.setLocale(locale);
        translation.setName(name);
        translation.setDescription(description);
        category.getTranslations().add(translation);
        return true;
    }

    private boolean ensureTranslation(Product product, String name, String description) {
        boolean exists = product.getTranslations().stream()
                .anyMatch(translation -> "en".equals(translation.getLocale()));
        if (exists) return false;

        ProductTranslation translation = new ProductTranslation();
        translation.setProduct(product);
        translation.setLocale("en");
        translation.setName(name);
        translation.setDescription(description);
        product.getTranslations().add(translation);
        return true;
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
