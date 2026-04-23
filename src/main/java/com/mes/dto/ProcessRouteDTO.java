package com.mes.dto;

import com.mes.entity.ProcessRoute;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProcessRouteDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long keyProcessId;
    private String keyProcessCode;
    private String keyProcessName;
    private boolean enabled;
    private String createTime;
    private String updateTime;
    private List<ProcessRouteStepDTO> steps;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ProcessRouteDTO fromEntity(ProcessRoute processRoute) {
        if (processRoute == null) return null;

        ProcessRouteDTO dto = new ProcessRouteDTO();
        dto.setId(processRoute.getId());
        dto.setCode(processRoute.getCode());
        dto.setName(processRoute.getName());
        dto.setDescription(processRoute.getDescription());
        dto.setEnabled(processRoute.isEnabled());

        if (processRoute.getKeyProcess() != null) {
            dto.setKeyProcessId(processRoute.getKeyProcess().getId());
            dto.setKeyProcessCode(processRoute.getKeyProcess().getCode());
            dto.setKeyProcessName(processRoute.getKeyProcess().getName());
        }

        if (processRoute.getCreateTime() != null) {
            dto.setCreateTime(processRoute.getCreateTime().format(formatter));
        }
        if (processRoute.getUpdateTime() != null) {
            dto.setUpdateTime(processRoute.getUpdateTime().format(formatter));
        }

        if (processRoute.getSteps() != null) {
            dto.setSteps(processRoute.getSteps().stream()
                    .map(ProcessRouteStepDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
