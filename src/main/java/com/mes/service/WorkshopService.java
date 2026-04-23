package com.mes.service;

import com.mes.dto.WorkshopDTO;
import com.mes.entity.Workshop;
import com.mes.repository.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    public List<WorkshopDTO> findAll() {
        return workshopRepository.findAll().stream()
                .map(WorkshopDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WorkshopDTO> findAllEnabled() {
        return workshopRepository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(WorkshopDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<WorkshopDTO> findById(Long id) {
        return workshopRepository.findById(id)
                .map(WorkshopDTO::fromEntity);
    }

    public Optional<Workshop> findEntityById(Long id) {
        return workshopRepository.findById(id);
    }

    public WorkshopDTO save(WorkshopDTO dto) {
        Workshop workshop = new Workshop();
        if (dto.getId() != null) {
            workshop = workshopRepository.findById(dto.getId()).orElse(workshop);
        }

        if (dto.getId() == null && dto.getCode() == null) {
            dto.setCode(generateCode());
        }

        workshop.setCode(dto.getCode());
        workshop.setName(dto.getName());
        workshop.setDescription(dto.getDescription());
        workshop.setLocation(dto.getLocation());
        workshop.setManager(dto.getManager());
        workshop.setEnabled(dto.isEnabled());

        Workshop saved = workshopRepository.save(workshop);
        return WorkshopDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        workshopRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return workshopRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return workshopRepository.existsByCodeAndIdNot(code, id);
    }

    public List<WorkshopDTO> search(String name, Boolean enabled) {
        if ((name == null || name.isEmpty()) && enabled == null) {
            return findAll();
        }
        return workshopRepository.search(name, enabled).stream()
                .map(WorkshopDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        List<Workshop> all = workshopRepository.findAll();
        int maxSeq = 0;
        for (Workshop w : all) {
            String code = w.getCode();
            if (code != null && code.startsWith("WS")) {
                try {
                    String numPart = code.substring(2);
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
        return "WS" + String.format("%03d", maxSeq + 1);
    }

    public long count() {
        return workshopRepository.count();
    }

    public long countEnabled() {
        return workshopRepository.countByEnabledTrue();
    }
}
