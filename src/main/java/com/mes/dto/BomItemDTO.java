package com.mes.dto;

import com.mes.entity.BomItem;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class BomItemDTO {
    private Long id;
    private Long productId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialSpecification;
    private Double quantity;
    private Long unitId;
    private String unitName;
    private String remark;
    private String createTime;
    private String updateTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static BomItemDTO fromEntity(BomItem bomItem) {
        if (bomItem == null) return null;

        BomItemDTO dto = new BomItemDTO();
        dto.setId(bomItem.getId());
        dto.setQuantity(bomItem.getQuantity());
        dto.setRemark(bomItem.getRemark());

        if (bomItem.getProduct() != null) {
            dto.setProductId(bomItem.getProduct().getId());
        }

        if (bomItem.getMaterial() != null) {
            dto.setMaterialId(bomItem.getMaterial().getId());
            dto.setMaterialCode(bomItem.getMaterial().getCode());
            dto.setMaterialName(bomItem.getMaterial().getName());
            dto.setMaterialSpecification(bomItem.getMaterial().getSpecification());
        }

        if (bomItem.getUnit() != null) {
            dto.setUnitId(bomItem.getUnit().getId());
            dto.setUnitName(bomItem.getUnit().getName());
        }

        if (bomItem.getCreateTime() != null) {
            dto.setCreateTime(bomItem.getCreateTime().format(formatter));
        }
        if (bomItem.getUpdateTime() != null) {
            dto.setUpdateTime(bomItem.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
