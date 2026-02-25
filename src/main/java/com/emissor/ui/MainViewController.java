package com.emissor.ui;

import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.service.ItemService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MainViewController {
    
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    @FXML private TableView<ItemResponseDTO> itemsTable;
    @FXML private TableColumn<ItemResponseDTO, Long> idColumn;
    @FXML private TableColumn<ItemResponseDTO, String> codigoColumn;
    @FXML private TableColumn<ItemResponseDTO, Integer> quantidadeColumn;
    @FXML private TableColumn<ItemResponseDTO, String> descricaoColumn;
    @FXML private TableColumn<ItemResponseDTO, String> dataColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    @FXML private Label footerLabel;
    
    private ItemService itemService;
    private ObservableList<ItemResponseDTO> itemsList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }
    
    @FXML
    public void initialize() {
        // Configurar colunas da tabela
        idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId()));
        
        codigoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCodigoReferencia()));
        
        quantidadeColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getQuantidade()));
        
        descricaoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescricao()));
        
        dataColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDataRecebimento() != null ? 
                cellData.getValue().getDataRecebimento().format(dateFormatter) : ""));
        
        // Configurar tabela
        itemsTable.setItems(itemsList);
        
        // Habilitar/desabilitar botões baseado na seleção
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        // Buscar ao pressionar Enter
        searchField.setOnAction(e -> handleSearch());
    }
    
    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText("Servidor: " + status));
    }
    
    public void loadItems() {
        if (itemService == null) return;
        
        Platform.runLater(() -> {
            try {
                List<ItemResponseDTO> items = itemService.getAllItems();
                itemsList.clear();
                itemsList.addAll(items);
                updateItemCount();
                footerLabel.setText("Última atualização: " + java.time.LocalDateTime.now().format(dateFormatter));
            } catch (Exception e) {
                showError("Erro ao carregar itens: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        
        if (itemService == null) return;
        
        try {
            List<ItemResponseDTO> items = itemService.searchItems(searchTerm);
            itemsList.clear();
            itemsList.addAll(items);
            updateItemCount();
        } catch (Exception e) {
            showError("Erro ao buscar itens: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadItems();
    }
    
    @FXML
    private void handleRefresh() {
        loadItems();
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar para CSV");
        fileChooser.setInitialFileName("itens_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            exportToCSV(file);
        }
    }
    
    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Header
            writer.write("ID,Código Referência,Quantidade,Descrição,Data Recebimento\n");
            
            // Data
            for (ItemResponseDTO item : itemsList) {
                writer.write(String.format("%d,\"%s\",%d,\"%s\",\"%s\"\n",
                    item.getId(),
                    item.getCodigoReferencia(),
                    item.getQuantidade(),
                    item.getDescricao(),
                    item.getDataRecebimento().format(dateFormatter)
                ));
            }
            
            showInfo("Exportação concluída", "Dados exportados com sucesso para: " + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Erro ao exportar: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAdd() {
        ItemDialog dialog = new ItemDialog();
        Optional<ItemRequestDTO> result = dialog.showAndWait();
        
        result.ifPresent(itemRequest -> {
            try {
                itemService.createItem(itemRequest);
                loadItems();
                showInfo("Sucesso", "Item adicionado com sucesso!");
            } catch (Exception e) {
                showError("Erro ao adicionar item: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleEdit() {
        ItemResponseDTO selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        ItemDialog dialog = new ItemDialog(selected);
        Optional<ItemRequestDTO> result = dialog.showAndWait();
        
        result.ifPresent(itemRequest -> {
            try {
                itemService.updateItem(selected.getId(), itemRequest);
                loadItems();
                showInfo("Sucesso", "Item atualizado com sucesso!");
            } catch (Exception e) {
                showError("Erro ao atualizar item: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleDelete() {
        ItemResponseDTO selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir este item?");
        alert.setContentText(String.format("Código: %s\nDescrição: %s", 
            selected.getCodigoReferencia(), selected.getDescricao()));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itemService.deleteItem(selected.getId());
                loadItems();
                showInfo("Sucesso", "Item excluído com sucesso!");
            } catch (Exception e) {
                showError("Erro ao excluir item: " + e.getMessage());
            }
        }
    }
    
    private void updateItemCount() {
        countLabel.setText("Total: " + itemsList.size() + " itens");
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
