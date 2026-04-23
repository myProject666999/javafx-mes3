package com.mes.controller;

import com.mes.dto.WorkshopDTO;
import com.mes.service.AuthService;
import com.mes.service.WorkshopService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WorkshopController {

    private final WorkshopService workshopService;
    private final AuthService authService;

    @FXML
    private TableView<WorkshopDTO> workshopTable;

    @FXML
    private TableColumn<WorkshopDTO, String> codeColumn;

    @FXML
    private TableColumn<WorkshopDTO, String> nameColumn;

    @FXML
    private TableColumn<WorkshopDTO, String> locationColumn;

    @FXML
    private TableColumn<WorkshopDTO, String> managerColumn;

    @FXML
    private TableColumn<WorkshopDTO, String> descriptionColumn;

    @FXML
    private TableColumn<WorkshopDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<WorkshopDTO, String> createTimeColumn;

    @FXML
    private TableColumn<WorkshopDTO, Boolean> actionColumn;

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

    private ObservableList<WorkshopDTO> allWorkshops = FXCollections.observableArrayList();

    public WorkshopController(WorkshopService workshopService, AuthService authService) {
        this.workshopService = workshopService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupEnabledComboBox();
        loadWorkshops();
        setupPermissions();
    }

    private void setupEnabledComboBox() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCode()));
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        locationColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLocation()));
        managerColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getManager()));
        descriptionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
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
        createTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCreateTime()));
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("workshop:edit");
        boolean canDelete = authService.hasPermission("workshop:delete");

        actionColumn.setCellFactory(param -> new TableCell<>() {
            final Button editBtn = new Button("修改");
            final Button deleteBtn = new Button("删除");
            final HBox pane = new HBox(5);

            {
                editBtn.getStyleClass().addAll("action-button", "edit-button");
                deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                editBtn.setOnAction(event -> {
                    WorkshopDTO dto = getTableView().getItems().get(getIndex());
                    showEditDialog(dto);
                });

                deleteBtn.setOnAction(event -> {
                    WorkshopDTO dto = getTableView().getItems().get(getIndex());
                    deleteWorkshop(dto);
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
        });
        actionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(true));
    }

    private void setupPermissions() {
        boolean canAdd = authService.hasPermission("workshop:add");
        boolean canEdit = authService.hasPermission("workshop:edit");
        boolean canDelete = authService.hasPermission("workshop:delete");

        addButton.setVisible(canAdd);
        addButton.setManaged(canAdd);
        editButton.setVisible(canEdit);
        editButton.setManaged(canEdit);
        deleteButton.setVisible(canDelete);
        deleteButton.setManaged(canDelete);

        if (!canEdit && !canDelete) {
            actionColumn.setVisible(false);
        }
    }

    private void loadWorkshops() {
        List<WorkshopDTO> workshops = workshopService.findAll();
        allWorkshops.setAll(workshops);
        workshopTable.setItems(allWorkshops);
    }

    @FXML
    public void handleSearch() {
        String name = nameSearchField.getText();
        String enabledStr = enabledSearchCombo.getValue();
        Boolean enabled = null;
        if ("启用".equals(enabledStr)) {
            enabled = true;
        } else if ("禁用".equals(enabledStr)) {
            enabled = false;
        }

        List<WorkshopDTO> results = workshopService.search(name, enabled);
        allWorkshops.setAll(results);
        workshopTable.setItems(allWorkshops);
    }

    @FXML
    public void handleReset() {
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadWorkshops();
    }

    @FXML
    public void showAddDialog() {
        Dialog<WorkshopDTO> dialog = new Dialog<>();
        dialog.setTitle("新增车间");
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
        codeField.setPromptText("自动生成");
        codeField.setEditable(false);
        codeField.setText(workshopService.generateCode());

        TextField nameField = new TextField();
        nameField.setPromptText("请输入车间名称");

        TextField locationField = new TextField();
        locationField.setPromptText("请输入车间位置");

        TextField managerField = new TextField();
        managerField.setPromptText("请输入负责人");

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("车间编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("车间名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("车间位置:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("负责人:"), 0, 3);
        grid.add(managerField, 1, 3);
        grid.add(new Label("描述:"), 0, 4);
        grid.add(descField, 1, 4);
        grid.add(enabledCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(300);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "车间名称不能为空");
                    return null;
                }

                WorkshopDTO dto = new WorkshopDTO();
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setLocation(locationField.getText());
                dto.setManager(managerField.getText());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<WorkshopDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                workshopService.save(dto);
                loadWorkshops();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(WorkshopDTO workshop) {
        Dialog<WorkshopDTO> dialog = new Dialog<>();
        dialog.setTitle("修改车间");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(80);
        labelCol2.setPrefWidth(80);
        ColumnConstraints fieldCol2 = new ColumnConstraints();
        fieldCol2.setMinWidth(200);
        fieldCol2.setPrefWidth(300);
        grid.getColumnConstraints().addAll(labelCol2, fieldCol2);

        TextField codeField = new TextField(workshop.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(workshop.getName());
        TextField locationField = new TextField(workshop.getLocation());
        TextField managerField = new TextField(workshop.getManager());
        TextArea descField = new TextArea(workshop.getDescription());
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(workshop.isEnabled());

        grid.add(new Label("车间编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("车间名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("车间位置:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("负责人:"), 0, 3);
        grid.add(managerField, 1, 3);
        grid.add(new Label("描述:"), 0, 4);
        grid.add(descField, 1, 4);
        grid.add(enabledCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(300);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "车间名称不能为空");
                    return null;
                }

                WorkshopDTO dto = new WorkshopDTO();
                dto.setId(workshop.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setLocation(locationField.getText());
                dto.setManager(managerField.getText());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<WorkshopDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                workshopService.save(dto);
                loadWorkshops();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteWorkshop(WorkshopDTO workshop) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除车间 \"" + workshop.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                workshopService.deleteById(workshop.getId());
                loadWorkshops();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        WorkshopDTO selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的车间");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        ObservableList<WorkshopDTO> selected = workshopTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要删除的车间");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selected.size() + " 个车间吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (WorkshopDTO dto : selected) {
                    workshopService.deleteById(dto.getId());
                }
                loadWorkshops();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出车间数据");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("车间数据.xlsx");

        File file = fileChooser.showSaveDialog(workshopTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("车间数据");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"车间编码", "车间名称", "车间位置", "负责人", "描述", "是否启用", "创建时间"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                List<WorkshopDTO> data = workshopTable.getItems();
                for (int i = 0; i < data.size(); i++) {
                    WorkshopDTO dto = data.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(dto.getCode());
                    row.createCell(1).setCellValue(dto.getName());
                    row.createCell(2).setCellValue(dto.getLocation() != null ? dto.getLocation() : "");
                    row.createCell(3).setCellValue(dto.getManager() != null ? dto.getManager() : "");
                    row.createCell(4).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                    row.createCell(5).setCellValue(dto.isEnabled() ? "启用" : "禁用");
                    row.createCell(6).setCellValue(dto.getCreateTime() != null ? dto.getCreateTime() : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                showAlert("成功", "导出成功！文件保存在：" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("错误", "导出失败: " + e.getMessage());
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
