package com.mes.repository;

import com.mes.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    List<Customer> findAllByOrderByCodeAsc();
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:code IS NULL OR c.code LIKE %:code%) AND " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:enabled IS NULL OR c.enabled = :enabled) " +
           "ORDER BY c.code ASC")
    List<Customer> search(@Param("code") String code, 
                          @Param("name") String name, 
                          @Param("enabled") Boolean enabled);
    
    @Query("SELECT MAX(CAST(SUBSTRING(c.code, :prefixLength + 1) AS integer)) FROM Customer c WHERE c.code LIKE :prefix%")
    Integer findMaxCodeSequence(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
}
