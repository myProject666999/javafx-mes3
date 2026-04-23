package com.mes.controller;

import com.mes.dto.ProductCategoryDTO;
import com.mes.entity.ProductCategory;
import com.mes.service.AuthService;
import com.mes.service.ProductCategoryService;
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
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductCategoryController {

    private final ProductCategoryService categoryService;
    private final AuthService authService;

    @FXML
    private TreeTableView<ProductCategoryDTO> categoryTreeTable;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, String> codeColumn;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, String> nameColumn;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, String> descriptionColumn;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, Boolean> enabledColumn;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, String> createTimeColumn;

    @FXML
    private TreeTableColumn<ProductCategoryDTO, Boolean> actionColumn;

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
    private Button expandButton;

    @FXML
    private Button collapseButton;

    private TreeItem<ProductCategoryDTO> rootItem;
    private ObservableList<ProductCategoryDTO> allCategories = FXCollections.observableArrayList();

    public ProductCategoryController(ProductCategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupTreeTableColumns();
        setupActionColumn();
        setupEnabledComboBox();
        loadCategories();
        setupPermissions();
    }

    private void setupEnabledComboBox() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupTreeTableColumns() {
        codeColumn.setCellValueFactory(param -> {
            TreeItem<ProductCategoryDTO> item = param.getValue();
            if (item == null || item.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(item.getValue().getCode());
        });
        nameColumn.setCellValueFactory(param -> {
            TreeItem<ProductCategoryDTO> item = param.getValue();
            if (item == null || item.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(item.getValue().getName());
        });
        descriptionColumn.setCellValueFactory(param -> {
            TreeItem<ProductCategoryDTO> item = param.getValue();
            if (item == null || item.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(item.getValue().getDescription());
        });
        enabledColumn.setCellValueFactory(param -> {
            TreeItem<ProductCategoryDTO> item = param.getValue();
            if (item == null || item.getValue() == null) return new SimpleBooleanProperty(false);
            return new SimpleBooleanProperty(item.getValue().isEnabled());
        });
        enabledColumn.setCellFactory(col -> new TreeTableCell<>() {
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
        createTimeColumn.setCellValueFactory(param -> {
            TreeItem<ProductCategoryDTO> item = param.getValue();
            if (item == null || item.getValue() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(item.getValue().getCreateTime());
        });
    }

    private void setupActionColumn() {
        boolean canEdit = authService.hasPermission("product_category:edit");
        boolean canDelete = authService.hasPermission("product_category:delete");

        Callback<TreeTableColumn<ProductCategoryDTO, Boolean>, TreeTableCell<ProductCategoryDTO, Boolean>> cellFactory =
                param -> new TreeTableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            ProductCategoryDTO dto = getTreeTableView().getTreeItem(getIndex()).getValue();
                            showEditDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            ProductCategoryDTO dto = getTreeTableView().getTreeItem(getIndex()).getValue();
                            deleteCategory(dto);
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
        boolean canAdd = authService.hasPermission("product_category:add");
        boolean canEdit = authService.hasPermission("product_category:edit");
        boolean canDelete = authService.hasPermission("product_category:delete");

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

    private void loadCategories() {
        List<ProductCategoryDTO> categories = categoryService.findAllTree();
        allCategories.setAll(categories);

        rootItem = new TreeItem<>(new ProductCategoryDTO());
        rootItem.setExpanded(true);

        for (ProductCategoryDTO category : categories) {
            TreeItem<ProductCategoryDTO> item = createTreeItem(category);
            rootItem.getChildren().add(item);
        }

        categoryTreeTable.setRoot(rootItem);
        categoryTreeTable.setShowRoot(false);
    }

    private TreeItem<ProductCategoryDTO> createTreeItem(ProductCategoryDTO dto) {
        TreeItem<ProductCategoryDTO> item = new TreeItem<>(dto);
        item.setExpanded(dto.isExpanded());

        if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
            for (ProductCategoryDTO child : dto.getChildren()) {
                item.getChildren().add(createTreeItem(child));
            }
        }

        return item;
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

        List<ProductCategoryDTO> results = categoryService.search(name, enabled);
        allCategories.setAll(results);

        // 重新创建 rootItem 避免状态问题
        rootItem = new TreeItem<>(new ProductCategoryDTO());
        rootItem.setExpanded(true);

        for (ProductCategoryDTO category : results) {
            if (category.getParentId() == null) {
                TreeItem<ProductCategoryDTO> item = createTreeItemFromFlat(category, results);
                rootItem.getChildren().add(item);
            }
        }

        categoryTreeTable.setRoot(rootItem);
        categoryTreeTable.setShowRoot(false);
    }

    private TreeItem<ProductCategoryDTO> createTreeItemFromFlat(ProductCategoryDTO dto, List<ProductCategoryDTO> all) {
        TreeItem<ProductCategoryDTO> item = new TreeItem<>(dto);
        item.setExpanded(true);

        List<ProductCategoryDTO> children = all.stream()
                .filter(c -> dto.getId().equals(c.getParentId()))
                .collect(Collectors.toList());

        for (ProductCategoryDTO child : children) {
            item.getChildren().add(createTreeItemFromFlat(child, all));
        }

        return item;
    }

    @FXML
    public void handleReset() {
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadCategories();
    }

    @FXML
    public void handleExpandAll() {
        expandAll(rootItem, true);
    }

    @FXML
    public void handleCollapseAll() {
        collapseChildren(rootItem);
    }

    private void collapseChildren(TreeItem<?> item) {
        if (item == null) return;
        for (TreeItem<?> child : item.getChildren()) {
            child.setExpanded(false);
            collapseChildren(child);
        }
    }

    private void expandAll(TreeItem<?> item, boolean expand) {
        if (item == null) return;
        item.setExpanded(expand);
        if (item.getChildren() != null) {
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child, expand);
            }
        }
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProductCategoryDTO> dialog = new Dialog<>();
        dialog.setTitle("新增分类");
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

        TextField nameField = new TextField();
        nameField.setPromptText("请输入分类名称");

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        ComboBox<ProductCategoryDTO> parentCombo = new ComboBox<>();
        parentCombo.setItems(FXCollections.observableArrayList(categoryService.findAll()));
        parentCombo.setPromptText("请选择父分类（可选）");
        parentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ProductCategoryDTO object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public ProductCategoryDTO fromString(String string) {
                return null;
            }
        });

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("分类编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("分类名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("父分类:"), 0, 2);
        grid.add(parentCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(enabledCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(280);

        parentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            Long parentId = newVal == null ? null : newVal.getId();
            String code = categoryService.generateCode(parentId);
            codeField.setText(code);
        });

        codeField.setText(categoryService.generateCode(null));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "分类名称不能为空");
                    return null;
                }

                ProductCategoryDTO dto = new ProductCategoryDTO();
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                if (parentCombo.getValue() != null) {
                    dto.setParentId(parentCombo.getValue().getId());
                }
                return dto;
            }
            return null;
        });

        Optional<ProductCategoryDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                categoryService.save(dto);
                loadCategories();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(ProductCategoryDTO category) {
        Dialog<ProductCategoryDTO> dialog = new Dialog<>();
        dialog.setTitle("修改分类");
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

        TextField codeField = new TextField(category.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(category.getName());
        TextArea descField = new TextArea(category.getDescription());
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(category.isEnabled());

        grid.add(new Label("分类编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("分类名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("描述:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(enabledCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(250);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "分类名称不能为空");
                    return null;
                }

                ProductCategoryDTO dto = new ProductCategoryDTO();
                dto.setId(category.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setEnabled(enabledCheck.isSelected());
                dto.setParentId(category.getParentId());
                return dto;
            }
            return null;
        });

        Optional<ProductCategoryDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                categoryService.save(dto);
                loadCategories();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void deleteCategory(ProductCategoryDTO category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除分类 \"" + category.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryService.deleteById(category.getId());
                loadCategories();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        TreeItem<ProductCategoryDTO> selected = categoryTreeTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null || selected.getValue().getId() == null) {
            showAlert("提示", "请先选择要修改的分类");
            return;
        }
        showEditDialog(selected.getValue());
    }

    @FXML
    public void handleBatchDelete() {
        TreeItem<ProductCategoryDTO> selected = categoryTreeTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null || selected.getValue().getId() == null) {
            showAlert("提示", "请先选择要删除的分类");
            return;
        }
        deleteCategory(selected.getValue());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
