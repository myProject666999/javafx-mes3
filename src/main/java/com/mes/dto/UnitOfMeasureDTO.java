package com.mes.dto;

import com.mes.entity.UnitOfMeasure;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnitOfMeasureDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean baseUnit;
    private boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UnitOfMeasureDTO fromEntity(UnitOfMeasure unit) {
        UnitOfMeasureDTO dto = new UnitOfMeasureDTO();
        dto.setId(unit.getId());
        dto.setCode(unit.getCode());
        dto.setName(unit.getName());
        dto.setDescription(unit.getDescription());
        dto.setBaseUnit(unit.isBaseUnit());
        dto.setEnabled(unit.isEnabled());
        dto.setCreateTime(unit.getCreateTime());
        dto.setUpdateTime(unit.getUpdateTime());
        return dto;
    }

    public UnitOfMeasure toEntity() {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(this.id);
        unit.setCode(this.code);
        unit.setName(this.name);
        unit.setDescription(this.description);
        unit.setBaseUnit(this.baseUnit);
        unit.setEnabled(this.enabled);
        return unit;
    }
}
