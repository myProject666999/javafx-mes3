package com.mes.service;

import com.mes.dto.CustomerDTO;
import com.mes.entity.Customer;
import com.mes.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerDTO> findAll() {
        return customerRepository.findAllByOrderByCodeAsc().stream()
                .map(CustomerDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<CustomerDTO> findById(Long id) {
        return customerRepository.findById(id)
                .map(CustomerDTO::fromEntity);
    }

    public CustomerDTO save(CustomerDTO dto) {
        Customer customer = new Customer();
        if (dto.getId() != null) {
            customer = customerRepository.findById(dto.getId()).orElse(customer);
        }

        customer.setCode(dto.getCode());
        customer.setName(dto.getName());
        customer.setContact(dto.getContact());
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
        customer.setAddress(dto.getAddress());
        customer.setTaxNumber(dto.getTaxNumber());
        customer.setBankAccount(dto.getBankAccount());
        customer.setBankName(dto.getBankName());
        customer.setRemark(dto.getRemark());
        customer.setEnabled(dto.isEnabled());

        Customer saved = customerRepository.save(customer);
        return CustomerDTO.fromEntity(saved);
    }

    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return customerRepository.existsByCode(code);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return customerRepository.existsByCodeAndIdNot(code, id);
    }

    public List<CustomerDTO> search(String code, String name, Boolean enabled) {
        return customerRepository.search(
                (code == null || code.isEmpty()) ? null : code,
                (name == null || name.isEmpty()) ? null : name,
                enabled
        ).stream()
                .map(CustomerDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String generateCode() {
        String prefix = "CUS";
        Integer maxSeq = customerRepository.findMaxCodeSequence(prefix, prefix.length());
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        return prefix + String.format("%06d", nextSeq);
    }
}
