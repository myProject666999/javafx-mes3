package com.mes.dto;

import com.mes.entity.Permission;
import com.mes.entity.Role;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private String permissionNames;

    public static RoleDTO fromEntity(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setPermissionNames(role.getPermissions().stream()
                .map(Permission::getDescription)
                .collect(Collectors.joining(", ")));
        return dto;
    }
}
