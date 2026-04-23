package com.mes.service;

import com.mes.dto.WorkstationDTO;
import com.mes.entity.Workshop;
import com.mes.entity.Workstation;
import com.mes.repository.WorkshopRepository;
import com.mes.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkstationService {

    private final WorkstationRepository workstationRepository;
    private final WorkshopRepository workshopRepository;

    public WorkstationService(WorkstationRepository workstationRepository, WorkshopRepository workshopRepository) {
        this.workstationRepository = workstationRepository;
        this.workshopRepository = workshopRepository;
    }

    public List<WorkstationDTO> findAll() {
        return workstationRepository.findAll().stream()
                .map(WorkstationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WorkstationDTO> findAllEnabled() {
        return workstationRepository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(WorkstationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<WorkstationDTO> findByWorkshopId(Long workshopId) {
        return workstationRepository.findByWorkshopIdOrderByCodeAsc(workshopId).stream()
                .map(WorkstationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<WorkstationDTO> findById(Long id) {
        return workstationRepository.findById(id)
                .map(WorkstationDTO::fromEntity);
    }

    public Optional<Workstation> findEntityById(Long id) {
        return workstationRepository.findById(id);
    }

    public WorkstationDTO save(WorkstationDTO dto) {
        Workstation workstation = new Workstation();
        if (dto.getId() != null) {
            workstation = workstationRepository.findById(dto.getId()).orElse(workstation);
        }

        if (dto.getId() == null && dto.getCode() == null) {
            dto.setCode(generateCode());
        }

        workstation.setCode(dto.getCode());
        workstation.setName(dto.getName());
        workstation.setDescription(dto.getDescription());
        workstation.setProcessName(dto.getProcessName());
        workstation.setEquipmentCount(dto.getEquipmentCount());
        workstation.setWorkerCount(dto.getWorkerCount());
        workstation.setEnabled(dto.isEnabled());

        if (dto.getWorkshopId() != null) {
            Workshop workshop = workshopRepository.findById(dto.getWorkshopId())
                    .orElseThrow(() -> new RuntimeException("车间不存在"));
            workstation.setWorkshop(workshop);
        } else {
            workstation.setWorkshop(null);
        }

        Workstation saved = workstationRepository.save(workstation);
        return WorkstationDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        workstationRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return workstationRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return workstationRepository.existsByCodeAndIdNot(code, id);
    }

    public List<WorkstationDTO> search(String name, Long workshopId, String processName, Boolean enabled) {
        if ((name == null || name.isEmpty()) && workshopId == null && 
            (processName == null || processName.isEmpty()) && enabled == null) {
            return findAll();
        }
        return workstationRepository.search(name, workshopId, processName, enabled).stream()
                .map(WorkstationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        List<Workstation> all = workstationRepository.findAll();
        int maxSeq = 0;
        for (Workstation w : all) {
            String code = w.getCode();
            if (code != null && code.startsWith("WK")) {
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
        return "WK" + String.format("%04d", maxSeq + 1);
    }

    public long count() {
        return workstationRepository.count();
    }

    public long countEnabled() {
        return workstationRepository.countByEnabledTrue();
    }

    public List<String> findAllProcessNames() {
        return workstationRepository.findAllProcessNames();
    }
}
