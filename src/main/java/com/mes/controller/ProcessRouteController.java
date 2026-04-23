package com.mes.controller;

import com.mes.dto.ProcessDTO;
import com.mes.dto.ProcessRouteDTO;
import com.mes.dto.ProcessRouteStepDTO;
import com.mes.service.AuthService;
import com.mes.service.ProcessRouteService;
import com.mes.service.ProcessService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProcessRouteController {

    private final ProcessRouteService processRouteService;
    private final ProcessService processService;
    private final AuthService authService;

    @FXML
    private TableView<ProcessRouteDTO> processRouteTable;

    @FXML
    private TableColumn<ProcessRouteDTO, String> codeColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, String> nameColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, String> keyProcessColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, String> descriptionColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, String> createTimeColumn;

    @FXML
    private TableColumn<ProcessRouteDTO, Boolean> actionColumn;

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

    private ObservableList<ProcessRouteDTO> allProcessRoutes = FXCollections.observableArrayList();

    public ProcessRouteController(ProcessRouteService processRouteService, ProcessService processService, AuthService authService) {
        this.processRouteService = processRouteService;
        this.processService = processService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupComboBoxes();
        loadProcessRoutes();
        setupPermissions();
    }

    private void setupComboBoxes() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCode()));
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        keyProcessColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKeyProcessName()));
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
        boolean canEdit = authService.hasPermission("process_route:edit");
        boolean canDelete = authService.hasPermission("process_route:delete");

        actionColumn.setCellFactory(param -> new TableCell<>() {
            final Button editBtn = new Button("修改");
            final Button deleteBtn = new Button("删除");
            final HBox pane = new HBox(5);

            {
                editBtn.getStyleClass().addAll("action-button", "edit-button");
                deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                editBtn.setOnAction(event -> {
                    ProcessRouteDTO dto = getTableView().getItems().get(getIndex());
                    showEditDialog(dto);
                });

                deleteBtn.setOnAction(event -> {
                    ProcessRouteDTO dto = getTableView().getItems().get(getIndex());
                    deleteProcessRoute(dto);
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
        boolean canAdd = authService.hasPermission("process_route:add");
        boolean canEdit = authService.hasPermission("process_route:edit");
        boolean canDelete = authService.hasPermission("process_route:delete");

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

    private void loadProcessRoutes() {
        List<ProcessRouteDTO> processRoutes = processRouteService.findAll();
        allProcessRoutes.setAll(processRoutes);
        processRouteTable.setItems(allProcessRoutes);
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

        List<ProcessRouteDTO> results = processRouteService.search(name, enabled);
        allProcessRoutes.setAll(results);
        processRouteTable.setItems(allProcessRoutes);
    }

    @FXML
    public void handleReset() {
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadProcessRoutes();
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProcessRouteDTO> dialog = new Dialog<>();
        dialog.setTitle("新增工艺流程");
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
        codeField.setText(processRouteService.generateCode());

        TextField nameField = new TextField();
        nameField.setPromptText("请输入工艺流程名称");

        List<ProcessDTO> enabledProcesses = processService.findAllEnabled();
        ComboBox<ProcessDTO> keyProcessCombo = new ComboBox<>();
        ProcessDTO emptyOption = new ProcessDTO();
        emptyOption.setId(null);
        emptyOption.setName("无");
        ObservableList<ProcessDTO> processOptions = FXCollections.observableArrayList();
        processOptions.add(emptyOption);
        processOptions.addAll(enabledProcesses);
        keyProcessCombo.setItems(processOptions);
        keyProcessCombo.setPromptText("请选择关键工序");
        keyProcessCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProcessDTO dto) {
                return dto == null || dto.getId() == null ? "无" : dto.getCode() + " - " + dto.getName();
            }

            @Override
            public ProcessDTO fromString(String string) {
                return null;
            }
        });
        keyProcessCombo.getSelectionModel().selectFirst();

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        VBox stepsBox = new VBox(10);
        stepsBox.setPadding(new Insets(10, 0, 0, 0));
        Label stepsLabel = new Label("工序步骤:");
        stepsLabel.setStyle("-fx-text-fill: #cccccc;");
        
        TableView<ProcessRouteStepDTO> stepsTable = new TableView<>();
        stepsTable.setPrefHeight(200);
        stepsTable.setEditable(true);

        TableColumn<ProcessRouteStepDTO, Number> orderCol = new TableColumn<>("顺序");
        orderCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getStepOrder()));
        orderCol.setPrefWidth(60);
        orderCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        TableColumn<ProcessRouteStepDTO, String> processCol = new TableColumn<>("工序");
        processCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProcessName()));
        processCol.setPrefWidth(200);

        TableColumn<ProcessRouteStepDTO, String> stepDescCol = new TableColumn<>("描述");
        stepDescCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        stepDescCol.setPrefWidth(150);

        stepsTable.getColumns().addAll(orderCol, processCol, stepDescCol);

        HBox stepsButtonBox = new HBox(10);
        Button addStepBtn = new Button("添加工序");
        Button removeStepBtn = new Button("删除工序");
        Button moveUpBtn = new Button("上移");
        Button moveDownBtn = new Button("下移");
        stepsButtonBox.getChildren().addAll(addStepBtn, removeStepBtn, moveUpBtn, moveDownBtn);

        ObservableList<ProcessRouteStepDTO> stepsList = FXCollections.observableArrayList();
        stepsTable.setItems(stepsList);

        addStepBtn.setOnAction(e -> {
            Dialog<ProcessRouteStepDTO> stepDialog = createStepDialog(enabledProcesses, null, stepsList.size() + 1);
            Optional<ProcessRouteStepDTO> stepResult = stepDialog.showAndWait();
            stepResult.ifPresent(step -> {
                step.setStepOrder(stepsList.size() + 1);
                stepsList.add(step);
            });
        });

        removeStepBtn.setOnAction(e -> {
            ProcessRouteStepDTO selected = stepsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                stepsList.remove(selected);
                for (int i = 0; i < stepsList.size(); i++) {
                    stepsList.get(i).setStepOrder(i + 1);
                }
                stepsTable.refresh();
            }
        });

        moveUpBtn.setOnAction(e -> {
            int selectedIndex = stepsTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex > 0) {
                ProcessRouteStepDTO current = stepsList.get(selectedIndex);
                ProcessRouteStepDTO previous = stepsList.get(selectedIndex - 1);
                int currentOrder = current.getStepOrder();
                current.setStepOrder(previous.getStepOrder());
                previous.setStepOrder(currentOrder);
                stepsList.set(selectedIndex - 1, current);
                stepsList.set(selectedIndex, previous);
                stepsTable.getSelectionModel().select(selectedIndex - 1);
            }
        });

        moveDownBtn.setOnAction(e -> {
            int selectedIndex = stepsTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < stepsList.size() - 1) {
                ProcessRouteStepDTO current = stepsList.get(selectedIndex);
                ProcessRouteStepDTO next = stepsList.get(selectedIndex + 1);
                int currentOrder = current.getStepOrder();
                current.setStepOrder(next.getStepOrder());
                next.setStepOrder(currentOrder);
                stepsList.set(selectedIndex + 1, current);
                stepsList.set(selectedIndex, next);
                stepsTable.getSelectionModel().select(selectedIndex + 1);
            }
        });

        stepsBox.getChildren().addAll(stepsLabel, stepsTable, stepsButtonBox);

        grid.add(new Label("工艺流程编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工艺流程名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("关键工序:"), 0, 2);
        grid.add(keyProcessCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(enabledCheck, 1, 4);
        grid.add(stepsBox, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(550);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工艺流程名称不能为空");
                    return null;
                }

                ProcessRouteDTO dto = new ProcessRouteDTO();
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                if (keyProcessCombo.getValue() != null && keyProcessCombo.getValue().getId() != null) {
                    dto.setKeyProcessId(keyProcessCombo.getValue().getId());
                }
                dto.setSteps(new ArrayList<>(stepsList));
                return dto;
            }
            return null;
        });

        Optional<ProcessRouteDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                processRouteService.save(dto);
                loadProcessRoutes();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    private Dialog<ProcessRouteStepDTO> createStepDialog(List<ProcessDTO> enabledProcesses, ProcessRouteStepDTO existingStep, int defaultOrder) {
        Dialog<ProcessRouteStepDTO> stepDialog = new Dialog<>();
        stepDialog.setTitle(existingStep == null ? "添加工序步骤" : "修改工序步骤");
        stepDialog.setHeaderText(null);

        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        stepDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane stepGrid = new GridPane();
        stepGrid.setHgap(10);
        stepGrid.setVgap(10);
        stepGrid.setPadding(new Insets(20, 20, 10, 20));

        TextField orderField = new TextField(String.valueOf(defaultOrder));
        orderField.setEditable(false);

        ComboBox<ProcessDTO> processCombo = new ComboBox<>();
        processCombo.setItems(FXCollections.observableArrayList(enabledProcesses));
        processCombo.setPromptText("请选择工序");
        processCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProcessDTO dto) {
                return dto == null ? "" : dto.getCode() + " - " + dto.getName();
            }

            @Override
            public ProcessDTO fromString(String string) {
                return null;
            }
        });

        if (existingStep != null && existingStep.getProcessId() != null) {
            for (ProcessDTO p : enabledProcesses) {
                if (p.getId().equals(existingStep.getProcessId())) {
                    processCombo.setValue(p);
                    break;
                }
            }
        }

        TextArea stepDescField = new TextArea();
        stepDescField.setPromptText("请输入步骤描述");
        stepDescField.setPrefRowCount(2);
        if (existingStep != null && existingStep.getDescription() != null) {
            stepDescField.setText(existingStep.getDescription());
        }

        stepGrid.add(new Label("顺序:"), 0, 0);
        stepGrid.add(orderField, 1, 0);
        stepGrid.add(new Label("工序:"), 0, 1);
        stepGrid.add(processCombo, 1, 1);
        stepGrid.add(new Label("描述:"), 0, 2);
        stepGrid.add(stepDescField, 1, 2);

        stepDialog.getDialogPane().setContent(stepGrid);

        stepDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                if (processCombo.getValue() == null) {
                    showAlert("错误", "请选择工序");
                    return null;
                }
                ProcessRouteStepDTO step = new ProcessRouteStepDTO();
                step.setProcessId(processCombo.getValue().getId());
                step.setProcessCode(processCombo.getValue().getCode());
                step.setProcessName(processCombo.getValue().getName());
                step.setStepOrder(Integer.parseInt(orderField.getText()));
                step.setDescription(stepDescField.getText());
                return step;
            }
            return null;
        });

        return stepDialog;
    }

    public void showEditDialog(ProcessRouteDTO processRoute) {
        Dialog<ProcessRouteDTO> dialog = new Dialog<>();
        dialog.setTitle("修改工艺流程");
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

        TextField codeField = new TextField(processRoute.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(processRoute.getName());

        List<ProcessDTO> enabledProcesses = processService.findAllEnabled();
        ComboBox<ProcessDTO> keyProcessCombo = new ComboBox<>();
        ProcessDTO emptyOption = new ProcessDTO();
        emptyOption.setId(null);
        emptyOption.setName("无");
        ObservableList<ProcessDTO> processOptions = FXCollections.observableArrayList();
        processOptions.add(emptyOption);
        processOptions.addAll(enabledProcesses);
        keyProcessCombo.setItems(processOptions);
        keyProcessCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProcessDTO dto) {
                return dto == null || dto.getId() == null ? "无" : dto.getCode() + " - " + dto.getName();
            }

            @Override
            public ProcessDTO fromString(String string) {
                return null;
            }
        });

        if (processRoute.getKeyProcessId() != null) {
            for (ProcessDTO p : processOptions) {
                if (p.getId() != null && p.getId().equals(processRoute.getKeyProcessId())) {
                    keyProcessCombo.setValue(p);
                    break;
                }
            }
            if (keyProcessCombo.getValue() == null) {
                keyProcessCombo.getSelectionModel().selectFirst();
            }
        } else {
            keyProcessCombo.getSelectionModel().selectFirst();
        }

        TextArea descField = new TextArea(processRoute.getDescription());
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(processRoute.isEnabled());

        VBox stepsBox = new VBox(10);
        stepsBox.setPadding(new Insets(10, 0, 0, 0));
        Label stepsLabel = new Label("工序步骤:");
        stepsLabel.setStyle("-fx-text-fill: #cccccc;");
        
        TableView<ProcessRouteStepDTO> stepsTable = new TableView<>();
        stepsTable.setPrefHeight(200);
        stepsTable.setEditable(true);

        TableColumn<ProcessRouteStepDTO, Number> orderCol = new TableColumn<>("顺序");
        orderCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getStepOrder()));
        orderCol.setPrefWidth(60);

        TableColumn<ProcessRouteStepDTO, String> processCol = new TableColumn<>("工序");
        processCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProcessName()));
        processCol.setPrefWidth(200);

        TableColumn<ProcessRouteStepDTO, String> stepDescCol = new TableColumn<>("描述");
        stepDescCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        stepDescCol.setPrefWidth(150);

        stepsTable.getColumns().addAll(orderCol, processCol, stepDescCol);

        HBox stepsButtonBox = new HBox(10);
        Button addStepBtn = new Button("添加工序");
        Button removeStepBtn = new Button("删除工序");
        Button moveUpBtn = new Button("上移");
        Button moveDownBtn = new Button("下移");
        stepsButtonBox.getChildren().addAll(addStepBtn, removeStepBtn, moveUpBtn, moveDownBtn);

        ObservableList<ProcessRouteStepDTO> stepsList = FXCollections.observableArrayList();
        if (processRoute.getSteps() != null) {
            stepsList.addAll(processRoute.getSteps());
        }
        stepsTable.setItems(stepsList);

        addStepBtn.setOnAction(e -> {
            Dialog<ProcessRouteStepDTO> stepDialog = createStepDialog(enabledProcesses, null, stepsList.size() + 1);
            Optional<ProcessRouteStepDTO> stepResult = stepDialog.showAndWait();
            stepResult.ifPresent(step -> {
                step.setStepOrder(stepsList.size() + 1);
                stepsList.add(step);
            });
        });

        removeStepBtn.setOnAction(e -> {
            ProcessRouteStepDTO selected = stepsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                stepsList.remove(selected);
                for (int i = 0; i < stepsList.size(); i++) {
                    stepsList.get(i).setStepOrder(i + 1);
                }
                stepsTable.refresh();
            }
        });

        moveUpBtn.setOnAction(e -> {
            int selectedIndex = stepsTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex > 0) {
                ProcessRouteStepDTO current = stepsList.get(selectedIndex);
                ProcessRouteStepDTO previous = stepsList.get(selectedIndex - 1);
                int currentOrder = current.getStepOrder();
                current.setStepOrder(previous.getStepOrder());
                previous.setStepOrder(currentOrder);
                stepsList.set(selectedIndex - 1, current);
                stepsList.set(selectedIndex, previous);
                stepsTable.getSelectionModel().select(selectedIndex - 1);
            }
        });

        moveDownBtn.setOnAction(e -> {
            int selectedIndex = stepsTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < stepsList.size() - 1) {
                ProcessRouteStepDTO current = stepsList.get(selectedIndex);
                ProcessRouteStepDTO next = stepsList.get(selectedIndex + 1);
                int currentOrder = current.getStepOrder();
                current.setStepOrder(next.getStepOrder());
                next.setStepOrder(currentOrder);
                stepsList.set(selectedIndex + 1, current);
                stepsList.set(selectedIndex, next);
                stepsTable.getSelectionModel().select(selectedIndex + 1);
            }
        });

        stepsBox.getChildren().addAll(stepsLabel, stepsTable, stepsButtonBox);

        grid.add(new Label("工艺流程编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("工艺流程名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("关键工序:"), 0, 2);
        grid.add(keyProcessCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(enabledCheck, 1, 4);
        grid.add(stepsBox, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(550);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "工艺流程名称不能为空");
                    return null;
                }

                ProcessRouteDTO dto = new ProcessRouteDTO();
                dto.setId(processRoute.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                if (keyProcessCombo.getValue() != null && keyProcessCombo.getValue().getId() != null) {
                    dto.setKeyProcessId(keyProcessCombo.getValue().getId());
                }
                dto.setSteps(new ArrayList<>(stepsList));
                return dto;
            }
            return null;
        });

        Optional<ProcessRouteDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                processRouteService.save(dto);
                loadProcessRoutes();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteProcessRoute(ProcessRouteDTO processRoute) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除工艺流程 \"" + processRoute.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                processRouteService.deleteById(processRoute.getId());
                loadProcessRoutes();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        ProcessRouteDTO selected = processRouteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的工艺流程");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        ObservableList<ProcessRouteDTO> selected = processRouteTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert("提示", "请先选择要删除的工艺流程");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selected.size() + " 个工艺流程吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (ProcessRouteDTO dto : selected) {
                    processRouteService.deleteById(dto.getId());
                }
                loadProcessRoutes();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出工艺流程数据");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("工艺流程数据.xlsx");

        File file = fileChooser.showSaveDialog(processRouteTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("工艺流程数据");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"工艺流程编码", "工艺流程名称", "关键工序", "描述", "是否启用", "创建时间"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                List<ProcessRouteDTO> data = processRouteTable.getItems();
                for (int i = 0; i < data.size(); i++) {
                    ProcessRouteDTO dto = data.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(dto.getCode());
                    row.createCell(1).setCellValue(dto.getName());
                    row.createCell(2).setCellValue(dto.getKeyProcessName() != null ? dto.getKeyProcessName() : "");
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
