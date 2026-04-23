package com.mes.service;

import com.mes.entity.UnitOfMeasure;
import com.mes.repository.UnitOfMeasureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public UnitOfMeasureService(UnitOfMeasureRepository unitOfMeasureRepository) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    public List<UnitOfMeasure> findAll() {
        return unitOfMeasureRepository.findAll();
    }

    public List<UnitOfMeasure> findAllEnabled() {
        return unitOfMeasureRepository.findByEnabledTrue();
    }

    public Page<UnitOfMeasure> findAll(Pageable pageable) {
        return unitOfMeasureRepository.findAll(pageable);
    }

    public Page<UnitOfMeasure> search(String code, String name, Pageable pageable) {
        Specification<UnitOfMeasure> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.trim().isEmpty()) {
                predicates.add(cb.like(root.get("code"), "%" + code.trim() + "%"));
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return unitOfMeasureRepository.findAll(spec, pageable);
    }

    public List<UnitOfMeasure> search(String code, String name) {
        Specification<UnitOfMeasure> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.trim().isEmpty()) {
                predicates.add(cb.like(root.get("code"), "%" + code.trim() + "%"));
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return unitOfMeasureRepository.findAll(spec);
    }

    public UnitOfMeasure findById(Long id) {
        return unitOfMeasureRepository.findById(id).orElse(null);
    }

    @Transactional
    public UnitOfMeasure create(UnitOfMeasure unit) {
        if (unitOfMeasureRepository.existsByCode(unit.getCode())) {
            throw new RuntimeException("单位编码已存在: " + unit.getCode());
        }
        return unitOfMeasureRepository.save(unit);
    }

    @Transactional
    public UnitOfMeasure update(UnitOfMeasure unit) {
        UnitOfMeasure existing = unitOfMeasureRepository.findById(unit.getId())
                .orElseThrow(() -> new RuntimeException("计量单位不存在"));
        
        if (!existing.getCode().equals(unit.getCode()) && 
            unitOfMeasureRepository.existsByCode(unit.getCode())) {
            throw new RuntimeException("单位编码已存在: " + unit.getCode());
        }
        
        existing.setCode(unit.getCode());
        existing.setName(unit.getName());
        existing.setDescription(unit.getDescription());
        existing.setBaseUnit(unit.isBaseUnit());
        existing.setEnabled(unit.isEnabled());
        return unitOfMeasureRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        unitOfMeasureRepository.deleteById(id);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        unitOfMeasureRepository.deleteAllById(ids);
    }
}
