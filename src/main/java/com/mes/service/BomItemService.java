package com.mes.service;

import com.mes.dto.BomItemDTO;
import com.mes.entity.BomItem;
import com.mes.entity.Product;
import com.mes.entity.UnitOfMeasure;
import com.mes.repository.BomItemRepository;
import com.mes.repository.ProductRepository;
import com.mes.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BomItemService {

    private final BomItemRepository bomItemRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public BomItemService(BomItemRepository bomItemRepository,
                         ProductRepository productRepository,
                         UnitOfMeasureRepository unitOfMeasureRepository) {
        this.bomItemRepository = bomItemRepository;
        this.productRepository = productRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    public List<BomItemDTO> findByProductId(Long productId) {
        return bomItemRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(BomItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<BomItemDTO> findById(Long id) {
        return bomItemRepository.findById(id)
                .map(BomItemDTO::fromEntity);
    }

    public BomItemDTO save(BomItemDTO dto) {
        BomItem bomItem = new BomItem();
        if (dto.getId() != null) {
            bomItem = bomItemRepository.findById(dto.getId()).orElse(bomItem);
        }

        bomItem.setQuantity(dto.getQuantity());
        bomItem.setRemark(dto.getRemark());

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            bomItem.setProduct(product);
        }

        if (dto.getMaterialId() != null) {
            Product material = productRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物料不存在"));
            bomItem.setMaterial(material);
        }

        if (dto.getUnitId() != null) {
            UnitOfMeasure unit = unitOfMeasureRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new RuntimeException("单位不存在"));
            bomItem.setUnit(unit);
        }

        BomItem saved = bomItemRepository.save(bomItem);
        return BomItemDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        bomItemRepository.deleteById(id);
    }

    public void deleteByProductId(Long productId) {
        bomItemRepository.deleteByProductId(productId);
    }

    public List<BomItemDTO> saveAll(Long productId, List<BomItemDTO> dtoList) {
        // 删除原有的BOM项
        bomItemRepository.deleteByProductId(productId);

        // 保存新的BOM项
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        return dtoList.stream().map(dto -> {
            BomItem bomItem = new BomItem();
            bomItem.setProduct(product);
            bomItem.setQuantity(dto.getQuantity());
            bomItem.setRemark(dto.getRemark());

            if (dto.getMaterialId() != null) {
                Product material = productRepository.findById(dto.getMaterialId())
                        .orElseThrow(() -> new RuntimeException("物料不存在: " + dto.getMaterialId()));
                bomItem.setMaterial(material);
            }

            if (dto.getUnitId() != null) {
                UnitOfMeasure unit = unitOfMeasureRepository.findById(dto.getUnitId())
                        .orElseThrow(() -> new RuntimeException("单位不存在"));
                bomItem.setUnit(unit);
            } else if (bomItem.getMaterial() != null && bomItem.getMaterial().getUnit() != null) {
                bomItem.setUnit(bomItem.getMaterial().getUnit());
            }

            BomItem saved = bomItemRepository.save(bomItem);
            return BomItemDTO.fromEntity(saved);
        }).collect(Collectors.toList());
    }

    public boolean existsByMaterialId(Long materialId) {
        return bomItemRepository.existsByMaterialId(materialId);
    }
}
