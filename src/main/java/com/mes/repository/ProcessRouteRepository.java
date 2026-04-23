package com.mes.repository;

import com.mes.entity.ProcessRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRouteRepository extends JpaRepository<ProcessRoute, Long> {

    List<ProcessRoute> findByEnabledTrueOrderByCodeAsc();

    Optional<ProcessRoute> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT pr FROM ProcessRoute pr LEFT JOIN FETCH pr.steps WHERE " +
           "(:name IS NULL OR pr.name LIKE %:name%) AND " +
           "(:enabled IS NULL OR pr.enabled = :enabled) " +
           "ORDER BY pr.code")
    List<ProcessRoute> search(@Param("name") String name,
                               @Param("enabled") Boolean enabled);

    @Query("SELECT pr FROM ProcessRoute pr LEFT JOIN FETCH pr.steps WHERE pr.id = :id")
    Optional<ProcessRoute> findByIdWithSteps(@Param("id") Long id);

    long countByEnabledTrue();
}
