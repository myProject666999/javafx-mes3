package com.mes.controller;

import com.mes.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordController {

    private final AuthService authService;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    public ChangePasswordController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("请填写所有字段");
            messageLabel.setStyle("-fx-text-fill: #f14c4c;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("两次输入的新密码不一致");
            messageLabel.setStyle("-fx-text-fill: #f14c4c;");
            return;
        }

        if (newPassword.length() < 6) {
            messageLabel.setText("新密码长度至少6位");
            messageLabel.setStyle("-fx-text-fill: #f14c4c;");
            return;
        }

        boolean success = authService.changePassword(oldPassword, newPassword);
        if (success) {
            messageLabel.setText("密码修改成功");
            messageLabel.setStyle("-fx-text-fill: #28a745;");
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            messageLabel.setText("原密码错误");
            messageLabel.setStyle("-fx-text-fill: #f14c4c;");
        }
    }
}
