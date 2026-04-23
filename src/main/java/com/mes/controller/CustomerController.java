package com.mes.controller;

import com.mes.dto.CustomerDTO;
import com.mes.service.AuthService;
import com.mes.service.CustomerService;
import com.mes.util.ExcelExportUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    @FXML
    private TableView<CustomerDTO> customerTable;

    @FXML
    private TableColumn<CustomerDTO, Boolean> selectColumn;

    @FXML
    private TableColumn<CustomerDTO, String> codeColumn;

    @FXML
    private TableColumn<CustomerDTO, String> nameColumn;

    @FXML
    private TableColumn<CustomerDTO, String> contactColumn;

    @FXML
    private TableColumn<CustomerDTO, String> phoneColumn;

    @FXML
    private TableColumn<CustomerDTO, String> emailColumn;

    @FXML
    private TableColumn<CustomerDTO, String> addressColumn;

    @FXML
    private TableColumn<CustomerDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<CustomerDTO, Boolean> actionColumn;

    @FXML
    private TextField codeSearchField;

    @FXML
    private TextField nameSearchField;

    @FXML
    private ComboBox<String> enabledSearchCombo;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    private ObservableList<CustomerDTO> customerList = FXCollections.observableArrayList();
    private ObservableList<CustomerDTO> selectedCustomers = FXCollections.observableArrayList();

    public CustomerController(CustomerService customerService, AuthService authService) {
        this.customerService = customerService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupCustomerTable();
        setupEnabledComboBox();
        loadCustomers();
        setupPermissions();
    }

    private void setupEnabledComboBox() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupCustomerTable() {
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(param -> {
            CustomerDTO customer = param.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(selectedCustomers.contains(customer));
            property.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedCustomers.add(customer);
                } else {
                    selectedCustomers.remove(customer);
                }
            });
            return property;
        });

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        enabledColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isEnabled()));
        enabledColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "启用" : "禁用");
                }
            }
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("customer:edit");
        boolean canDelete = authService.hasPermission("customer:delete");

        Callback<TableColumn<CustomerDTO, Boolean>, TableCell<CustomerDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            CustomerDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            CustomerDTO dto = getTableView().getItems().get(getIndex());
                            deleteCustomer(dto);
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

        actionColumn.setCellFactory(cellFactory);
        actionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(true));
    }

    private void setupPermissions() {
        boolean canAdd = authService.hasPermission("customer:add");
        boolean canEdit = authService.hasPermission("customer:edit");
        boolean canDelete = authService.hasPermission("customer:delete");
        boolean canExport = authService.hasPermission("customer:export");

        addButton.setVisible(canAdd);
        addButton.setManaged(canAdd);
        editButton.setVisible(canEdit);
        editButton.setManaged(canEdit);
        deleteButton.setVisible(canDelete);
        deleteButton.setManaged(canDelete);
        exportButton.setVisible(canExport);
        exportButton.setManaged(canExport);

        if (!canEdit && !canDelete) {
            actionColumn.setVisible(false);
        }
    }

    private void loadCustomers() {
        List<CustomerDTO> customers = customerService.findAll();
        customerList.setAll(customers);
        customerTable.setItems(customerList);
        selectedCustomers.clear();
    }

    @FXML
    public void handleSearch() {
        String code = codeSearchField.getText();
        String name = nameSearchField.getText();
        String enabledStr = enabledSearchCombo.getValue();
        Boolean enabled = null;
        if ("启用".equals(enabledStr)) {
            enabled = true;
        } else if ("禁用".equals(enabledStr)) {
            enabled = false;
        }

        List<CustomerDTO> results = customerService.search(code, name, enabled);
        customerList.setAll(results);
        customerTable.setItems(customerList);
        selectedCustomers.clear();
    }

    @FXML
    public void handleReset() {
        codeSearchField.clear();
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadCustomers();
    }

    @FXML
    public void showAddDialog() {
        Dialog<CustomerDTO> dialog = new Dialog<>();
        dialog.setTitle("新增客户");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(80);
        labelCol.setPrefWidth(80);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(200);
        fieldCol.setPrefWidth(300);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField codeField = new TextField();
        codeField.setPromptText("请输入客户编码");

        Button autoGenBtn = new Button("自动生成");
        autoGenBtn.setOnAction(e -> {
            String code = customerService.generateCode();
            codeField.setText(code);
        });

        HBox codeBox = new HBox(10, codeField, autoGenBtn);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入客户名称");

        TextField contactField = new TextField();
        contactField.setPromptText("请输入联系人");

        TextField phoneField = new TextField();
        phoneField.setPromptText("请输入联系电话");

        TextField emailField = new TextField();
        emailField.setPromptText("请输入邮箱");

        TextField addressField = new TextField();
        addressField.setPromptText("请输入地址");

        TextField taxNumberField = new TextField();
        taxNumberField.setPromptText("请输入税号");

        TextField bankAccountField = new TextField();
        bankAccountField.setPromptText("请输入银行账号");

        TextField bankNameField = new TextField();
        bankNameField.setPromptText("请输入开户银行");

        TextArea remarkField = new TextArea();
        remarkField.setPromptText("请输入备注");
        remarkField.setPrefRowCount(2);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("客户编码:"), 0, 0);
        grid.add(codeBox, 1, 0);
        grid.add(new Label("客户名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("联系人:"), 0, 2);
        grid.add(contactField, 1, 2);
        grid.add(new Label("联系电话:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("邮箱:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("地址:"), 0, 5);
        grid.add(addressField, 1, 5);
        grid.add(new Label("税号:"), 0, 6);
        grid.add(taxNumberField, 1, 6);
        grid.add(new Label("银行账号:"), 0, 7);
        grid.add(bankAccountField, 1, 7);
        grid.add(new Label("开户银行:"), 0, 8);
        grid.add(bankNameField, 1, 8);
        grid.add(new Label("备注:"), 0, 9);
        grid.add(remarkField, 1, 9);
        grid.add(enabledCheck, 1, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
                    showAlert("错误", "客户编码不能为空");
                    return null;
                }
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "客户名称不能为空");
                    return null;
                }

                CustomerDTO dto = new CustomerDTO();
                dto.setCode(codeField.getText().trim());
                dto.setName(nameField.getText().trim());
                dto.setContact(contactField.getText());
                dto.setPhone(phoneField.getText());
                dto.setEmail(emailField.getText());
                dto.setAddress(addressField.getText());
                dto.setTaxNumber(taxNumberField.getText());
                dto.setBankAccount(bankAccountField.getText());
                dto.setBankName(bankNameField.getText());
                dto.setRemark(remarkField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<CustomerDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                customerService.save(dto);
                loadCustomers();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(CustomerDTO customer) {
        Dialog<CustomerDTO> dialog = new Dialog<>();
        dialog.setTitle("修改客户");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(80);
        labelCol.setPrefWidth(80);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(200);
        fieldCol.setPrefWidth(300);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField codeField = new TextField(customer.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(customer.getName());

        TextField contactField = new TextField(customer.getContact() != null ? customer.getContact() : "");
        contactField.setPromptText("请输入联系人");

        TextField phoneField = new TextField(customer.getPhone() != null ? customer.getPhone() : "");
        phoneField.setPromptText("请输入联系电话");

        TextField emailField = new TextField(customer.getEmail() != null ? customer.getEmail() : "");
        emailField.setPromptText("请输入邮箱");

        TextField addressField = new TextField(customer.getAddress() != null ? customer.getAddress() : "");
        addressField.setPromptText("请输入地址");

        TextField taxNumberField = new TextField(customer.getTaxNumber() != null ? customer.getTaxNumber() : "");
        taxNumberField.setPromptText("请输入税号");

        TextField bankAccountField = new TextField(customer.getBankAccount() != null ? customer.getBankAccount() : "");
        bankAccountField.setPromptText("请输入银行账号");

        TextField bankNameField = new TextField(customer.getBankName() != null ? customer.getBankName() : "");
        bankNameField.setPromptText("请输入开户银行");

        TextArea remarkField = new TextArea(customer.getRemark() != null ? customer.getRemark() : "");
        remarkField.setPromptText("请输入备注");
        remarkField.setPrefRowCount(2);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(customer.isEnabled());

        grid.add(new Label("客户编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("客户名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("联系人:"), 0, 2);
        grid.add(contactField, 1, 2);
        grid.add(new Label("联系电话:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("邮箱:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("地址:"), 0, 5);
        grid.add(addressField, 1, 5);
        grid.add(new Label("税号:"), 0, 6);
        grid.add(taxNumberField, 1, 6);
        grid.add(new Label("银行账号:"), 0, 7);
        grid.add(bankAccountField, 1, 7);
        grid.add(new Label("开户银行:"), 0, 8);
        grid.add(bankNameField, 1, 8);
        grid.add(new Label("备注:"), 0, 9);
        grid.add(remarkField, 1, 9);
        grid.add(enabledCheck, 1, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "客户名称不能为空");
                    return null;
                }

                CustomerDTO dto = new CustomerDTO();
                dto.setId(customer.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setContact(contactField.getText());
                dto.setPhone(phoneField.getText());
                dto.setEmail(emailField.getText());
                dto.setAddress(addressField.getText());
                dto.setTaxNumber(taxNumberField.getText());
                dto.setBankAccount(bankAccountField.getText());
                dto.setBankName(bankNameField.getText());
                dto.setRemark(remarkField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<CustomerDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                customerService.save(dto);
                loadCustomers();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteCustomer(CustomerDTO customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除客户 \"" + customer.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteById(customer.getId());
                loadCustomers();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        CustomerDTO selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的客户");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        if (selectedCustomers.isEmpty()) {
            showAlert("提示", "请先选择要删除的客户");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selectedCustomers.size() + " 个客户吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (CustomerDTO customer : selectedCustomers) {
                try {
                    customerService.deleteById(customer.getId());
                } catch (Exception e) {
                    showAlert("错误", "删除客户 " + customer.getName() + " 失败: " + e.getMessage());
                }
            }
            loadCustomers();
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出客户数据");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("客户数据_" + timestamp + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));

        File file = fileChooser.showSaveDialog(customerTable.getScene().getWindow());
        if (file != null) {
            try {
                List<String> headers = Arrays.asList(
                        "客户编码", "客户名称", "联系人", "联系电话", "邮箱",
                        "地址", "税号", "银行账号", "开户银行", "备注", "状态"
                );

                List<CustomerDTO> dataToExport = customerList;

                ExcelExportUtil.exportToExcel(
                        file.getAbsolutePath(),
                        "客户数据",
                        headers,
                        dataToExport,
                        customer -> Arrays.asList(
                                customer.getCode(),
                                customer.getName(),
                                customer.getContact() != null ? customer.getContact() : "",
                                customer.getPhone() != null ? customer.getPhone() : "",
                                customer.getEmail() != null ? customer.getEmail() : "",
                                customer.getAddress() != null ? customer.getAddress() : "",
                                customer.getTaxNumber() != null ? customer.getTaxNumber() : "",
                                customer.getBankAccount() != null ? customer.getBankAccount() : "",
                                customer.getBankName() != null ? customer.getBankName() : "",
                                customer.getRemark() != null ? customer.getRemark() : "",
                                customer.isEnabled() ? "启用" : "禁用"
                        )
                );

                showAlert("成功", "客户数据已成功导出到：\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("错误", "导出失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
