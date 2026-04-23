package com.mes.dto;

import com.mes.entity.Workstation;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class WorkstationDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long workshopId;
    private String workshopName;
    private String processName;
    private int equipmentCount;
    private int workerCount;
    private boolean enabled;
    private String createTime;
    private String updateTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static WorkstationDTO fromEntity(Workstation workstation) {
        if (workstation == null) return null;

        WorkstationDTO dto = new WorkstationDTO();
        dto.setId(workstation.getId());
        dto.setCode(workstation.getCode());
        dto.setName(workstation.getName());
        dto.setDescription(workstation.getDescription());
        dto.setProcessName(workstation.getProcessName());
        dto.setEquipmentCount(workstation.getEquipmentCount());
        dto.setWorkerCount(workstation.getWorkerCount());
        dto.setEnabled(workstation.isEnabled());

        if (workstation.getWorkshop() != null) {
            dto.setWorkshopId(workstation.getWorkshop().getId());
            dto.setWorkshopName(workstation.getWorkshop().getName());
        }

        if (workstation.getCreateTime() != null) {
            dto.setCreateTime(workstation.getCreateTime().format(formatter));
        }
        if (workstation.getUpdateTime() != null) {
            dto.setUpdateTime(workstation.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
