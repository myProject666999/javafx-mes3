package com.mes.service;

import com.mes.config.UserContext;
import com.mes.entity.User;
import com.mes.repository.UserRepository;
import com.mes.util.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.isEnabled())
                .filter(user -> PasswordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    UserContext.setCurrentUser(user);
                    return true;
                })
                .orElse(false);
    }

    public void logout() {
        UserContext.clear();
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) return false;

        if (PasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            currentUser.setPassword(PasswordEncoder.encode(newPassword));
            userRepository.save(currentUser);
            UserContext.setCurrentUser(currentUser);
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return UserContext.getCurrentUser();
    }

    public boolean hasPermission(String permissionName) {
        return UserContext.hasPermission(permissionName);
    }
}
