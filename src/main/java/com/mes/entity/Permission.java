package com.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "permissions")
@EqualsAndHashCode(exclude = "roles")
@ToString(exclude = "roles")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType type = PermissionType.MENU;

    public enum PermissionType {
        MENU,
        BUTTON
    }

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
