package com.mes.repository;

import com.mes.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    List<Workshop> findByEnabledTrueOrderByCodeAsc();

    Optional<Workshop> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT w FROM Workshop w WHERE (:name IS NULL OR w.name LIKE %:name%) AND (:enabled IS NULL OR w.enabled = :enabled) ORDER BY w.code")
    List<Workshop> search(@Param("name") String name, @Param("enabled") Boolean enabled);

    long countByEnabledTrue();
}
