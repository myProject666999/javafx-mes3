package com.mes.controller;

import com.mes.entity.Permission;
import com.mes.service.AuthService;
import com.mes.service.PermissionService;
import com.mes.service.RoleService;
import com.mes.service.UserService;
import com.mes.view.StageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MainController {

    private final AuthService authService;
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final StageManager stageManager;
    private final ApplicationContext applicationContext;

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox dashboardView;

    @FXML
    private Label userCountLabel;

    @FXML
    private Label roleCountLabel;

    @FXML
    private Label permissionCountLabel;

    @FXML
    private ListView<String> permissionListView;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Button userMenuBtn;

    @FXML
    private Button roleMenuBtn;

    @FXML
    private Button permissionMenuBtn;

    @FXML
    private Button uomMenuBtn;

    @FXML
    private Button productCategoryMenuBtn;

    @FXML
    private Button productManagementMenuBtn;

    @FXML
    private Button workshopMenuBtn;

    @FXML
    private Button workstationMenuBtn;

    @FXML
    private Button processMenuBtn;

    @FXML
    private Button processRouteMenuBtn;

    @FXML
    private Label systemMenuLabel;

    @FXML
    private Label processMenuLabel;

    @FXML
    private Label masterDataMenuLabel;

    @FXML
    private Label productMenuLabel;

    @FXML
    private Label workshopMenuLabel;

    @FXML
    private Label personalMenuLabel;

    public MainController(AuthService authService, UserService userService,
                          RoleService roleService, PermissionService permissionService,
                          StageManager stageManager, ApplicationContext applicationContext) {
        this.authService = authService;
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.stageManager = stageManager;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        String welcome = "欢迎, " + authService.getCurrentUser().getRealName();
        userInfoLabel.setText(welcome);

        updateDashboard();

        boolean hasUserPerm = authService.hasPermission("user:manage");
        boolean hasRolePerm = authService.hasPermission("role:manage");
        boolean hasPermPerm = authService.hasPermission("permission:manage");
        boolean hasUomPerm = authService.hasPermission("uom:manage");
        boolean hasProductCategoryPerm = authService.hasPermission("product_category:manage");
        boolean hasProductManagePerm = authService.hasPermission("product:manage");
        boolean hasWorkshopPerm = authService.hasPermission("workshop:manage");
        boolean hasWorkstationPerm = authService.hasPermission("workstation:manage");
        boolean hasProcessPerm = authService.hasPermission("process:manage");
        boolean hasProcessRoutePerm = authService.hasPermission("process_route:manage");

        userMenuBtn.setVisible(hasUserPerm);
        userMenuBtn.setManaged(hasUserPerm);
        roleMenuBtn.setVisible(hasRolePerm);
        roleMenuBtn.setManaged(hasRolePerm);
        permissionMenuBtn.setVisible(hasPermPerm);
        permissionMenuBtn.setManaged(hasPermPerm);
        uomMenuBtn.setVisible(hasUomPerm);
        uomMenuBtn.setManaged(hasUomPerm);
        productCategoryMenuBtn.setVisible(hasProductCategoryPerm);
        productCategoryMenuBtn.setManaged(hasProductCategoryPerm);
        productManagementMenuBtn.setVisible(hasProductManagePerm);
        productManagementMenuBtn.setManaged(hasProductManagePerm);
        workshopMenuBtn.setVisible(hasWorkshopPerm);
        workshopMenuBtn.setManaged(hasWorkshopPerm);
        workstationMenuBtn.setVisible(hasWorkstationPerm);
        workstationMenuBtn.setManaged(hasWorkstationPerm);
        processMenuBtn.setVisible(hasProcessPerm);
        processMenuBtn.setManaged(hasProcessPerm);
        processRouteMenuBtn.setVisible(hasProcessRoutePerm);
        processRouteMenuBtn.setManaged(hasProcessRoutePerm);

        boolean hasSystemMenu = hasUserPerm || hasRolePerm || hasPermPerm;
        systemMenuLabel.setVisible(hasSystemMenu);
        systemMenuLabel.setManaged(hasSystemMenu);

        masterDataMenuLabel.setVisible(hasUomPerm);
        masterDataMenuLabel.setManaged(hasUomPerm);

        boolean hasProductMenu = hasProductCategoryPerm || hasProductManagePerm;
        productMenuLabel.setVisible(hasProductMenu);
        productMenuLabel.setManaged(hasProductMenu);

        boolean hasWorkshopMenu = hasWorkshopPerm || hasWorkstationPerm;
        workshopMenuLabel.setVisible(hasWorkshopMenu);
        workshopMenuLabel.setManaged(hasWorkshopMenu);

        boolean hasProcessMenu = hasProcessPerm || hasProcessRoutePerm;
        processMenuLabel.setVisible(hasProcessMenu);
        processMenuLabel.setManaged(hasProcessMenu);
    }

    private void updateDashboard() {
        userCountLabel.setText(String.valueOf(userService.findAll().size()));
        roleCountLabel.setText(String.valueOf(roleService.findAll().size()));
        permissionCountLabel.setText(String.valueOf(permissionService.findAll().size()));

        var permissions = authService.getCurrentUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getDescription)
                .distinct()
                .collect(Collectors.toList());
        permissionListView.setItems(FXCollections.observableArrayList(permissions));
    }

    private void setActiveButton(Button activeButton) {
        // 使用递归查找所有带有 sidebar-button 样式的按钮
        findAllButtonsWithStyle(activeButton.getScene().getRoot(), "sidebar-button")
                .forEach(btn -> btn.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }

    private Set<Button> findAllButtonsWithStyle(javafx.scene.Parent parent, String styleClass) {
        Set<Button> buttons = new HashSet<>();
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button && node.getStyleClass().contains(styleClass)) {
                buttons.add((Button) node);
            }
            if (node instanceof javafx.scene.Parent) {
                buttons.addAll(findAllButtonsWithStyle((javafx.scene.Parent) node, styleClass));
            }
        }
        return buttons;
    }

    @FXML
    public void showDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(dashboardView);
        updateDashboard();
        Button dashboardBtn = (Button) contentPane.getScene().lookup(".sidebar-button");
        if (dashboardBtn != null) {
            setActiveButton(dashboardBtn);
        }
    }

    @FXML
    public void showUserManagement() {
        loadView("/fxml/user-management.fxml");
        setActiveButton(userMenuBtn);
    }

    @FXML
    public void showRoleManagement() {
        loadView("/fxml/role-management.fxml");
        setActiveButton(roleMenuBtn);
    }

    @FXML
    public void showPermissionManagement() {
        loadView("/fxml/permission-management.fxml");
        setActiveButton(permissionMenuBtn);
    }

    @FXML
    public void showUnitOfMeasure() {
        loadView("/fxml/unit-of-measure.fxml");
        setActiveButton(uomMenuBtn);
    }

    @FXML
    public void showProductCategory() {
        loadView("/fxml/product-category.fxml");
        setActiveButton(productCategoryMenuBtn);
    }

    @FXML
    public void showProductManagement() {
        loadView("/fxml/product-management.fxml");
        setActiveButton(productManagementMenuBtn);
    }

    @FXML
    public void showWorkshop() {
        loadView("/fxml/workshop.fxml");
        setActiveButton(workshopMenuBtn);
    }

    @FXML
    public void showWorkstation() {
        loadView("/fxml/workstation.fxml");
        setActiveButton(workstationMenuBtn);
    }

    @FXML
    public void showProcess() {
        loadView("/fxml/process.fxml");
        setActiveButton(processMenuBtn);
    }

    @FXML
    public void showProcessRoute() {
        loadView("/fxml/process-route.fxml");
        setActiveButton(processRouteMenuBtn);
    }

    @FXML
    public void showChangePassword() {
        loadView("/fxml/change-password.fxml");
        findAllButtonsWithStyle(contentPane.getScene().getRoot(), "sidebar-button")
                .forEach(btn -> {
                    if ("🔐  修改密码".equals(btn.getText())) {
                        setActiveButton(btn);
                    }
                });
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("确定要退出登录吗？");
            alert.getDialogPane().getStylesheets().add("/css/style.css");
            
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    authService.logout();
                    stageManager.showLogin();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            authService.logout();
            stageManager.showLogin();
        }
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

    @FXML
    public void maximizeWindow() {
        stageManager.maximize();
    }
}
