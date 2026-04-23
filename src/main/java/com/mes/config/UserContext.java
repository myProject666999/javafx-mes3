package com.mes.config;

import com.mes.entity.User;

public class UserContext {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean hasPermission(String permissionName) {
        if (currentUser == null) return false;
        return currentUser.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}
