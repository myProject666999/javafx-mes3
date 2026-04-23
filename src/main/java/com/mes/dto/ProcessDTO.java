package com.mes.dto;

import com.mes.entity.Process;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class ProcessDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long workshopId;
    private String workshopName;
    private boolean enabled;
    private String createTime;
    private String updateTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ProcessDTO fromEntity(Process process) {
        if (process == null) return null;

        ProcessDTO dto = new ProcessDTO();
        dto.setId(process.getId());
        dto.setCode(process.getCode());
        dto.setName(process.getName());
        dto.setDescription(process.getDescription());
        dto.setEnabled(process.isEnabled());

        if (process.getWorkshop() != null) {
            dto.setWorkshopId(process.getWorkshop().getId());
            dto.setWorkshopName(process.getWorkshop().getName());
        }

        if (process.getCreateTime() != null) {
            dto.setCreateTime(process.getCreateTime().format(formatter));
        }
        if (process.getUpdateTime() != null) {
            dto.setUpdateTime(process.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
