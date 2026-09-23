package com.tiagofsps.hunting_fishing_store.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Long id);

  /**
   * One grouped query returning categoryId -> product count, avoiding N+1
   * lookups when building CategoryResponse.productCount.
   */
  @Query("select p.category.id, count(p) from Product p group by p.category.id")
  List<Object[]> countProductsByCategory();

  /**
   * Paginated, filterable product search. All filters are optional and handled
   * by the single query; null/blank parameters are ignored.
   */
  @Query("""
          select p from Product p
          where (:categoryId is null or p.category.id = :categoryId)
            and (:brandId is null or p.brand.id = :brandId)
            and (:q is null or lower(p.name) like lower(concat('%', :q, '%'))
              or (p.description is not null and lower(p.description) like lower(concat('%', :q, '%'))))
          """)
  Page<Product> search(@Param("categoryId") Long categoryId,
                       @Param("brandId") Long brandId,
                       @Param("q") String q,
                       Pageable pageable);
}
