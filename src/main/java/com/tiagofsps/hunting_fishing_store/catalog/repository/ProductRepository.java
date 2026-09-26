package com.tiagofsps.hunting_fishing_store.catalog.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

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
   * Builds the paginated, filterable product search as a Criteria API
   * {@link Specification}. Each supplied filter adds one predicate; omitted
   * filters are simply not applied.
   *
   * <p>This deliberately replaces a JPQL {@code :param is null or ...} query.
   * That form binds an untyped NULL placeholder, which the PostgreSQL driver
   * sends as {@code bytea}, so any request that omitted a filter failed with
   * {@code function lower(bytea) does not exist}. A Criteria predicate is only
   * ever bound with its real Java type, so absent filters are safe.
   */
  static Specification<Product> search(Long categoryId, Long brandId, String query) {
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (categoryId != null) {
        predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
      }
      if (brandId != null) {
        predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
      }
      if (query != null && !query.isBlank()) {
        String pattern = "%" + query.toLowerCase() + "%";
        predicates.add(criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
            criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("description")),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern))));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
