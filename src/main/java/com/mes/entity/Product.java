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
@Table(name = "product")
@EqualsAndHashCode(exclude = {"category", "bomItems"})
@ToString(exclude = {"category", "bomItems"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String specification;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private UnitOfMeasure unit;

    @Column(name = "min_stock")
    private Double minStock;

    @Column(name = "max_stock")
    private Double maxStock;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BomItem> bomItems = new ArrayList<>();

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
