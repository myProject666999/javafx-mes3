package com.mes.dto;

import com.mes.entity.Supplier;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class SupplierDTO {
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

    public static SupplierDTO fromEntity(Supplier supplier) {
        if (supplier == null) return null;

        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setCode(supplier.getCode());
        dto.setName(supplier.getName());
        dto.setContact(supplier.getContact());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setAddress(supplier.getAddress());
        dto.setTaxNumber(supplier.getTaxNumber());
        dto.setBankAccount(supplier.getBankAccount());
        dto.setBankName(supplier.getBankName());
        dto.setRemark(supplier.getRemark());
        dto.setEnabled(supplier.isEnabled());

        if (supplier.getCreateTime() != null) {
            dto.setCreateTime(supplier.getCreateTime().format(formatter));
        }
        if (supplier.getUpdateTime() != null) {
            dto.setUpdateTime(supplier.getUpdateTime().format(formatter));
        }

        return dto;
    }
}
