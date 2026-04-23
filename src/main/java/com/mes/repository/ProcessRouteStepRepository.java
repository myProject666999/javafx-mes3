package com.mes.repository;

import com.mes.entity.ProcessRouteStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessRouteStepRepository extends JpaRepository<ProcessRouteStep, Long> {

    List<ProcessRouteStep> findByProcessRouteIdOrderByStepOrderAsc(Long processRouteId);

    void deleteByProcessRouteId(Long processRouteId);

    long countByProcessId(Long processId);
}
