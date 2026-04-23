package com.mes.service;

import com.mes.dto.ProductCategoryDTO;
import com.mes.entity.ProductCategory;
import com.mes.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<ProductCategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(ProductCategoryDTO::fromEntityFlat)
                .collect(Collectors.toList());
    }

    public List<ProductCategoryDTO> findAllTree() {
        List<ProductCategory> rootCategories = categoryRepository.findByParentIsNullOrderByCodeAsc();
        return rootCategories.stream()
                .map(ProductCategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductCategoryDTO> findAllEnabled() {
        return categoryRepository.findByEnabledTrueOrderByCodeAsc().stream()
                .map(ProductCategoryDTO::fromEntityFlat)
                .collect(Collectors.toList());
    }

    public Optional<ProductCategoryDTO> findById(Long id) {
        return categoryRepository.findById(id)
                .map(ProductCategoryDTO::fromEntityFlat);
    }

    public Optional<ProductCategory> findEntityById(Long id) {
        return categoryRepository.findById(id);
    }

    public ProductCategoryDTO save(ProductCategoryDTO dto) {
        ProductCategory category = new ProductCategory();
        if (dto.getId() != null) {
            category = categoryRepository.findById(dto.getId()).orElse(category);
        }

        category.setCode(dto.getCode());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setEnabled(dto.isEnabled());

        if (dto.getParentId() != null) {
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("父分类不存在"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        ProductCategory saved = categoryRepository.save(category);
        return ProductCategoryDTO.fromEntityFlat(saved);
    }

    public void deleteById(Long id) {
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        categoryRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return categoryRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return categoryRepository.existsByCodeAndIdNot(code, id);
    }

    public List<ProductCategoryDTO> search(String name, Boolean enabled) {
        if ((name == null || name.isEmpty()) && enabled == null) {
            return findAll();
        }
        return categoryRepository.search(name, enabled).stream()
                .map(ProductCategoryDTO::fromEntityFlat)
                .collect(Collectors.toList());
    }

    public List<ProductCategoryDTO> getCategoryPath(Long categoryId) {
        List<ProductCategoryDTO> path = new ArrayList<>();
        Optional<ProductCategory> optional = categoryRepository.findById(categoryId);

        while (optional.isPresent()) {
            ProductCategory category = optional.get();
            path.add(0, ProductCategoryDTO.fromEntityFlat(category));
            optional = Optional.ofNullable(category.getParent());
        }

        return path;
    }

    public List<Long> getAllChildrenIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        collectChildrenIds(categoryId, ids);
        return ids;
    }

    private void collectChildrenIds(Long parentId, List<Long> ids) {
        List<ProductCategory> children = categoryRepository.findByParentIdOrderByCodeAsc(parentId);
        for (ProductCategory child : children) {
            ids.add(child.getId());
            collectChildrenIds(child.getId(), ids);
        }
    }

    public String generateCode(Long parentId) {
        String prefix;
        if (parentId == null) {
            prefix = "CAT";
        } else {
            ProductCategory parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("父分类不存在"));
            prefix = parent.getCode();
        }

        List<ProductCategory> siblings;
        if (parentId == null) {
            siblings = categoryRepository.findByParentIsNullOrderByCodeAsc();
        } else {
            siblings = categoryRepository.findByParentIdOrderByCodeAsc(parentId);
        }

        int maxSeq = 0;
        for (ProductCategory sibling : siblings) {
            String code = sibling.getCode();
            if (code.startsWith(prefix)) {
                try {
                    String numPart = code.substring(prefix.length());
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }

        return prefix + String.format("%03d", maxSeq + 1);
    }
}
