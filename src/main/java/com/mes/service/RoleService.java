package com.mes.service;

import com.mes.entity.Permission;
import com.mes.entity.Role;
import com.mes.repository.PermissionRepository;
import com.mes.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Transactional
    public Role create(Role role, List<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null) {
            permissionRepository.findAllById(permissionIds).forEach(permissions::add);
        }
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Role role, List<Long> permissionIds) {
        Role existing = roleRepository.findById(role.getId()).orElseThrow();
        existing.setName(role.getName());
        existing.setDescription(role.getDescription());

        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>();
            permissionRepository.findAllById(permissionIds).forEach(permissions::add);
            existing.setPermissions(permissions);
        }

        return roleRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
