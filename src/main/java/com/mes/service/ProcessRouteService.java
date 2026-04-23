package com.mes.service;

import com.mes.dto.ProcessRouteDTO;
import com.mes.dto.ProcessRouteStepDTO;
import com.mes.entity.Process;
import com.mes.entity.ProcessRoute;
import com.mes.entity.ProcessRouteStep;
import com.mes.repository.ProcessRepository;
import com.mes.repository.ProcessRouteRepository;
import com.mes.repository.ProcessRouteStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessRouteService {

    private final ProcessRouteRepository processRouteRepository;
    private final ProcessRouteStepRepository processRouteStepRepository;
    private final ProcessRepository processRepository;

    public ProcessRouteService(ProcessRouteRepository processRouteRepository,
                                ProcessRouteStepRepository processRouteStepRepository,
                                ProcessRepository processRepository) {
        this.processRouteRepository = processRouteRepository;
        this.processRouteStepRepository = processRouteStepRepository;
        this.processRepository = processRepository;
    }

    public List<ProcessRouteDTO> findAll() {
        return processRouteRepository.findAll().stream()
                .map(ProcessRouteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProcessRouteDTO> findAllEnabled() {
        return processRouteRepository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(ProcessRouteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ProcessRouteDTO> findById(Long id) {
        return processRouteRepository.findByIdWithSteps(id)
                .map(ProcessRouteDTO::fromEntity);
    }

    public Optional<ProcessRoute> findEntityById(Long id) {
        return processRouteRepository.findById(id);
    }

    public ProcessRouteDTO save(ProcessRouteDTO dto) {
        ProcessRoute processRoute = new ProcessRoute();
        if (dto.getId() != null) {
            processRoute = processRouteRepository.findById(dto.getId()).orElse(processRoute);
        }

        if (dto.getId() == null && dto.getCode() == null) {
            dto.setCode(generateCode());
        }

        processRoute.setCode(dto.getCode());
        processRoute.setName(dto.getName());
        processRoute.setDescription(dto.getDescription());
        processRoute.setEnabled(dto.isEnabled());

        if (dto.getKeyProcessId() != null) {
            Process keyProcess = processRepository.findById(dto.getKeyProcessId())
                    .orElseThrow(() -> new RuntimeException("关键工序不存在"));
            processRoute.setKeyProcess(keyProcess);
        } else {
            processRoute.setKeyProcess(null);
        }

        ProcessRoute saved = processRouteRepository.save(processRoute);

        if (dto.getSteps() != null) {
            updateSteps(saved, dto.getSteps());
        }

        return ProcessRouteDTO.fromEntity(processRouteRepository.findByIdWithSteps(saved.getId()).orElse(saved));
    }

    private void updateSteps(ProcessRoute processRoute, List<ProcessRouteStepDTO> stepDtos) {
        processRoute.getSteps().clear();

        for (ProcessRouteStepDTO stepDto : stepDtos) {
            ProcessRouteStep step = new ProcessRouteStep();
            step.setProcessRoute(processRoute);
            step.setStepOrder(stepDto.getStepOrder());
            step.setDescription(stepDto.getDescription());

            if (stepDto.getProcessId() != null) {
                Process process = processRepository.findById(stepDto.getProcessId())
                        .orElseThrow(() -> new RuntimeException("工序不存在: " + stepDto.getProcessId()));
                step.setProcess(process);
            }

            processRoute.getSteps().add(step);
        }
    }

    public void deleteById(Long id) {
        processRouteRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return processRouteRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return processRouteRepository.existsByCodeAndIdNot(code, id);
    }

    public List<ProcessRouteDTO> search(String name, Boolean enabled) {
        if ((name == null || name.isEmpty()) && enabled == null) {
            return findAll();
        }
        return processRouteRepository.search(name, enabled).stream()
                .map(ProcessRouteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        List<ProcessRoute> all = processRouteRepository.findAll();
        int maxSeq = 0;
        for (ProcessRoute pr : all) {
            String code = pr.getCode();
            if (code != null && code.startsWith("PRT")) {
                try {
                    String numPart = code.substring(3);
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
        return "PRT" + String.format("%04d", maxSeq + 1);
    }

    public long count() {
        return processRouteRepository.count();
    }

    public long countEnabled() {
        return processRouteRepository.countByEnabledTrue();
    }

    public List<ProcessRouteStepDTO> findStepsByProcessRouteId(Long processRouteId) {
        return processRouteStepRepository.findByProcessRouteIdOrderByStepOrderAsc(processRouteId).stream()
                .map(ProcessRouteStepDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
