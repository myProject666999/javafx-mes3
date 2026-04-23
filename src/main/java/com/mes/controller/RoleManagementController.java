package com.mes.controller;

import com.mes.dto.RoleDTO;
import com.mes.entity.Permission;
import com.mes.entity.Role;
import com.mes.service.AuthService;
import com.mes.service.PermissionService;
import com.mes.service.RoleService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleManagementController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AuthService authService;

    @FXML
    private TableView<RoleDTO> roleTable;

    @FXML
    private TableColumn<RoleDTO, Boolean> roleActionColumn;

    @FXML
    private Button addRoleBtn;

    public RoleManagementController(RoleService roleService, PermissionService permissionService, AuthService authService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        loadRoles();
        setupActionColumn();
        
        addRoleBtn.setVisible(authService.hasPermission("role:add"));
        addRoleBtn.setManaged(authService.hasPermission("role:add"));
    }

    private void loadRoles() {
        var roles = roleService.findAll().stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());
        roleTable.setItems(FXCollections.observableArrayList(roles));
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("role:edit");
        boolean canDelete = authService.hasPermission("role:delete");
        
        Callback<TableColumn<RoleDTO, Boolean>, TableCell<RoleDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("编辑");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            RoleDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            RoleDTO dto = getTableView().getItems().get(getIndex());
                            deleteRole(dto);
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
                            if (canDelete) pane.getChildren().add(deleteBtn);
                            setGraphic(pane);
                        }
                    }
                };

        roleActionColumn.setCellFactory(cellFactory);
        roleActionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    @FXML
    public void showAddDialog() {
        Set<Permission> selectedPermissions = new HashSet<>();
        Dialog<Role> dialog = createRoleDialog("添加角色", null, selectedPermissions);
        dialog.showAndWait().ifPresent(role -> {
            role.setPermissions(selectedPermissions);
            roleService.create(role, selectedPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList()));
            loadRoles();
        });
    }

    private void showEditDialog(RoleDTO dto) {
        Role role = roleService.findById(dto.getId());
        Set<Permission> selectedPermissions = new HashSet<>(role.getPermissions());
        Dialog<Role> dialog = createRoleDialog("编辑角色", role, selectedPermissions);
        dialog.showAndWait().ifPresent(updated -> {
            updated.setPermissions(selectedPermissions);
            roleService.update(updated, selectedPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList()));
            loadRoles();
        });
    }

    private Dialog<Role> createRoleDialog(String title, Role role, Set<Permission> selectedPermissions) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.getStyleClass().add("input-field");

        TextField descField = new TextField();
        descField.getStyleClass().add("input-field");

        ListView<Permission> permListView = new ListView<>();
        permListView.setItems(FXCollections.observableArrayList(permissionService.findAll()));
        permListView.setPrefHeight(200);
        permListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            
            @Override
            protected void updateItem(Permission perm, boolean empty) {
                super.updateItem(perm, empty);
                if (empty || perm == null) {
                    setGraphic(null);
                    setText(null);
                    checkBox.setOnAction(null);
                } else {
                    checkBox.setText(perm.getName() + " - " + perm.getDescription());
                    checkBox.setSelected(selectedPermissions.contains(perm));
                    checkBox.setOnAction(event -> {
                        if (checkBox.isSelected()) {
                            selectedPermissions.add(perm);
                        } else {
                            selectedPermissions.remove(perm);
                        }
                    });
                    setGraphic(checkBox);
                }
            }
        });

        if (role != null) {
            nameField.setText(role.getName());
            descField.setText(role.getDescription());
        }

        grid.add(new Label("角色名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("权限:"), 0, 2);
        grid.add(permListView, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Role result = role != null ? role : new Role();
                result.setName(nameField.getText());
                result.setDescription(descField.getText());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void deleteRole(RoleDTO dto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除角色 " + dto.getName() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                roleService.delete(dto.getId());
                loadRoles();
            }
        });
    }
}
