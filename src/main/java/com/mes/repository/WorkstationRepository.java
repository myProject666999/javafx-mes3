package com.mes.repository;

import com.mes.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, Long> {

    List<Workstation> findByEnabledTrueOrderByCodeAsc();

    List<Workstation> findByWorkshopIdOrderByCodeAsc(Long workshopId);

    Optional<Workstation> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT w FROM Workstation w WHERE " +
           "(:name IS NULL OR w.name LIKE %:name%) AND " +
           "(:workshopId IS NULL OR w.workshop.id = :workshopId) AND " +
           "(:processName IS NULL OR w.processName LIKE %:processName%) AND " +
           "(:enabled IS NULL OR w.enabled = :enabled) " +
           "ORDER BY w.code")
    List<Workstation> search(@Param("name") String name, 
                             @Param("workshopId") Long workshopId,
                             @Param("processName") String processName,
                             @Param("enabled") Boolean enabled);

    long countByWorkshopId(Long workshopId);

    long countByEnabledTrue();

    @Query("SELECT DISTINCT w.processName FROM Workstation w WHERE w.processName IS NOT NULL ORDER BY w.processName")
    List<String> findAllProcessNames();
}
