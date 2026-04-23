package com.mes.service;

import com.mes.dto.SupplierDTO;
import com.mes.entity.Supplier;
import com.mes.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierDTO> findAll() {
        return supplierRepository.findAllByOrderByCodeAsc().stream()
                .map(SupplierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<SupplierDTO> findById(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierDTO::fromEntity);
    }

    public SupplierDTO save(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        if (dto.getId() != null) {
            supplier = supplierRepository.findById(dto.getId()).orElse(supplier);
        }

        supplier.setCode(dto.getCode());
        supplier.setName(dto.getName());
        supplier.setContact(dto.getContact());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setTaxNumber(dto.getTaxNumber());
        supplier.setBankAccount(dto.getBankAccount());
        supplier.setBankName(dto.getBankName());
        supplier.setRemark(dto.getRemark());
        supplier.setEnabled(dto.isEnabled());

        Supplier saved = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        supplierRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return supplierRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return supplierRepository.existsByCodeAndIdNot(code, id);
    }

    public List<SupplierDTO> search(String code, String name, Boolean enabled) {
        return supplierRepository.search(
                (code == null || code.isEmpty()) ? null : code,
                (name == null || name.isEmpty()) ? null : name,
                enabled
        ).stream()
                .map(SupplierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        String prefix = "SUP";
        Integer maxSeq = supplierRepository.findMaxCodeSequence(prefix, prefix.length());
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        return prefix + String.format("%06d", nextSeq);
    }
}
