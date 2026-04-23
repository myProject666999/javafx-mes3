package com.mes.controller;

import com.mes.dto.WorkshopDTO;
import com.mes.dto.WorkstationDTO;
import com.mes.service.AuthService;
import com.mes.service.WorkshopService;
import com.mes.service.WorkstationService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

@Component
public class WorkstationController {

    private final WorkstationService workstationService;
    private final WorkshopService workshopService;
    private final AuthService authService;

    @FXML
    private TableView<WorkstationDTO> workstationTable;

    @FXML
    private TableColumn<WorkstationDTO, String> codeColumn;

    @FXML
    private TableColumn<WorkstationDTO, String> nameColumn;

    @FXML
    private TableColumn<WorkstationDTO, String> workshopColumn;

    @FXML
    private TableColumn<WorkstationDTO, String> processColumn;

    @FXML
    private TableColumn<WorkstationDTO, Number> equipmentCountColumn;

    @FXML
    private TableColumn<WorkstationDTO, Number> workerCountColumn;

    @FXML
    private TableColumn<WorkstationDTO, String> descriptionColumn;

    @FXML
    private TableColumn<WorkstationDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<WorkstationDTO, String> createTimeColumn;

    @FXML
    private TableColumn<WorkstationDTO, Boolean> actionColumn;

    @FXML
    private TextField nameSearchField;

    @FXML
    private ComboBox<WorkshopDTO> workshopSearchCombo;

    @FXML
    private TextField processSearchField;

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

    private ObservableList<WorkstationDTO> allWorkstations = FXCollections.observableArrayList();

    public WorkstationController(WorkstationService workstationService, WorkshopService workshopService, AuthService authService) {
        this.workstationService = workstationService;
        this.workshopService = workshopService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupComboBoxes();
        loadWorkstations();
        setupPermissions();
    }

