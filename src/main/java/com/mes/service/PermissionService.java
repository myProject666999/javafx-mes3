package com.mes.service;

import com.mes.entity.Permission;
import com.mes.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Permission findById(Long id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Permission create(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission update(Permission permission) {
        Permission existing = permissionRepository.findById(permission.getId()).orElseThrow();
        existing.setName(permission.getName());
        existing.setDescription(permission.getDescription());
        return permissionRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }
}
