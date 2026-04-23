package com.mes.repository;

import com.mes.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByParentIsNullOrderByCodeAsc();

    List<ProductCategory> findByParentIdOrderByCodeAsc(Long parentId);

    List<ProductCategory> findByEnabledTrueOrderByCodeAsc();

    Optional<ProductCategory> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c FROM ProductCategory c WHERE c.name LIKE %:name% AND (:enabled IS NULL OR c.enabled = :enabled) ORDER BY c.code")
    List<ProductCategory> searchByNameAndEnabled(@Param("name") String name, @Param("enabled") Boolean enabled);

    @Query("SELECT c FROM ProductCategory c WHERE (:name IS NULL OR c.name LIKE %:name%) AND (:enabled IS NULL OR c.enabled = :enabled) ORDER BY c.code")
    List<ProductCategory> search(@Param("name") String name, @Param("enabled") Boolean enabled);

    long countByParentId(Long parentId);
}
