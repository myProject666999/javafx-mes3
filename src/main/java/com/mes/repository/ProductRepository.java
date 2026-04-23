package com.mes.repository;

import com.mes.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Product> findByCategoryIdOrderByCodeAsc(Long categoryId);

    List<Product> findByCategoryIdInOrderByCodeAsc(List<Long> categoryIds);

    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds")
    List<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT p FROM Product p WHERE " +
           "(:code IS NULL OR p.code LIKE %:code%) AND " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:enabled IS NULL OR p.enabled = :enabled) " +
           "ORDER BY p.code")
    List<Product> search(@Param("code") String code,
                        @Param("name") String name,
                        @Param("categoryId") Long categoryId,
                        @Param("enabled") Boolean enabled);

    @Query("SELECT MAX(CAST(SUBSTRING(p.code, :prefixLength + 1) AS int)) FROM Product p WHERE p.code LIKE :prefix%")
    Integer findMaxCodeSequence(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
}
