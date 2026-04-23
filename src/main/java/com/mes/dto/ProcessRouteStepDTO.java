package com.mes.dto;

import com.mes.entity.ProcessRouteStep;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class ProcessRouteStepDTO {
    private Long id;
    private Long processRouteId;
    private Long processId;
    private String processCode;
    private String processName;
    private int stepOrder;
    private String description;
    private String createTime;
    private String updateTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ProcessRouteStepDTO fromEntity(ProcessRouteStep step) {
        if (step == null) return null;

        ProcessRouteStepDTO dto = new ProcessRouteStepDTO();
        dto.setId(step.getId());
        if (step.getProcessRoute() != null) {
            dto.setProcessRouteId(step.getProcessRoute().getId());
        }
        if (step.getProcess() != null) {
            dto.setProcessId(step.getProcess().getId());
            dto.setProcessCode(step.getProcess().getCode());
            dto.setProcessName(step.getProcess().getName());
        }
        dto.setStepOrder(step.getStepOrder());
        dto.setDescription(step.getDescription());

        if (step.getCreateTime() != null) {
            dto.setCreateTime(step.getCreateTime().format(formatter));
        }
        if (step.getUpdateTime() != null) {
            dto.setUpdateTime(step.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
