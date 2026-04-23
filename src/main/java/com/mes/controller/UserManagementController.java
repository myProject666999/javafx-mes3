package com.mes.controller;

import com.mes.dto.UserDTO;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.service.AuthService;
import com.mes.service.RoleService;
import com.mes.service.UserService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserManagementController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthService authService;

    @FXML
    private TableView<UserDTO> userTable;

    @FXML
    private TableColumn<UserDTO, Boolean> actionColumn;

    @FXML
    private Button addUserBtn;

    public UserManagementController(UserService userService, RoleService roleService, AuthService authService) {
        this.userService = userService;
        this.roleService = roleService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        loadUsers();
        setupActionColumn();
        
        addUserBtn.setVisible(authService.hasPermission("user:add"));
        addUserBtn.setManaged(authService.hasPermission("user:add"));
    }

    private void loadUsers() {
        List<UserDTO> users = userService.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("user:edit");
        boolean canDelete = authService.hasPermission("user:delete");
        boolean canResetPassword = authService.hasPermission("user:resetPassword");
        
        Callback<TableColumn<UserDTO, Boolean>, TableCell<UserDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("编辑");
                    final Button resetBtn = new Button("重置密码");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        resetBtn.getStyleClass().addAll("action-button");
                        resetBtn.setStyle("-fx-background-color: #007acc; -fx-text-fill: white;");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        resetBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            showResetPasswordDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            UserDTO dto = getTableView().getItems().get(getIndex());
                            deleteUser(dto);
                        });
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            pane.getChildren().clear();
                            if (canEdit) pane.getChildren().add(editBtn);
                            if (canResetPassword) pane.getChildren().add(resetBtn);
                            if (canDelete) pane.getChildren().add(deleteBtn);
                            setGraphic(pane);
                        }
                    }
                };

        actionColumn.setCellFactory(cellFactory);
        actionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    @FXML
    public void showAddDialog() {
        Dialog<User> dialog = createUserDialog("添加用户", null);
        dialog.showAndWait().ifPresent(user -> {
            String password = user.getPassword() != null && !user.getPassword().isEmpty() 
                    ? user.getPassword() : "123456";
            userService.create(user, password, user.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList()));
            loadUsers();
        });
    }

    private void showEditDialog(UserDTO dto) {
        User user = userService.findById(dto.getId());
        Dialog<User> dialog = createUserDialog("编辑用户", user);
        dialog.showAndWait().ifPresent(updated -> {
            userService.update(updated, updated.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList()));
            loadUsers();
        });
    }

    private Dialog<User> createUserDialog(String title, User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("input-field");
        usernameField.setPromptText("用户名");

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("input-field");
        passwordField.setPromptText("密码");

        TextField realNameField = new TextField();
        realNameField.getStyleClass().add("input-field");
        realNameField.setPromptText("真实姓名");

        TextField emailField = new TextField();
        emailField.getStyleClass().add("input-field");
        emailField.setPromptText("邮箱");

        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("input-field");
        phoneField.setPromptText("电话");

        CheckBox enabledCheckBox = new CheckBox("启用");

        ListView<Role> roleListView = new ListView<>();
        roleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        roleListView.setItems(FXCollections.observableArrayList(roleService.findAll()));
        roleListView.setPrefHeight(150);
        roleListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setText(role.getName() + " - " + role.getDescription());
                    Platform.runLater(() -> checkBox.setSelected(getListView().getSelectionModel().getSelectedItems().contains(role)));
                    setGraphic(checkBox);
                }
            }
        });

        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setDisable(true);
            realNameField.setText(user.getRealName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            enabledCheckBox.setSelected(user.isEnabled());
            user.getRoles().forEach(role -> roleListView.getSelectionModel().select(role));
        }

        int row = 0;
        grid.add(new Label("用户名:"), 0, row);
        grid.add(usernameField, 1, row++);
        
        if (user == null) {
            grid.add(new Label("密码:"), 0, row);
            grid.add(passwordField, 1, row++);
        }
        
        grid.add(new Label("真实姓名:"), 0, row);
        grid.add(realNameField, 1, row++);
        grid.add(new Label("邮箱:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("电话:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("角色:"), 0, row);
        grid.add(roleListView, 1, row++);
        grid.add(enabledCheckBox, 1, row);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User result = user != null ? user : new User();
                result.setUsername(usernameField.getText());
                if (user == null) {
                    result.setPassword(passwordField.getText());
                }
                result.setRealName(realNameField.getText());
                result.setEmail(emailField.getText());
                result.setPhone(phoneField.getText());
                result.setEnabled(enabledCheckBox.isSelected());
                result.setRoles(new java.util.HashSet<>(roleListView.getSelectionModel().getSelectedItems()));
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void showResetPasswordDialog(UserDTO dto) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("重置密码");
        dialog.setHeaderText("重置用户 " + dto.getUsername() + " 的密码");
        dialog.setContentText("请输入新密码:");
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (!newPassword.isEmpty()) {
                userService.resetPassword(dto.getId(), newPassword);
                showAlert("成功", "密码已重置");
            }
        });
    }

    private void deleteUser(UserDTO dto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除用户 " + dto.getUsername() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userService.delete(dto.getId());
                loadUsers();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }
}
