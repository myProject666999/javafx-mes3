package com.mes.repository;

import com.mes.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {

    List<Process> findByEnabledTrueOrderByCodeAsc();

    List<Process> findByWorkshopIdOrderByCodeAsc(Long workshopId);

    Optional<Process> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT p FROM Process p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:workshopId IS NULL OR p.workshop.id = :workshopId) AND " +
           "(:enabled IS NULL OR p.enabled = :enabled) " +
           "ORDER BY p.code")
    List<Process> search(@Param("name") String name,
                         @Param("workshopId") Long workshopId,
                         @Param("enabled") Boolean enabled);

    long countByEnabledTrue();
}