    private void setupComboBoxes() {
        List<WorkshopDTO> workshops = workshopService.findAllEnabled();
        WorkshopDTO allOption = new WorkshopDTO();
        allOption.setId(null);
        allOption.setName("全部");
        ObservableList<WorkshopDTO> workshopOptions = FXCollections.observableArrayList();
        workshopOptions.add(allOption);
        workshopOptions.addAll(workshops);
        workshopSearchCombo.setItems(workshopOptions);
        workshopSearchCombo.getSelectionModel().selectFirst();
        workshopSearchCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(WorkshopDTO dto) {
                return dto == null ? "" : dto.getName();
            }

            @Override
            public WorkshopDTO fromString(String string) {
                return null;
            }
        });

        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCode()));
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        workshopColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWorkshopName()));
        processColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProcessName()));
        equipmentCountColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getEquipmentCount()));
        workerCountColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getWorkerCount()));
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
        boolean canEdit = authService.hasPermission("workstation:edit");
        boolean canDelete = authService.hasPermission("workstation:delete");

        actionColumn.setCellFactory(param -> new TableCell<>() {
            final Button editBtn = new Button("修改");
            final Button deleteBtn = new Button("删除");
            final HBox pane = new HBox(5);

            {
                editBtn.getStyleClass().addAll("action-button", "edit-button");
                deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                editBtn.setOnAction(event -> {
                    WorkstationDTO dto = getTableView().getItems().get(getIndex());
                    showEditDialog(dto);
                });

                deleteBtn.setOnAction(event -> {
                    WorkstationDTO dto = getTableView().getItems().get(getIndex());
                    deleteWorkstation(dto);
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
        boolean canAdd = authService.hasPermission("workstation:add");
        boolean canEdit = authService.hasPermission("workstation:edit");
        boolean canDelete = authService.hasPermission("workstation:delete");

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

    private void loadWorkstations() {
        List<WorkstationDTO> workstations = workstationService.findAll();
        allWorkstations.setAll(workstations);
        workstationTable.setItems(allWorkstations);
    }

    @FXML
    public void handleSearch() {
        String name = nameSearchField.getText();
        WorkshopDTO selectedWorkshop = workshopSearchCombo.getValue();
        Long workshopId = (selectedWorkshop != null && selectedWorkshop.getId() != null) ? selectedWorkshop.getId() : null;
        String processName = processSearchField.getText();
        String enabledStr = enabledSearchCombo.getValue();
        Boolean enabled = null;
        if ("启用".equals(enabledStr)) {
            enabled = true;
        } else if ("禁用".equals(enabledStr)) {
            enabled = false;
        }

        List<WorkstationDTO> results = workstationService.search(name, workshopId, processName, enabled);
        allWorkstations.setAll(results);
        workstationTable.setItems(allWorkstations);
    }

    @FXML
    public void handleReset() {
        nameSearchField.clear();
        workshopSearchCombo.getSelectionModel().selectFirst();
        processSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadWorkstations();
    }

    @FXML
    public void showAddDialog() {
        Dialog<WorkstationDTO> dialog = new Dialog<>();
        dialog.setTitle("新增工作站");
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
        codeField.setText(workstationService.generateCode());

        TextField nameField = new TextField();
        nameField.setPromptText("请输入工作站名称");

        ComboBox<WorkshopDTO> workshopCombo = new ComboBox<>();
        workshopCombo.setItems(FXCollections.observableArrayList(workshopService.findAllEnabled()));
        workshopCombo.setPromptText("请选择车间");
        workshopCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(WorkshopDTO dto) {
                return dto == null ? "" : dto.getCode() + " - " + dto.getName();
            }

            @Override
            public WorkshopDTO fromString(String string) {
                return null;
            }
        });

        TextField processField = new TextField();
        processField.setPromptText("请输入所属工序");

        TextField equipmentCountField = new TextField("0");
        equipmentCountField.setPromptText("设备数量");

        TextField workerCountField = new TextField("0");
        workerCountField.setPromptText("工人数量");

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("工作站编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工作站名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("所在车间:"), 0, 2);
        grid.add(workshopCombo, 1, 2);
        grid.add(new Label("所属工序:"), 0, 3);
        grid.add(processField, 1, 3);
        grid.add(new Label("设备数量:"), 0, 4);
        grid.add(equipmentCountField, 1, 4);
        grid.add(new Label("工人数量:"), 0, 5);
        grid.add(workerCountField, 1, 5);
        grid.add(new Label("描述:"), 0, 6);
        grid.add(descField, 1, 6);
        grid.add(enabledCheck, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(380);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工作站名称不能为空");
                    return null;
                }
                if (workshopCombo.getValue() == null) {
                    showAlert("错误", "请选择所在车间");
                    return null;
                }
                if (processField.getText() == null || processField.getText().trim().isEmpty()) {
                    showAlert("错误", "所属工序不能为空");
                    return null;
                }

                WorkstationDTO dto = new WorkstationDTO();
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setWorkshopId(workshopCombo.getValue().getId());
                dto.setProcessName(processField.getText().trim());
                try {
                    dto.setEquipmentCount(Integer.parseInt(equipmentCountField.getText()));
                } catch (NumberFormatException e) {
                    dto.setEquipmentCount(0);
                }
                try {
                    dto.setWorkerCount(Integer.parseInt(workerCountField.getText()));
                } catch (NumberFormatException e) {
                    dto.setWorkerCount(0);
                }
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<WorkstationDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                workstationService.save(dto);
                loadWorkstations();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(WorkstationDTO workstation) {
        Dialog<WorkstationDTO> dialog = new Dialog<>();
        dialog.setTitle("修改工作站");
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

        TextField codeField = new TextField(workstation.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(workstation.getName());

        ComboBox<WorkshopDTO> workshopCombo = new ComboBox<>();
        workshopCombo.setItems(FXCollections.observableArrayList(workshopService.findAllEnabled()));
        workshopCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(WorkshopDTO dto) {
                return dto == null ? "" : dto.getCode() + " - " + dto.getName();
            }

            @Override
            public WorkshopDTO fromString(String string) {
                return null;
            }
        });
        if (workstation.getWorkshopId() != null) {
            workshopService.findById(workstation.getWorkshopId()).ifPresent(workshopCombo::setValue);
        }

        TextField processField = new TextField(workstation.getProcessName());
        TextField equipmentCountField = new TextField(String.valueOf(workstation.getEquipmentCount()));
        TextField workerCountField = new TextField(String.valueOf(workstation.getWorkerCount()));
        TextArea descField = new TextArea(workstation.getDescription());
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(workstation.isEnabled());

        grid.add(new Label("工作站编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工作站名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("所在车间:"), 0, 2);
        grid.add(workshopCombo, 1, 2);
        grid.add(new Label("所属工序:"), 0, 3);
        grid.add(processField, 1, 3);
        grid.add(new Label("设备数量:"), 0, 4);
        grid.add(equipmentCountField, 1, 4);
        grid.add(new Label("工人数量:"), 0, 5);
        grid.add(workerCountField, 1, 5);
        grid.add(new Label("描述:"), 0, 6);
        grid.add(descField, 1, 6);
        grid.add(enabledCheck, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(380);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工作站名称不能为空");
                    return null;
                }
                if (workshopCombo.getValue() == null) {
                    showAlert("错误", "请选择所在车间");
                    return null;
                }
                if (processField.getText() == null || processField.getText().trim().isEmpty()) {
                    showAlert("错误", "所属工序不能为空");
                    return null;
                }

                WorkstationDTO dto = new WorkstationDTO();
                dto.setId(workstation.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setWorkshopId(workshopCombo.getValue().getId());
                dto.setProcessName(processField.getText().trim());
                try {
                    dto.setEquipmentCount(Integer.parseInt(equipmentCountField.getText()));
                } catch (NumberFormatException e) {
                    dto.setEquipmentCount(0);
                }
                try {
                    dto.setWorkerCount(Integer.parseInt(workerCountField.getText()));
                } catch (NumberFormatException e) {
                    dto.setWorkerCount(0);
                }
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<WorkstationDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                workstationService.save(dto);
                loadWorkstations();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteWorkstation(WorkstationDTO workstation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除工作站 \"" + workstation.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                workstationService.deleteById(workstation.getId());
                loadWorkstations();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        WorkstationDTO selected = workstationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的工作站");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        ObservableList<WorkstationDTO> selected = workstationTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要删除的工作站");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selected.size() + " 个工作站吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (WorkstationDTO dto : selected) {
                    workstationService.deleteById(dto.getId());
                }
                loadWorkstations();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出工作站数据");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("工作站数据.xlsx");

        File file = fileChooser.showSaveDialog(workstationTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("工作站数据");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"工作站编码", "工作站名称", "所在车间", "所属工序", "设备数量", "工人数量", "描述", "是否启用", "创建时间"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                List<WorkstationDTO> data = workstationTable.getItems();
                for (int i = 0; i < data.size(); i++) {
                    WorkstationDTO dto = data.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(dto.getCode());
                    row.createCell(1).setCellValue(dto.getName());
                    row.createCell(2).setCellValue(dto.getWorkshopName() != null ? dto.getWorkshopName() : "");
                    row.createCell(3).setCellValue(dto.getProcessName() != null ? dto.getProcessName() : "");
                    row.createCell(4).setCellValue(dto.getEquipmentCount());
                    row.createCell(5).setCellValue(dto.getWorkerCount());
                    row.createCell(6).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                    row.createCell(7).setCellValue(dto.isEnabled() ? "启用" : "禁用");
                    row.createCell(8).setCellValue(dto.getCreateTime() != null ? dto.getCreateTime() : "");
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
