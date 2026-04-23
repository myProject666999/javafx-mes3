package com.mes.repository;

import com.mes.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long>, JpaSpecificationExecutor<UnitOfMeasure> {
    Optional<UnitOfMeasure> findByCode(String code);
    
    List<UnitOfMeasure> findByNameContaining(String name);
    
    List<UnitOfMeasure> findByCodeContaining(String code);
    
    boolean existsByCode(String code);

    List<UnitOfMeasure> findByEnabledTrue();
}
