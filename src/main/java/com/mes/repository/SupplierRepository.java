package com.mes.repository;

import com.mes.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    List<Supplier> findAllByOrderByCodeAsc();
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:code IS NULL OR s.code LIKE %:code%) AND " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:enabled IS NULL OR s.enabled = :enabled) " +
           "ORDER BY s.code ASC")
    List<Supplier> search(@Param("code") String code, 
                          @Param("name") String name, 
                          @Param("enabled") Boolean enabled);
    
    @Query("SELECT MAX(CAST(SUBSTRING(s.code, :prefixLength + 1) AS integer)) FROM Supplier s WHERE s.code LIKE :prefix%")
    Integer findMaxCodeSequence(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
}
