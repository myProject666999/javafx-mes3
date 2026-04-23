package com.mes.dto;

import com.mes.entity.Customer;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class CustomerDTO {
    private Long id;
    private String code;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String taxNumber;
    private String bankAccount;
    private String bankName;
    private String remark;
    private boolean enabled;
    private String createTime;
    private String updateTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CustomerDTO fromEntity(Customer customer) {
        if (customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCode(customer.getCode());
        dto.setName(customer.getName());
        dto.setContact(customer.getContact());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setTaxNumber(customer.getTaxNumber());
        dto.setBankAccount(customer.getBankAccount());
        dto.setBankName(customer.getBankName());
        dto.setRemark(customer.getRemark());
        dto.setEnabled(customer.isEnabled());

        if (customer.getCreateTime() != null) {
            dto.setCreateTime(customer.getCreateTime().format(formatter));
        }
        if (customer.getUpdateTime() != null) {
            dto.setUpdateTime(customer.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
