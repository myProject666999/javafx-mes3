package com.mes.controller;

import com.mes.dto.ProcessDTO;
import com.mes.dto.WorkshopDTO;
import com.mes.service.AuthService;
import com.mes.service.ProcessService;
import com.mes.service.WorkshopService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
public class ProcessController {

    private final ProcessService processService;
    private final WorkshopService workshopService;
    private final AuthService authService;

    @FXML
    private TableView<ProcessDTO> processTable;

    @FXML
    private TableColumn<ProcessDTO, String> codeColumn;

    @FXML
    private TableColumn<ProcessDTO, String> nameColumn;

    @FXML
    private TableColumn<ProcessDTO, String> workshopColumn;

    @FXML
    private TableColumn<ProcessDTO, String> descriptionColumn;

    @FXML
    private TableColumn<ProcessDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<ProcessDTO, String> createTimeColumn;

    @FXML
    private TableColumn<ProcessDTO, Boolean> actionColumn;

    @FXML
    private TextField nameSearchField;

    @FXML
    private ComboBox<WorkshopDTO> workshopSearchCombo;

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

    private ObservableList<ProcessDTO> allProcesses = FXCollections.observableArrayList();

    public ProcessController(ProcessService processService, WorkshopService workshopService, AuthService authService) {
        this.processService = processService;
        this.workshopService = workshopService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupComboBoxes();
        loadProcesses();
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
        boolean canEdit = authService.hasPermission("process:edit");
        boolean canDelete = authService.hasPermission("process:delete");

        actionColumn.setCellFactory(param -> new TableCell<>() {
            final Button editBtn = new Button("修改");
            final Button deleteBtn = new Button("删除");
            final HBox pane = new HBox(5);

            {
                editBtn.getStyleClass().addAll("action-button", "edit-button");
                deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                editBtn.setOnAction(event -> {
                    ProcessDTO dto = getTableView().getItems().get(getIndex());
                    showEditDialog(dto);
                });

                deleteBtn.setOnAction(event -> {
                    ProcessDTO dto = getTableView().getItems().get(getIndex());
                    deleteProcess(dto);
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
        boolean canAdd = authService.hasPermission("process:add");
        boolean canEdit = authService.hasPermission("process:edit");
        boolean canDelete = authService.hasPermission("process:delete");

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

    private void loadProcesses() {
        List<ProcessDTO> processes = processService.findAll();
        allProcesses.setAll(processes);
        processTable.setItems(allProcesses);
    }

    @FXML
    public void handleSearch() {
        String name = nameSearchField.getText();
        WorkshopDTO selectedWorkshop = workshopSearchCombo.getValue();
        Long workshopId = (selectedWorkshop != null && selectedWorkshop.getId() != null) ? selectedWorkshop.getId() : null;
        String enabledStr = enabledSearchCombo.getValue();
        Boolean enabled = null;
        if ("启用".equals(enabledStr)) {
            enabled = true;
        } else if ("禁用".equals(enabledStr)) {
            enabled = false;
        }

        List<ProcessDTO> results = processService.search(name, workshopId, enabled);
        allProcesses.setAll(results);
        processTable.setItems(allProcesses);
    }

    @FXML
    public void handleReset() {
        nameSearchField.clear();
        workshopSearchCombo.getSelectionModel().selectFirst();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadProcesses();
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProcessDTO> dialog = new Dialog<>();
        dialog.setTitle("新增工序");
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
        codeField.setText(processService.generateCode());

        TextField nameField = new TextField();
        nameField.setPromptText("请输入工序名称");

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

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("工序编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工序名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("所属车间:"), 0, 2);
        grid.add(workshopCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(enabledCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(320);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工序名称不能为空");
                    return null;
                }

                ProcessDTO dto = new ProcessDTO();
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                if (workshopCombo.getValue() != null) {
                    dto.setWorkshopId(workshopCombo.getValue().getId());
                }
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<ProcessDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                processService.save(dto);
                loadProcesses();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(ProcessDTO process) {
        Dialog<ProcessDTO> dialog = new Dialog<>();
        dialog.setTitle("修改工序");
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

        TextField codeField = new TextField(process.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(process.getName());

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
        if (process.getWorkshopId() != null) {
            workshopService.findById(process.getWorkshopId()).ifPresent(workshopCombo::setValue);
        }

        TextArea descField = new TextArea(process.getDescription());
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(process.isEnabled());

        grid.add(new Label("工序编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工序名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("所属车间:"), 0, 2);
        grid.add(workshopCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(enabledCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(320);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工序名称不能为空");
                    return null;
                }

                ProcessDTO dto = new ProcessDTO();
                dto.setId(process.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                if (workshopCombo.getValue() != null) {
                    dto.setWorkshopId(workshopCombo.getValue().getId());
                }
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<ProcessDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                processService.save(dto);
                loadProcesses();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteProcess(ProcessDTO process) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除工序 \"" + process.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                processService.deleteById(process.getId());
                loadProcesses();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        ProcessDTO selected = processTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的工序");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        ObservableList<ProcessDTO> selected = processTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要删除的工序");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selected.size() + " 个工序吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (ProcessDTO dto : selected) {
                    processService.deleteById(dto.getId());
                }
                loadProcesses();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出工序数据");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("工序数据.xlsx");

        File file = fileChooser.showSaveDialog(processTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("工序数据");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"工序编码", "工序名称", "所属车间", "描述", "是否启用", "创建时间"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                List<ProcessDTO> data = processTable.getItems();
                for (int i = 0; i < data.size(); i++) {
                    ProcessDTO dto = data.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(dto.getCode());
                    row.createCell(1).setCellValue(dto.getName());
                    row.createCell(2).setCellValue(dto.getWorkshopName() != null ? dto.getWorkshopName() : "");
                    row.createCell(3).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                    row.createCell(4).setCellValue(dto.isEnabled() ? "启用" : "禁用");
                    row.createCell(5).setCellValue(dto.getCreateTime() != null ? dto.getCreateTime() : "");
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
