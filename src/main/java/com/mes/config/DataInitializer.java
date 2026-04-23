package com.mes.config;

import com.mes.entity.Permission;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.repository.PermissionRepository;
import com.mes.repository.RoleRepository;
import com.mes.repository.UserRepository;
import com.mes.util.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        Permission userManage = createPermissionIfNotExists("user:manage", "用户管理", Permission.PermissionType.MENU);
        Permission userAdd = createPermissionIfNotExists("user:add", "添加用户", Permission.PermissionType.BUTTON);
        Permission userEdit = createPermissionIfNotExists("user:edit", "编辑用户", Permission.PermissionType.BUTTON);
        Permission userDelete = createPermissionIfNotExists("user:delete", "删除用户", Permission.PermissionType.BUTTON);
        Permission userResetPassword = createPermissionIfNotExists("user:resetPassword", "重置密码", Permission.PermissionType.BUTTON);
        
        Permission roleManage = createPermissionIfNotExists("role:manage", "角色管理", Permission.PermissionType.MENU);
        Permission roleAdd = createPermissionIfNotExists("role:add", "添加角色", Permission.PermissionType.BUTTON);
        Permission roleEdit = createPermissionIfNotExists("role:edit", "编辑角色", Permission.PermissionType.BUTTON);
        Permission roleDelete = createPermissionIfNotExists("role:delete", "删除角色", Permission.PermissionType.BUTTON);
        
        Permission permissionManage = createPermissionIfNotExists("permission:manage", "权限管理", Permission.PermissionType.MENU);
        Permission permissionAdd = createPermissionIfNotExists("permission:add", "添加权限", Permission.PermissionType.BUTTON);
        Permission permissionEdit = createPermissionIfNotExists("permission:edit", "编辑权限", Permission.PermissionType.BUTTON);
        Permission permissionDelete = createPermissionIfNotExists("permission:delete", "删除权限", Permission.PermissionType.BUTTON);
        
        Permission passwordChange = createPermissionIfNotExists("password:change", "修改密码", Permission.PermissionType.MENU);
        Permission mesView = createPermissionIfNotExists("mes:view", "查看MES数据", Permission.PermissionType.MENU);
        
        Permission uomManage = createPermissionIfNotExists("uom:manage", "计量单位管理", Permission.PermissionType.MENU);
        Permission uomAdd = createPermissionIfNotExists("uom:add", "添加计量单位", Permission.PermissionType.BUTTON);
        Permission uomEdit = createPermissionIfNotExists("uom:edit", "编辑计量单位", Permission.PermissionType.BUTTON);
        Permission uomDelete = createPermissionIfNotExists("uom:delete", "删除计量单位", Permission.PermissionType.BUTTON);

        // 产品管理权限
        Permission productCategoryManage = createPermissionIfNotExists("product_category:manage", "物料产品分类", Permission.PermissionType.MENU);
        Permission productCategoryAdd = createPermissionIfNotExists("product_category:add", "添加分类", Permission.PermissionType.BUTTON);
        Permission productCategoryEdit = createPermissionIfNotExists("product_category:edit", "编辑分类", Permission.PermissionType.BUTTON);
        Permission productCategoryDelete = createPermissionIfNotExists("product_category:delete", "删除分类", Permission.PermissionType.BUTTON);

        Permission productManage = createPermissionIfNotExists("product:manage", "物料产品管理", Permission.PermissionType.MENU);
        Permission productAdd = createPermissionIfNotExists("product:add", "添加物料", Permission.PermissionType.BUTTON);
        Permission productEdit = createPermissionIfNotExists("product:edit", "编辑物料", Permission.PermissionType.BUTTON);
        Permission productDelete = createPermissionIfNotExists("product:delete", "删除物料", Permission.PermissionType.BUTTON);

        Permission workshopManage = createPermissionIfNotExists("workshop:manage", "车间设置", Permission.PermissionType.MENU);
        Permission workshopAdd = createPermissionIfNotExists("workshop:add", "添加车间", Permission.PermissionType.BUTTON);
        Permission workshopEdit = createPermissionIfNotExists("workshop:edit", "编辑车间", Permission.PermissionType.BUTTON);
        Permission workshopDelete = createPermissionIfNotExists("workshop:delete", "删除车间", Permission.PermissionType.BUTTON);

        Permission workstationManage = createPermissionIfNotExists("workstation:manage", "工作站管理", Permission.PermissionType.MENU);
        Permission workstationAdd = createPermissionIfNotExists("workstation:add", "添加工作站", Permission.PermissionType.BUTTON);
        Permission workstationEdit = createPermissionIfNotExists("workstation:edit", "编辑工作站", Permission.PermissionType.BUTTON);
        Permission workstationDelete = createPermissionIfNotExists("workstation:delete", "删除工作站", Permission.PermissionType.BUTTON);

        Permission processManage = createPermissionIfNotExists("process:manage", "工序设置", Permission.PermissionType.MENU);
        Permission processAdd = createPermissionIfNotExists("process:add", "添加工序", Permission.PermissionType.BUTTON);
        Permission processEdit = createPermissionIfNotExists("process:edit", "编辑工序", Permission.PermissionType.BUTTON);
        Permission processDelete = createPermissionIfNotExists("process:delete", "删除工序", Permission.PermissionType.BUTTON);

        Permission processRouteManage = createPermissionIfNotExists("process_route:manage", "工序流程", Permission.PermissionType.MENU);
        Permission processRouteAdd = createPermissionIfNotExists("process_route:add", "添加工艺流程", Permission.PermissionType.BUTTON);
        Permission processRouteEdit = createPermissionIfNotExists("process_route:edit", "编辑工艺流程", Permission.PermissionType.BUTTON);
        Permission processRouteDelete = createPermissionIfNotExists("process_route:delete", "删除工艺流程", Permission.PermissionType.BUTTON);

        // 客户管理权限
        Permission customerManage = createPermissionIfNotExists("customer:manage", "客户管理", Permission.PermissionType.MENU);
        Permission customerAdd = createPermissionIfNotExists("customer:add", "添加客户", Permission.PermissionType.BUTTON);
        Permission customerEdit = createPermissionIfNotExists("customer:edit", "编辑客户", Permission.PermissionType.BUTTON);
        Permission customerDelete = createPermissionIfNotExists("customer:delete", "删除客户", Permission.PermissionType.BUTTON);
        Permission customerExport = createPermissionIfNotExists("customer:export", "导出客户", Permission.PermissionType.BUTTON);

        // 供应商管理权限
        Permission supplierManage = createPermissionIfNotExists("supplier:manage", "供应商管理", Permission.PermissionType.MENU);
        Permission supplierAdd = createPermissionIfNotExists("supplier:add", "添加供应商", Permission.PermissionType.BUTTON);
        Permission supplierEdit = createPermissionIfNotExists("supplier:edit", "编辑供应商", Permission.PermissionType.BUTTON);
        Permission supplierDelete = createPermissionIfNotExists("supplier:delete", "删除供应商", Permission.PermissionType.BUTTON);
        Permission supplierExport = createPermissionIfNotExists("supplier:export", "导出供应商", Permission.PermissionType.BUTTON);

        Role adminRole = createRoleIfNotExists("ADMIN", "系统管理员",
                userManage, userAdd, userEdit, userDelete, userResetPassword,
                roleManage, roleAdd, roleEdit, roleDelete,
                permissionManage, permissionAdd, permissionEdit, permissionDelete,
                passwordChange, mesView,
                uomManage, uomAdd, uomEdit, uomDelete,
                productCategoryManage, productCategoryAdd, productCategoryEdit, productCategoryDelete,
                productManage, productAdd, productEdit, productDelete,
                workshopManage, workshopAdd, workshopEdit, workshopDelete,
                workstationManage, workstationAdd, workstationEdit, workstationDelete,
                processManage, processAdd, processEdit, processDelete,
                processRouteManage, processRouteAdd, processRouteEdit, processRouteDelete,
                customerManage, customerAdd, customerEdit, customerDelete, customerExport,
                supplierManage, supplierAdd, supplierEdit, supplierDelete, supplierExport);

        Role userRole = createRoleIfNotExists("USER", "普通用户",
                passwordChange, mesView);

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(PasswordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(PasswordEncoder.encode("user123"));
            user.setRealName("普通用户");
            user.setRoles(new HashSet<>(Arrays.asList(userRole)));
            userRepository.save(user);
        }
    }

    private Permission createPermissionIfNotExists(String name, String description, Permission.PermissionType type) {
        return permissionRepository.findByName(name)
                .map(perm -> {
                    perm.setType(type);
                    return permissionRepository.save(perm);
                })
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setType(type);
                    return permissionRepository.save(permission);
                });
    }

    private Role createRoleIfNotExists(String name, String description, Permission... permissions) {
        return roleRepository.findByName(name)
                .map(role -> {
                    role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                    return roleRepository.save(role);
                })
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                    return roleRepository.save(role);
                });
    }
}
