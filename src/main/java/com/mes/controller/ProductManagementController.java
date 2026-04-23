package com.mes.controller;

import com.mes.dto.BomItemDTO;
import com.mes.dto.ProductCategoryDTO;
import com.mes.dto.ProductDTO;
import com.mes.entity.ProductCategory;
import com.mes.entity.UnitOfMeasure;
import com.mes.service.AuthService;
import com.mes.service.BomItemService;
import com.mes.service.ProductCategoryService;
import com.mes.service.ProductService;
import com.mes.service.UnitOfMeasureService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductManagementController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final UnitOfMeasureService unitOfMeasureService;
    private final BomItemService bomItemService;
    private final AuthService authService;

    @FXML
    private TreeView<String> categoryTree;

    @FXML
    private TableView<ProductDTO> productTable;

    @FXML
    private TableColumn<ProductDTO, Boolean> selectColumn;

    @FXML
    private TableColumn<ProductDTO, String> codeColumn;

    @FXML
    private TableColumn<ProductDTO, String> nameColumn;

    @FXML
    private TableColumn<ProductDTO, String> categoryColumn;

    @FXML
    private TableColumn<ProductDTO, String> unitColumn;

    @FXML
    private TableColumn<ProductDTO, String> specColumn;

    @FXML
    private TableColumn<ProductDTO, String> stockRangeColumn;

    @FXML
    private TableColumn<ProductDTO, Boolean> enabledColumn;

    @FXML
    private TableColumn<ProductDTO, Boolean> actionColumn;

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

    private ObservableList<ProductDTO> productList = FXCollections.observableArrayList();
    private TreeItem<String> rootTreeItem;
    private Long selectedCategoryId = null;
    private ObservableList<ProductDTO> selectedProducts = FXCollections.observableArrayList();

    public ProductManagementController(ProductService productService,
                                      ProductCategoryService categoryService,
                                      UnitOfMeasureService unitOfMeasureService,
                                      BomItemService bomItemService,
                                      AuthService authService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.unitOfMeasureService = unitOfMeasureService;
        this.bomItemService = bomItemService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        setupCategoryTree();
        setupProductTable();
        setupEnabledComboBox();
        loadCategories();
        loadProducts();
        setupPermissions();
    }

    private void setupEnabledComboBox() {
        enabledSearchCombo.setItems(FXCollections.observableArrayList("全部", "启用", "禁用"));
        enabledSearchCombo.getSelectionModel().selectFirst();
    }

    private void setupCategoryTree() {
        rootTreeItem = new TreeItem<>("全部分类");
        rootTreeItem.setExpanded(true);
        categoryTree.setRoot(rootTreeItem);
        categoryTree.setShowRoot(true);

        categoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal == rootTreeItem) {
                    selectedCategoryId = null;
                } else {
                    selectedCategoryId = (Long) newVal.getGraphic().getUserData();
                }
                loadProducts();
            }
        });
    }

    private void setupProductTable() {
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(param -> {
            ProductDTO product = param.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(selectedProducts.contains(product));
            property.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedProducts.add(product);
                } else {
                    selectedProducts.remove(product);
                }
            });
            return property;
        });

        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        specColumn.setCellValueFactory(new PropertyValueFactory<>("specification"));
        stockRangeColumn.setCellValueFactory(new PropertyValueFactory<>("stockRange"));

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
        boolean canEdit = authService.hasPermission("product:edit");
        boolean canDelete = authService.hasPermission("product:delete");

        Callback<TableColumn<ProductDTO, Boolean>, TableCell<ProductDTO, Boolean>> cellFactory =
                param -> new TableCell<>() {
                    final Button editBtn = new Button("修改");
                    final Button bomBtn = new Button("BOM");
                    final Button deleteBtn = new Button("删除");
                    final HBox pane = new HBox(5);

                    {
                        editBtn.getStyleClass().addAll("action-button", "edit-button");
                        bomBtn.getStyleClass().addAll("action-button");
                        bomBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                        deleteBtn.getStyleClass().addAll("action-button", "delete-button");

                        editBtn.setOnAction(event -> {
                            ProductDTO dto = getTableView().getItems().get(getIndex());
                            showEditDialog(dto);
                        });

                        bomBtn.setOnAction(event -> {
                            ProductDTO dto = getTableView().getItems().get(getIndex());
                            showBomDialog(dto);
                        });

                        deleteBtn.setOnAction(event -> {
                            ProductDTO dto = getTableView().getItems().get(getIndex());
                            deleteProduct(dto);
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
                            pane.getChildren().add(bomBtn);
                            if (canDelete) pane.getChildren().add(deleteBtn);
                            setGraphic(pane);
                        }
                    }
                };

        actionColumn.setCellFactory(cellFactory);
        actionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(true));
    }

    private void setupPermissions() {
        boolean canAdd = authService.hasPermission("product:add");
        boolean canEdit = authService.hasPermission("product:edit");
        boolean canDelete = authService.hasPermission("product:delete");

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
        rootTreeItem.getChildren().clear();
        List<ProductCategoryDTO> categories = categoryService.findAllTree();
        for (ProductCategoryDTO category : categories) {
            TreeItem<String> item = createCategoryTreeItem(category);
            rootTreeItem.getChildren().add(item);
        }
    }

    @SuppressWarnings("unchecked")
    private TreeItem<String> createCategoryTreeItem(ProductCategoryDTO category) {
        Label label = new Label(category.getName());
        label.setUserData(category.getId());

        TreeItem<String> item = new TreeItem<>(category.getName());
        item.setGraphic(label);
        item.setExpanded(true);

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (ProductCategoryDTO child : category.getChildren()) {
                TreeItem<String> childItem = createCategoryTreeItem(child);
                item.getChildren().add(childItem);
            }
        }

        return item;
    }

    private void loadProducts() {
        List<ProductDTO> products;
        if (selectedCategoryId == null) {
            products = productService.findAll();
        } else {
            products = productService.findByCategoryId(selectedCategoryId);
        }
        productList.setAll(products);
        productTable.setItems(productList);
        selectedProducts.clear();
    }

    @FXML
    public void refreshCategoryTree() {
        loadCategories();
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

        List<ProductDTO> results = productService.search(code, name, selectedCategoryId, enabled);
        productList.setAll(results);
        productTable.setItems(productList);
        selectedProducts.clear();
    }

    @FXML
    public void handleReset() {
        codeSearchField.clear();
        nameSearchField.clear();
        enabledSearchCombo.getSelectionModel().selectFirst();
        loadProducts();
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProductDTO> dialog = new Dialog<>();
        dialog.setTitle("新增物料/产品");
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
        codeField.setPromptText("请输入物料编码");

        Button autoGenBtn = new Button("自动生成");
        autoGenBtn.setOnAction(e -> {
            String code = productService.generateCode(selectedCategoryId);
            codeField.setText(code);
        });

        HBox codeBox = new HBox(10, codeField, autoGenBtn);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入物料名称");

        ComboBox<ProductCategoryDTO> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(categoryService.findAllEnabled()));
        categoryCombo.setPromptText("请选择分类");
        categoryCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ProductCategoryDTO object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public ProductCategoryDTO fromString(String string) {
                return null;
            }
        });
        if (selectedCategoryId != null) {
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId().equals(selectedCategoryId))
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);
        }

        ComboBox<UnitOfMeasure> unitCombo = new ComboBox<>();
        unitCombo.setItems(FXCollections.observableArrayList(unitOfMeasureService.findAllEnabled()));
        unitCombo.setPromptText("请选择单位");
        unitCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(UnitOfMeasure object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public UnitOfMeasure fromString(String string) {
                return null;
            }
        });

        TextField specField = new TextField();
        specField.setPromptText("请输入规格");

        TextArea descField = new TextArea();
        descField.setPromptText("请输入描述");
        descField.setPrefRowCount(3);

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(true);

        grid.add(new Label("物料编码:"), 0, 0);
        grid.add(codeBox, 1, 0);
        grid.add(new Label("物料名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("分类:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("单位:"), 0, 3);
        grid.add(unitCombo, 1, 3);
        grid.add(new Label("规格:"), 0, 4);
        grid.add(specField, 1, 4);
        grid.add(new Label("描述:"), 0, 5);
        grid.add(descField, 1, 5);
        grid.add(enabledCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(350);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
                    showAlert("错误", "物料编码不能为空");
                    return null;
                }
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "物料名称不能为空");
                    return null;
                }
                if (categoryCombo.getValue() == null) {
                    showAlert("错误", "请选择分类");
                    return null;
                }
                if (unitCombo.getValue() == null) {
                    showAlert("错误", "请选择单位");
                    return null;
                }

                ProductDTO dto = new ProductDTO();
                dto.setCode(codeField.getText().trim());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setSpecification(specField.getText());
                dto.setCategoryId(categoryCombo.getValue().getId());
                dto.setUnitId(unitCombo.getValue().getId());
                dto.setEnabled(enabledCheck.isSelected());
                return dto;
            }
            return null;
        });

        Optional<ProductDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                productService.save(dto);
                loadProducts();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showEditDialog(ProductDTO product) {
        Dialog<ProductDTO> dialog = new Dialog<>();
        dialog.setTitle("修改物料/产品");
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

        TextField codeField = new TextField(product.getCode());
        codeField.setEditable(false);

        TextField nameField = new TextField(product.getName());

        ComboBox<ProductCategoryDTO> categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList(categoryService.findAllEnabled()));
        categoryCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ProductCategoryDTO object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public ProductCategoryDTO fromString(String string) {
                return null;
            }
        });
        categoryCombo.getItems().stream()
                .filter(c -> c.getId().equals(product.getCategoryId()))
                .findFirst()
                .ifPresent(categoryCombo::setValue);

        ComboBox<UnitOfMeasure> unitCombo = new ComboBox<>();
        unitCombo.setItems(FXCollections.observableArrayList(unitOfMeasureService.findAllEnabled()));
        unitCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(UnitOfMeasure object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public UnitOfMeasure fromString(String string) {
                return null;
            }
        });
        unitCombo.getItems().stream()
                .filter(u -> u.getId().equals(product.getUnitId()))
                .findFirst()
                .ifPresent(unitCombo::setValue);

        TextField specField = new TextField(product.getSpecification());

        TextArea descField = new TextArea(product.getDescription());
        descField.setPrefRowCount(3);

        TextField minStockField = new TextField(product.getMinStock() != null ? String.valueOf(product.getMinStock()) : "");
        minStockField.setPromptText("最小库存");

        TextField maxStockField = new TextField(product.getMaxStock() != null ? String.valueOf(product.getMaxStock()) : "");
        maxStockField.setPromptText("最大库存");

        CheckBox enabledCheck = new CheckBox("启用");
        enabledCheck.setSelected(product.isEnabled());

        grid.add(new Label("物料编码:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("物料名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("分类:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("单位:"), 0, 3);
        grid.add(unitCombo, 1, 3);
        grid.add(new Label("规格:"), 0, 4);
        grid.add(specField, 1, 4);
        grid.add(new Label("描述:"), 0, 5);
        grid.add(descField, 1, 5);
        grid.add(new Label("最小库存:"), 0, 6);
        grid.add(minStockField, 1, 6);
        grid.add(new Label("最大库存:"), 0, 7);
        grid.add(maxStockField, 1, 7);
        grid.add(enabledCheck, 1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(400);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    showAlert("错误", "物料名称不能为空");
                    return null;
                }
                if (categoryCombo.getValue() == null) {
                    showAlert("错误", "请选择分类");
                    return null;
                }
                if (unitCombo.getValue() == null) {
                    showAlert("错误", "请选择单位");
                    return null;
                }

                ProductDTO dto = new ProductDTO();
                dto.setId(product.getId());
                dto.setCode(codeField.getText());
                dto.setName(nameField.getText().trim());
                dto.setDescription(descField.getText());
                dto.setSpecification(specField.getText());
                dto.setCategoryId(categoryCombo.getValue().getId());
                dto.setUnitId(unitCombo.getValue().getId());
                dto.setEnabled(enabledCheck.isSelected());

                try {
                    if (!minStockField.getText().trim().isEmpty()) {
                        dto.setMinStock(Double.parseDouble(minStockField.getText().trim()));
                    }
                    if (!maxStockField.getText().trim().isEmpty()) {
                        dto.setMaxStock(Double.parseDouble(maxStockField.getText().trim()));
                    }
                } catch (NumberFormatException e) {
                    showAlert("错误", "库存数值格式不正确");
                    return null;
                }

                return dto;
            }
            return null;
        });

        Optional<ProductDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                productService.save(dto);
                loadProducts();
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    public void showBomDialog(ProductDTO product) {
        Dialog<List<BomItemDTO>> dialog = new Dialog<>();
        dialog.setTitle("BOM配置 - " + product.getName());
        dialog.setHeaderText(null);
        dialog.setWidth(800);
        dialog.setHeight(600);

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label infoLabel = new Label("产品: " + product.getCode() + " - " + product.getName());
        infoLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");

        TableView<BomItemDTO> bomTable = new TableView<>();
        bomTable.setPrefHeight(350);

        TableColumn<BomItemDTO, String> materialCodeCol = new TableColumn<>("物料编码");
        materialCodeCol.setCellValueFactory(new PropertyValueFactory<>("materialCode"));
        materialCodeCol.setPrefWidth(100);

        TableColumn<BomItemDTO, String> materialNameCol = new TableColumn<>("物料名称");
        materialNameCol.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        materialNameCol.setPrefWidth(150);

        TableColumn<BomItemDTO, String> specCol = new TableColumn<>("规格");
        specCol.setCellValueFactory(new PropertyValueFactory<>("materialSpecification"));
        specCol.setPrefWidth(100);

        TableColumn<BomItemDTO, Double> qtyCol = new TableColumn<>("数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<BomItemDTO, String> unitCol = new TableColumn<>("单位");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        unitCol.setPrefWidth(60);

        TableColumn<BomItemDTO, String> remarkCol = new TableColumn<>("备注");
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("remark"));
        remarkCol.setPrefWidth(120);

        TableColumn<BomItemDTO, Boolean> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(param -> new TableCell<>() {
            final Button deleteBtn = new Button("删除");
            {
                deleteBtn.getStyleClass().addAll("action-button", "delete-button");
                deleteBtn.setOnAction(event -> {
                    BomItemDTO item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        actionCol.setCellValueFactory(param -> new SimpleBooleanProperty(true));

        bomTable.getColumns().addAll(materialCodeCol, materialNameCol, specCol, qtyCol, unitCol, remarkCol, actionCol);

        // Load existing BOM items
        List<BomItemDTO> existingItems = bomItemService.findByProductId(product.getId());
        ObservableList<BomItemDTO> bomItems = FXCollections.observableArrayList(existingItems);
        bomTable.setItems(bomItems);

        Button addBomBtn = new Button("➕ 添加物料");
        addBomBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        addBomBtn.setOnAction(e -> {
            showAddBomItemDialog(bomItems);
        });

        content.getChildren().addAll(infoLabel, bomTable, addBomBtn);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new ArrayList<>(bomItems);
            }
            return null;
        });

        Optional<List<BomItemDTO>> result = dialog.showAndWait();
        result.ifPresent(items -> {
            try {
                bomItemService.saveAll(product.getId(), items);
                showAlert("成功", "BOM配置已保存");
            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        });
    }

    private void showAddBomItemDialog(ObservableList<BomItemDTO> bomItems) {
        Dialog<BomItemDTO> dialog = new Dialog<>();
        dialog.setTitle("添加BOM物料");
        dialog.setHeaderText(null);

        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<ProductDTO> materialCombo = new ComboBox<>();
        materialCombo.setItems(FXCollections.observableArrayList(productService.findAllSimple()));
        materialCombo.setPromptText("请选择物料");
        materialCombo.setPrefWidth(250);
        materialCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ProductDTO object) {
                return object == null ? "" : object.getCode() + " - " + object.getName();
            }

            @Override
            public ProductDTO fromString(String string) {
                return null;
            }
        });

        TextField qtyField = new TextField();
        qtyField.setPromptText("请输入数量");

        TextField remarkField = new TextField();
        remarkField.setPromptText("请输入备注（可选）");

        grid.add(new Label("物料:"), 0, 0);
        grid.add(materialCombo, 1, 0);
        grid.add(new Label("数量:"), 0, 1);
        grid.add(qtyField, 1, 1);
        grid.add(new Label("备注:"), 0, 2);
        grid.add(remarkField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (materialCombo.getValue() == null) {
                    showAlert("错误", "请选择物料");
                    return null;
                }
                if (qtyField.getText() == null || qtyField.getText().trim().isEmpty()) {
                    showAlert("错误", "请输入数量");
                    return null;
                }

                double quantity;
                try {
                    quantity = Double.parseDouble(qtyField.getText().trim());
                    if (quantity <= 0) {
                        showAlert("错误", "数量必须大于0");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("错误", "数量格式不正确");
                    return null;
                }

                ProductDTO material = materialCombo.getValue();

                // Check if material already exists in BOM
                boolean exists = bomItems.stream()
                        .anyMatch(item -> item.getMaterialId().equals(material.getId()));
                if (exists) {
                    showAlert("错误", "该物料已在BOM中");
                    return null;
                }

                BomItemDTO dto = new BomItemDTO();
                dto.setMaterialId(material.getId());
                dto.setMaterialCode(material.getCode());
                dto.setMaterialName(material.getName());
                dto.setMaterialSpecification(material.getSpecification());
                dto.setQuantity(quantity);
                dto.setUnitId(material.getUnitId());
                dto.setUnitName(material.getUnitName());
                dto.setRemark(remarkField.getText());

                return dto;
            }
            return null;
        });

        Optional<BomItemDTO> result = dialog.showAndWait();
        result.ifPresent(bomItems::add);
    }

    public void deleteProduct(ProductDTO product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除物料 \"" + product.getName() + "\" 吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteById(product.getId());
                loadProducts();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBatchEdit() {
        ProductDTO selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要修改的物料");
            return;
        }
        showEditDialog(selected);
    }

    @FXML
    public void handleBatchDelete() {
        if (selectedProducts.isEmpty()) {
            showAlert("提示", "请先选择要删除的物料");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的 " + selectedProducts.size() + " 个物料吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (ProductDTO product : selectedProducts) {
                try {
                    productService.deleteById(product.getId());
                } catch (Exception e) {
                    showAlert("错误", "删除物料 " + product.getName() + " 失败: " + e.getMessage());
                }
            }
            loadProducts();
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
