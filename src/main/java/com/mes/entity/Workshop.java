package com.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "workshop")
@EqualsAndHashCode(exclude = {"workstations"})
@ToString(exclude = {"workstations"})
public class Workshop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String location;

    private String manager;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workstation> workstations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
