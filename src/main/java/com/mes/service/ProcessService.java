package com.mes.service;

import com.mes.dto.ProcessDTO;
import com.mes.entity.Process;
import com.mes.entity.Workshop;
import com.mes.repository.ProcessRepository;
import com.mes.repository.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessService {

    private final ProcessRepository processRepository;
    private final WorkshopRepository workshopRepository;

    public ProcessService(ProcessRepository processRepository, WorkshopRepository workshopRepository) {
        this.processRepository = processRepository;
        this.workshopRepository = workshopRepository;
    }

    public List<ProcessDTO> findAll() {
        return processRepository.findAll().stream()
                .map(ProcessDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> findAllEnabled() {
        return processRepository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(ProcessDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> findByWorkshopId(Long workshopId) {
        return processRepository.findByWorkshopIdOrderByCodeAsc(workshopId).stream()
                .map(ProcessDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ProcessDTO> findById(Long id) {
        return processRepository.findById(id)
                .map(ProcessDTO::fromEntity);
    }

    public Optional<Process> findEntityById(Long id) {
        return processRepository.findById(id);
    }

    public ProcessDTO save(ProcessDTO dto) {
        Process process = new Process();
        if (dto.getId() != null) {
            process = processRepository.findById(dto.getId()).orElse(process);
        }

        if (dto.getId() == null && dto.getCode() == null) {
            dto.setCode(generateCode());
        }

        process.setCode(dto.getCode());
        process.setName(dto.getName());
        process.setDescription(dto.getDescription());
        process.setEnabled(dto.isEnabled());

        if (dto.getWorkshopId() != null) {
            Workshop workshop = workshopRepository.findById(dto.getWorkshopId())
                    .orElseThrow(() -> new RuntimeException("车间不存在"));
            process.setWorkshop(workshop);
        } else {
            process.setWorkshop(null);
        }

        Process saved = processRepository.save(process);
        return ProcessDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        processRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return processRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return processRepository.existsByCodeAndIdNot(code, id);
    }

    public List<ProcessDTO> search(String name, Long workshopId, Boolean enabled) {
        if ((name == null || name.isEmpty()) && workshopId == null && enabled == null) {
            return findAll();
        }
        return processRepository.search(name, workshopId, enabled).stream()
                .map(ProcessDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        List<Process> all = processRepository.findAll();
        int maxSeq = 0;
        for (Process p : all) {
            String code = p.getCode();
            if (code != null && code.startsWith("PR")) {
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
        return "PR" + String.format("%04d", maxSeq + 1);
    }

    public long count() {
        return processRepository.count();
    }

    public long countEnabled() {
        return processRepository.countByEnabledTrue();
    }
}
