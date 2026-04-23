package com.mes.controller;

import com.mes.service.AuthService;
import com.mes.view.StageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private final AuthService authService;
    private final StageManager stageManager;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    public LoginController(AuthService authService, StageManager stageManager) {
        this.authService = authService;
        this.stageManager = stageManager;
    }

    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("请输入用户名和密码");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setText("");

        new Thread(() -> {
            boolean success = authService.login(username, password);
            Platform.runLater(() -> {
                if (success) {
                    stageManager.showMain();
                } else {
                    errorLabel.setText("用户名或密码错误");
                    loginButton.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    public void closeWindow() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void minimizeWindow() {
        stageManager.minimize();
    }
}
