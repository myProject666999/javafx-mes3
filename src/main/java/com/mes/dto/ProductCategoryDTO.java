package com.mes.dto;

import com.mes.entity.ProductCategory;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductCategoryDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private boolean enabled;
    private String createTime;
    private String updateTime;
    private List<ProductCategoryDTO> children = new ArrayList<>();
    private boolean expanded = false;
    private int level = 0;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ProductCategoryDTO fromEntity(ProductCategory category) {
        return fromEntity(category, 0);
    }

    public static ProductCategoryDTO fromEntity(ProductCategory category, int level) {
        if (category == null) return null;

        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(category.getId());
        dto.setCode(category.getCode());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setEnabled(category.isEnabled());
        dto.setLevel(level);

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        if (category.getCreateTime() != null) {
            dto.setCreateTime(category.getCreateTime().format(formatter));
        }
        if (category.getUpdateTime() != null) {
            dto.setUpdateTime(category.getUpdateTime().format(formatter));
        }

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(child -> fromEntity(child, level + 1))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static ProductCategoryDTO fromEntityFlat(ProductCategory category) {
        if (category == null) return null;

        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(category.getId());
        dto.setCode(category.getCode());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setEnabled(category.isEnabled());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        if (category.getCreateTime() != null) {
            dto.setCreateTime(category.getCreateTime().format(formatter));
        }
        if (category.getUpdateTime() != null) {
            dto.setUpdateTime(category.getUpdateTime().format(formatter));
        }

        return dto;
    }

    public String getDisplayName() {
        String indent = "  ".repeat(level);
        return indent + name;
    }

    public String getFullName() {
        if (parentName != null && !parentName.isEmpty()) {
            return parentName + " > " + name;
        }
        return name;
    }
}
