package com.mes.service;

import com.mes.dto.ProductDTO;
import com.mes.entity.Product;
import com.mes.entity.ProductCategory;
import com.mes.entity.UnitOfMeasure;
import com.mes.repository.ProductCategoryRepository;
import com.mes.repository.ProductRepository;
import com.mes.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductCategoryService categoryService;

    public ProductService(ProductRepository productRepository,
                         ProductCategoryRepository categoryRepository,
                         UnitOfMeasureRepository unitOfMeasureRepository,
                         ProductCategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.categoryService = categoryService;
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return findAll();
        }
        List<Long> categoryIds = categoryService.getAllChildrenIds(categoryId);
        return productRepository.findByCategoryIdInOrderByCodeAsc(categoryIds).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id)
                .map(ProductDTO::fromEntity);
    }

    public Optional<Product> findEntityById(Long id) {
        return productRepository.findById(id);
    }

    public ProductDTO save(ProductDTO dto) {
        Product product = new Product();
        if (dto.getId() != null) {
            product = productRepository.findById(dto.getId()).orElse(product);
        }

        product.setCode(dto.getCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSpecification(dto.getSpecification());
        product.setMinStock(dto.getMinStock());
        product.setMaxStock(dto.getMaxStock());
        product.setEnabled(dto.isEnabled());

        if (dto.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            product.setCategory(category);
        }

        if (dto.getUnitId() != null) {
            UnitOfMeasure unit = unitOfMeasureRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new RuntimeException("单位不存在"));
            product.setUnit(unit);
        }

        Product saved = productRepository.save(product);
        return ProductDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return productRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return productRepository.existsByCodeAndIdNot(code, id);
    }

    public List<ProductDTO> search(String code, String name, Long categoryId, Boolean enabled) {
        return productRepository.search(
                (code == null || code.isEmpty()) ? null : code,
                (name == null || name.isEmpty()) ? null : name,
                categoryId,
                enabled
        ).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode(Long categoryId) {
        String prefix = "MAT";
        if (categoryId != null) {
            Optional<ProductCategory> category = categoryRepository.findById(categoryId);
            if (category.isPresent()) {
                String categoryCode = category.get().getCode();
                prefix = categoryCode.replaceAll("[^A-Za-z]", "");
                if (prefix.isEmpty()) {
                    prefix = "MAT";
                }
                prefix = prefix.toUpperCase();
            }
        }

        Integer maxSeq = productRepository.findMaxCodeSequence(prefix, prefix.length());
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;

        return prefix + String.format("%06d", nextSeq);
    }

    public List<ProductDTO> findAllSimple() {
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntitySimple)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> findByCategoryIdSimple(Long categoryId) {
        if (categoryId == null) {
            return findAllSimple();
        }
        List<Long> categoryIds = categoryService.getAllChildrenIds(categoryId);
        return productRepository.findByCategoryIdInOrderByCodeAsc(categoryIds).stream()
                .map(ProductDTO::fromEntitySimple)
                .collect(Collectors.toList());
    }
}
