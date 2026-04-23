package com.mes.controller;

import com.mes.dto.SupplierDTO;
import com.mes.service.AuthService;
import com.mes.service.SupplierService;
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
public class SupplierController {

    private final SupplierService supplierService;
    private final AuthService authService;

    @FXML
    private TableView<SupplierDTO> supplierTable;

    @FXML
    private TableColumn<SupplierDTO, Boolean> selectColumn;

    @FXML
    private TableColumn<SupplierDTO, String> codeColumn;

    @FXML
    private TableColumn<SupplierDTO, String> nameColumn;

    @FXML
    private TableColumn<SupplierDTO, String> contactColumn;

    @FXML
    private TableColumn<SupplierDTO, String> phoneColumn;

    @FXML
    private TableColumn<SupplierDTO, String> emailColumn;

    @FXML
    private TableColumn<SupplierDTO, String> addressColumn;

    @FXML
    private TableColumn<SupplierDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<SupplierDTO, Boolean> actionColumn;

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

    private ObservableList<SupplierDTO> supplierList = FXCollections.observableArrayList();
    private ObservableList<SupplierDTO> selectedSuppliers = FXCollections.observableArrayList();

    public SupplierController(SupplierService supplierService, AuthService authService) {
        this.supplierService = supplierService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupSupplierTable();
        setupEnabledComboBox();
        loadSuppliers();
        setupPermissions();
    }

    private void setupEnabledComboBox() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupSupplierTable() {
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(param -> {
            SupplierDTO supplier = param.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(selectedSuppliers.contains(supplier));
            property.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedSuppliers.add(supplier);
                } else {
                    selectedSuppliers.remove(supplier);
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
        boolean canEdit = authService.hasPermission("supplier:edit");
        boolean canDelete = authService.hasPermission("supplier:delete");

        Callback<TableColumn<SupplierDTO, Boolean>, TableCell<SupplierDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            SupplierDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            SupplierDTO dto = getTableView().getItems().get(getIndex());
                            deleteSupplier(dto);
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
        boolean canAdd = authService.hasPermission("supplier:add");
        boolean canEdit = authService.hasPermission("supplier:edit");
        boolean canDelete = authService.hasPermission("supplier:delete");
        boolean canExport = authService.hasPermission("supplier:export");

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

    private void loadSuppliers() {
        List<SupplierDTO> suppliers = supplierService.findAll();
        supplierList.setAll(suppliers);
        supplierTable.setItems(supplierList);
        selectedSuppliers.clear();
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

        List<SupplierDTO> results = supplierService.search(code, name, enabled);
        supplierList.setAll(results);
        supplierTable.setItems(supplierList);
        selectedSuppliers.clear();
    }

    @FXML
    public void handleReset() {
        codeSearchField.clear();
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadSuppliers();
    }

    @FXML
    public void showAddDialog() {
        Dialog<SupplierDTO> dialog = new Dialog<>();
        dialog.setTitle("新增供应商");
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
        codeField.setPromptText("请输入供应商编码");

        Button autoGenBtn = new Button("自动生成");
        autoGenBtn.setOnAction(e -> {
            String code = supplierService.generateCode();
            codeField.setText(code);
        });

        HBox codeBox = new HBox(10, codeField, autoGenBtn);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入供应商名称");

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

        grid.add(new Label("供应商编码:"), 0, 0);
        grid.add(codeBox, 1, 0);
        grid.add(new Label("供应商名称:"), 0, 1);
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
                    showAlert("错误", "供应商编码不能为空");
                    return null;
                }
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "供应商名称不能为空");
                    return null;
                }

                SupplierDTO dto = new SupplierDTO();
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

        Optional<SupplierDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                supplierService.save(dto);
                loadSuppliers();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(SupplierDTO supplier) {
        Dialog<SupplierDTO> dialog = new Dialog<>();
        dialog.setTitle("修改供应商");
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

        TextField codeField = new TextField(supplier.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(supplier.getName());

        TextField contactField = new TextField(supplier.getContact() != null ? supplier.getContact() : "");
        contactField.setPromptText("请输入联系人");

        TextField phoneField = new TextField(supplier.getPhone() != null ? supplier.getPhone() : "");
        phoneField.setPromptText("请输入联系电话");

        TextField emailField = new TextField(supplier.getEmail() != null ? supplier.getEmail() : "");
        emailField.setPromptText("请输入邮箱");

        TextField addressField = new TextField(supplier.getAddress() != null ? supplier.getAddress() : "");
        addressField.setPromptText("请输入地址");

        TextField taxNumberField = new TextField(supplier.getTaxNumber() != null ? supplier.getTaxNumber() : "");
        taxNumberField.setPromptText("请输入税号");

        TextField bankAccountField = new TextField(supplier.getBankAccount() != null ? supplier.getBankAccount() : "");
        bankAccountField.setPromptText("请输入银行账号");

        TextField bankNameField = new TextField(supplier.getBankName() != null ? supplier.getBankName() : "");
        bankNameField.setPromptText("请输入开户银行");

        TextArea remarkField = new TextArea(supplier.getRemark() != null ? supplier.getRemark() : "");
        remarkField.setPromptText("请输入备注");
        remarkField.setPrefRowCount(2);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(supplier.isEnabled());

        grid.add(new Label("供应商编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("供应商名称:"), 0, 1);
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
                    showAlert("错误", "供应商名称不能为空");
                    return null;
                }

                SupplierDTO dto = new SupplierDTO();
                dto.setId(supplier.getId());
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

        Optional<SupplierDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                supplierService.save(dto);
                loadSuppliers();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteSupplier(SupplierDTO supplier) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除供应商 \"" + supplier.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                supplierService.deleteById(supplier.getId());
                loadSuppliers();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        SupplierDTO selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的供应商");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        if (selectedSuppliers.isEmpty()) {
            showAlert("提示", "请先选择要删除的供应商");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selectedSuppliers.size() + " 个供应商吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (SupplierDTO supplier : selectedSuppliers) {
                try {
                    supplierService.deleteById(supplier.getId());
                } catch (Exception e) {
                    showAlert("错误", "删除供应商 " + supplier.getName() + " 失败: " + e.getMessage());
                }
            }
            loadSuppliers();
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出供应商数据");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("供应商数据_" + timestamp + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));

        File file = fileChooser.showSaveDialog(supplierTable.getScene().getWindow());
        if (file != null) {
            try {
                List<String> headers = Arrays.asList(
                        "供应商编码", "供应商名称", "联系人", "联系电话", "邮箱",
                        "地址", "税号", "银行账号", "开户银行", "备注", "状态"
                );

                List<SupplierDTO> dataToExport = supplierList;

                ExcelExportUtil.exportToExcel(
                        file.getAbsolutePath(),
                        "供应商数据",
                        headers,
                        dataToExport,
                        supplier -> Arrays.asList(
                                supplier.getCode(),
                                supplier.getName(),
                                supplier.getContact() != null ? supplier.getContact() : "",
                                supplier.getPhone() != null ? supplier.getPhone() : "",
                                supplier.getEmail() != null ? supplier.getEmail() : "",
                                supplier.getAddress() != null ? supplier.getAddress() : "",
                                supplier.getTaxNumber() != null ? supplier.getTaxNumber() : "",
                                supplier.getBankAccount() != null ? supplier.getBankAccount() : "",
                                supplier.getBankName() != null ? supplier.getBankName() : "",
                                supplier.getRemark() != null ? supplier.getRemark() : "",
                                supplier.isEnabled() ? "启用" : "禁用"
                        )
                );

                showAlert("成功", "供应商数据已成功导出到：\n" + file.getAbsolutePath());
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
