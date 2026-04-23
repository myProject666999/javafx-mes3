package com.mes.dto;

import com.mes.entity.Workshop;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class WorkshopDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String location;
    private String manager;
    private boolean enabled;
    private String createTime;
    private String updateTime;
    private int workstationCount;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static WorkshopDTO fromEntity(Workshop workshop) {
        if (workshop == null) return null;

        WorkshopDTO dto = new WorkshopDTO();
        dto.setId(workshop.getId());
        dto.setCode(workshop.getCode());
        dto.setName(workshop.getName());
        dto.setDescription(workshop.getDescription());
        dto.setLocation(workshop.getLocation());
        dto.setManager(workshop.getManager());
        dto.setEnabled(workshop.isEnabled());

        if (workshop.getCreateTime() != null) {
            dto.setCreateTime(workshop.getCreateTime().format(formatter));
        }
        if (workshop.getUpdateTime() != null) {
            dto.setUpdateTime(workshop.getUpdateTime().format(formatter));
        }

        if (workshop.getWorkstations() != null) {
            dto.setWorkstationCount(workshop.getWorkstations().size());
        }

        return dto;
    }
}
