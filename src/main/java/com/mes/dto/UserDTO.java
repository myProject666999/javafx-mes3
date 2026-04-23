package com.mes.dto;

import com.mes.entity.Role;
import com.mes.entity.User;
import lombok.Data;

import java.util.stream.Collectors;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String roleNames;
    private String enabledText;
    private boolean enabled;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEnabled(user.isEnabled());
        dto.setEnabledText(user.isEnabled() ? "启用" : "禁用");
        dto.setRoleNames(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(", ")));
        return dto;
    }
}
