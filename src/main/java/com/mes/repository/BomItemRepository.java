package com.mes.repository;

import com.mes.entity.BomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long> {

    List<BomItem> findByProductIdOrderByIdAsc(Long productId);

    @Modifying
    @Query("DELETE FROM BomItem b WHERE b.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    boolean existsByMaterialId(Long materialId);

    @Query("SELECT COUNT(b) FROM BomItem b WHERE b.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
}
