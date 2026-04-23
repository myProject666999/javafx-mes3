package com.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bom_item")
@EqualsAndHashCode(exclude = {"product", "material"})
@ToString(exclude = {"product", "material"})
public class BomItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Product material;

    @Column(nullable = false)
    private Double quantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private UnitOfMeasure unit;

    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

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
