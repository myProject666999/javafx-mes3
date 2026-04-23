package com.mes.controller;

import com.mes.dto.UnitOfMeasureDTO;
import com.mes.entity.UnitOfMeasure;
import com.mes.service.AuthService;
import com.mes.service.UnitOfMeasureService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;
    private final AuthService authService;

    @FXML
    private TableView<UnitOfMeasureDTO> unitTable;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Long> idColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, String> codeColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, String> nameColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, String> descriptionColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Boolean> baseUnitColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<UnitOfMeasureDTO, Boolean> actionColumn;

    @FXML
    private TextField codeSearchField;

    @FXML
    private TextField nameSearchField;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    private ObservableList<UnitOfMeasureDTO> unitList = FXCollections.observableArrayList();

    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService, AuthService authService) {
        this.unitOfMeasureService = unitOfMeasureService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        loadUnits();
        setupPermissions();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        baseUnitColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isBaseUnit()));
        baseUnitColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "是" : "否");
                }
            }
        });

        enabledColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isEnabled()));
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
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("uom:edit");
        boolean canDelete = authService.hasPermission("uom:delete");
        
        Callback<TableColumn<UnitOfMeasureDTO, Boolean>, TableCell<UnitOfMeasureDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            UnitOfMeasureDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            UnitOfMeasureDTO dto = getTableView().getItems().get(getIndex());
                            deleteUnit(dto);
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
        actionColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(true));
    }

    private void setupPermissions() {
        boolean canAdd = authService.hasPermission("uom:add");
        boolean canEdit = authService.hasPermission("uom:edit");
        boolean canDelete = authService.hasPermission("uom:delete");
        boolean canExport = authService.hasPermission("uom:manage");

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

    private void loadUnits() {
        List<UnitOfMeasureDTO> units = unitOfMeasureService.findAll().stream()
                .map(UnitOfMeasureDTO::fromEntity)
                .collect(Collectors.toList());
        unitList.setAll(units);
        unitTable.setItems(unitList);
    }

    @FXML
    public void handleSearch() {
        String code = codeSearchField.getText();
        String name = nameSearchField.getText();
        
        List<UnitOfMeasureDTO> units = unitOfMeasureService.search(code, name).stream()
                .map(UnitOfMeasureDTO::fromEntity)
                .collect(Collectors.toList());
        unitList.setAll(units);
        unitTable.setItems(unitList);
    }

    @FXML
    public void handleReset() {
        codeSearchField.clear();
        nameSearchField.clear();
        loadUnits();
    }

    @FXML
    public void showAddDialog() {
        Dialog<UnitOfMeasure> dialog = createUnitDialog("新增计量单位", null);
        dialog.showAndWait().ifPresent(unit -> {
            try {
                unitOfMeasureService.create(unit);
                loadUnits();
                showSuccessAlert("新增成功", "计量单位新增成功");
            } catch (Exception e) {
                showErrorAlert("新增失败", e.getMessage());
            }
        });
    }

    @FXML
    public void handleBatchEdit() {
        ObservableList<UnitOfMeasureDTO> selectedItems = unitTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showWarningAlert("提示", "请先选择要修改的数据");
            return;
        }
        if (selectedItems.size() > 1) {
            showWarningAlert("提示", "一次只能修改一条数据");
            return;
        }
        showEditDialog(selectedItems.get(0));
    }

    private void showEditDialog(UnitOfMeasureDTO dto) {
        UnitOfMeasure unit = unitOfMeasureService.findById(dto.getId());
        if (unit == null) {
            showErrorAlert("错误", "数据不存在");
            return;
        }
        Dialog<UnitOfMeasure> dialog = createUnitDialog("修改计量单位", unit);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                unitOfMeasureService.update(updated);
                loadUnits();
                showSuccessAlert("修改成功", "计量单位修改成功");
            } catch (Exception e) {
                showErrorAlert("修改失败", e.getMessage());
            }
        });
    }

    @FXML
    public void handleBatchDelete() {
        ObservableList<UnitOfMeasureDTO> selectedItems = unitTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showWarningAlert("提示", "请先选择要删除的数据");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除选中的 " + selectedItems.size() + " 条数据?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    List<Long> ids = selectedItems.stream()
                            .map(UnitOfMeasureDTO::getId)
                            .collect(Collectors.toList());
                    unitOfMeasureService.deleteByIds(ids);
                    loadUnits();
                    showSuccessAlert("删除成功", "计量单位删除成功");
                } catch (Exception e) {
                    showErrorAlert("删除失败", e.getMessage());
                }
            }
        });
    }

    private void deleteUnit(UnitOfMeasureDTO dto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确认删除计量单位 " + dto.getName() + "?");
        alert.getDialogPane().getStylesheets().add("/css/style.css");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    unitOfMeasureService.delete(dto.getId());
                    loadUnits();
                    showSuccessAlert("删除成功", "计量单位删除成功");
                } catch (Exception e) {
                    showErrorAlert("删除失败", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出计量单位");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("计量单位_" + 
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        
        File file = fileChooser.showSaveDialog(unitTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("计量单位");
                
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "单位编码", "单位名称", "描述", "启用主单位", "是否启用", "创建时间", "更新时间"};
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                int rowNum = 1;
                for (UnitOfMeasureDTO dto : unitList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(dto.getId());
                    row.createCell(1).setCellValue(dto.getCode());
                    row.createCell(2).setCellValue(dto.getName());
                    row.createCell(3).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                    row.createCell(4).setCellValue(dto.isBaseUnit() ? "是" : "否");
                    row.createCell(5).setCellValue(dto.isEnabled() ? "启用" : "禁用");
                    row.createCell(6).setCellValue(dto.getCreateTime() != null ? dto.getCreateTime().format(formatter) : "");
                    row.createCell(7).setCellValue(dto.getUpdateTime() != null ? dto.getUpdateTime().format(formatter) : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                
                showSuccessAlert("导出成功", "数据已导出到: " + file.getAbsolutePath());
            } catch (Exception e) {
                showErrorAlert("导出失败", e.getMessage());
            }
        }
    }

    private Dialog<UnitOfMeasure> createUnitDialog(String title, UnitOfMeasure unit) {
        Dialog<UnitOfMeasure> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getStylesheets().add("/css/style.css");

        ButtonType saveButtonType = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.getStyleClass().add("input-field");
        codeField.setPromptText("请输入单位编码");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("input-field");
        nameField.setPromptText("请输入单位名称");

        TextField descField = new TextField();
        descField.getStyleClass().add("input-field");
        descField.setPromptText("请输入描述（可选）");

        CheckBox baseUnitCheckBox = new CheckBox("启用主单位");
        baseUnitCheckBox.setSelected(false);

        CheckBox enabledCheckBox = new CheckBox("是否启用");
        enabledCheckBox.setSelected(true);

        if (unit != null) {
            codeField.setText(unit.getCode());
            nameField.setText(unit.getName());
            descField.setText(unit.getDescription());
            baseUnitCheckBox.setSelected(unit.isBaseUnit());
            enabledCheckBox.setSelected(unit.isEnabled());
        }

        Label codeRequired = new Label("*");
        codeRequired.setStyle("-fx-text-fill: red;");
        Label nameRequired = new Label("*");
        nameRequired.setStyle("-fx-text-fill: red;");
        Label baseUnitRequired = new Label("*");
        baseUnitRequired.setStyle("-fx-text-fill: red;");
        Label enabledRequired = new Label("*");
        enabledRequired.setStyle("-fx-text-fill: red;");

        HBox codeBox = new HBox(5, new Label("单位编码:"), codeRequired);
        HBox nameBox = new HBox(5, new Label("单位名称:"), nameRequired);
        HBox baseUnitBox = new HBox(5, new Label("启用主单位:"), baseUnitRequired);
        HBox enabledBox = new HBox(5, new Label("是否启用:"), enabledRequired);

        grid.add(codeBox, 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(nameBox, 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("描述:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(baseUnitBox, 0, 3);
        grid.add(baseUnitCheckBox, 1, 3);
        grid.add(enabledBox, 0, 4);
        grid.add(enabledCheckBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        codeField.textProperty().addListener((obs, oldVal, newVal) -> 
            saveButton.setDisable(newVal.trim().isEmpty() || nameField.getText().trim().isEmpty()));
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            saveButton.setDisable(newVal.trim().isEmpty() || codeField.getText().trim().isEmpty()));

        if (unit != null) {
            saveButton.setDisable(false);
        }

        Platform.runLater(codeField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                UnitOfMeasure result = unit != null ? unit : new UnitOfMeasure();
                result.setCode(codeField.getText().trim());
                result.setName(nameField.getText().trim());
                result.setDescription(descField.getText().trim());
                result.setBaseUnit(baseUnitCheckBox.isSelected());
                result.setEnabled(enabledCheckBox.isSelected());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("/css/style.css");
        alert.showAndWait();
    }
}
