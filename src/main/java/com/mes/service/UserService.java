package com.mes.service;

import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.repository.RoleRepository;
import com.mes.repository.UserRepository;
import com.mes.util.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public User create(User user, String rawPassword, List<Long> roleIds) {
        user.setPassword(PasswordEncoder.encode(rawPassword));
        Set<Role> roles = new HashSet<>();
        if (roleIds != null) {
            roleRepository.findAllById(roleIds).forEach(roles::add);
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public User update(User user, List<Long> roleIds) {
        User existing = userRepository.findById(user.getId()).orElseThrow();
        existing.setRealName(user.getRealName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setEnabled(user.isEnabled());

        if (roleIds != null) {
            Set<Role> roles = new HashSet<>();
            roleRepository.findAllById(roleIds).forEach(roles::add);
            existing.setRoles(roles);
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPassword(PasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
