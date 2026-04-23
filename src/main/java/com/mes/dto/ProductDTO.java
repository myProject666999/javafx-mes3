package com.mes.dto;

import com.mes.entity.Product;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String specification;
    private Long categoryId;
    private String categoryName;
    private Long unitId;
    private String unitName;
    private Double minStock;
    private Double maxStock;
    private boolean enabled;
    private String createTime;
    private String updateTime;
    private List<BomItemDTO> bomItems = new ArrayList<>();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ProductDTO fromEntity(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSpecification(product.getSpecification());
        dto.setMinStock(product.getMinStock());
        dto.setMaxStock(product.getMaxStock());
        dto.setEnabled(product.isEnabled());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getUnit() != null) {
            dto.setUnitId(product.getUnit().getId());
            dto.setUnitName(product.getUnit().getName());
        }

        if (product.getCreateTime() != null) {
            dto.setCreateTime(product.getCreateTime().format(formatter));
        }
        if (product.getUpdateTime() != null) {
            dto.setUpdateTime(product.getUpdateTime().format(formatter));
        }

        if (product.getBomItems() != null && !product.getBomItems().isEmpty()) {
            dto.setBomItems(product.getBomItems().stream()
                    .map(BomItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static ProductDTO fromEntitySimple(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setSpecification(product.getSpecification());
        dto.setEnabled(product.isEnabled());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getUnit() != null) {
            dto.setUnitId(product.getUnit().getId());
            dto.setUnitName(product.getUnit().getName());
        }

        return dto;
    }

    public String getStockRange() {
        if (minStock != null && maxStock != null) {
            return minStock + " - " + maxStock;
        } else if (minStock != null) {
            return "≥ " + minStock;
        } else if (maxStock != null) {
            return "≤ " + maxStock;
        }
        return "-";
    }
}
