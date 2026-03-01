package com.emissor.ui;

import com.emissor.config.ConfigurationManager;
import com.emissor.dto.ColetaResponseDTO;
import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import com.emissor.service.ItemService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainViewController {

    private static final String DEFAULT_COLETA = ItemService.DEFAULT_COLETA;
    private static final long COLETAS_REFRESH_INTERVAL_MS = 15000L;

    private SettingsViewController settingsViewController;
    private final ConfigurationManager configurationManager = new ConfigurationManager();

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private ComboBox<ColetaOption> coletaComboBox;

    @FXML private TextField coletaNameField;
    @FXML private Button createColetaButton;
    @FXML private Button toggleBlockColetaButton;
    @FXML private Button deleteColetaButton;
    @FXML private Button refreshColetasButton;
    @FXML private TableView<ColetaResponseDTO> coletasTable;
    @FXML private TableColumn<ColetaResponseDTO, String> coletaNameColumn;
    @FXML private TableColumn<ColetaResponseDTO, String> coletaBlockedColumn;
    @FXML private TableColumn<ColetaResponseDTO, Long> coletaTotalColumn;

    @FXML private TableView<ItemResponseDTO> itemsTable;
    @FXML private TableColumn<ItemResponseDTO, Long> idColumn;
    @FXML private TableColumn<ItemResponseDTO, String> coletaColumn;
    @FXML private TableColumn<ItemResponseDTO, String> codigoColumn;
    @FXML private TableColumn<ItemResponseDTO, Integer> quantidadeColumn;
    @FXML private TableColumn<ItemResponseDTO, String> descricaoColumn;
    @FXML private TableColumn<ItemResponseDTO, String> dataColumn;

    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    @FXML private Label footerLabel;

    private ItemService itemService;
    private boolean autoUpdateEnabled = false;
    private int autoUpdateInterval = 5;
    private Thread autoUpdateThread;
    private volatile boolean dialogOpen = false;
    private boolean loadingColetas = false;
    private boolean syncingSelection = false;
    private long lastColetasLoadAt = 0L;

    private final ObservableList<ItemResponseDTO> itemsList = FXCollections.observableArrayList();
    private final ObservableList<ColetaResponseDTO> coletasList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
        Platform.runLater(() -> {
            loadColetas(true);
            refreshItems(false);
        });
    }

    @FXML
    public void initialize() {
        setupItemsTable();
        setupColetasTable();
        setupSelections();
        setupButtonsAndSearch();
    }

    private void setupItemsTable() {
        idColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getId()));
        coletaColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getColeta()));
        codigoColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCodigoReferencia()));
        quantidadeColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getQuantidade()));
        descricaoColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDescricao()));
        dataColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDataRecebimento() != null
                ? cellData.getValue().getDataRecebimento().format(dateFormatter)
                : ""));

        itemsTable.setItems(itemsList);
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateItemActionButtons();
        });
    }

    private void setupColetasTable() {
        coletaNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNome()));
        coletaBlockedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isBloqueada() ? "Sim" : "Nao"));
        coletaTotalColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getTotalItens()));

        coletasTable.setItems(coletasList);
    }

    private void setupSelections() {
        coletaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (loadingColetas || syncingSelection || newValue == null) {
                return;
            }
            configurationManager.setSelectedColeta(newValue.nome);
            try {
                configurationManager.saveConfiguration();
            } catch (IOException ignored) {
            }
            syncTableSelectionByName(newValue.nome);
            updateColetaActionButtons();
            refreshItems(false);
        });

        coletasTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (loadingColetas || syncingSelection || newValue == null) {
                return;
            }
            coletaNameField.setText(newValue.getNome());
            syncComboSelectionByName(newValue.getNome());
            updateColetaActionButtons();
        });
    }

    private void setupButtonsAndSearch() {
        settingsViewController = SettingsViewController.getInstance();
        if (settingsViewController != null) {
            setupAutoUpdateIntegration();
        } else {
            Platform.runLater(() -> {
                settingsViewController = SettingsViewController.getInstance();
                setupAutoUpdateIntegration();
            });
        }
        searchField.setOnAction(e -> handleSearch());
    }

    private void setupAutoUpdateIntegration() {
        if (settingsViewController == null) {
            return;
        }
        settingsViewController.setAutoUpdateListener((enabled, interval) -> {
            autoUpdateEnabled = enabled;
            autoUpdateInterval = Math.max(1, interval);
            restartAutoUpdateThread();
        });
        settingsViewController.setManualUpdateListener(() -> refreshItems(true));
    }

    private void restartAutoUpdateThread() {
        if (autoUpdateThread != null && autoUpdateThread.isAlive()) {
            autoUpdateThread.interrupt();
        }
        if (autoUpdateEnabled) {
            autoUpdateThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(autoUpdateInterval * 1000L);
                        if (!dialogOpen) {
                            refreshItems(false);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            autoUpdateThread.setDaemon(true);
            autoUpdateThread.start();
        }
    }

    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText("Servidor: " + status));
    }

    public void loadItems() {
        refreshItems(false);
    }

    @FXML
    private void handleSearch() {
        refreshItems(false);
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        refreshItems(false);
    }

    @FXML
    private void handleRefresh() {
        loadColetas(true);
        refreshItems(false);
    }

    @FXML
    private void handleRefreshColetas() {
        loadColetas(true);
    }

    @FXML
    private void handleCreateColetaFromControl() {
        String coleta = coletaNameField.getText() == null ? "" : coletaNameField.getText().trim();
        if (coleta.isEmpty()) {
            showError("Informe o nome da coleta.");
            return;
        }
        try {
            itemService.createColeta(coleta);
            loadColetas(true);
            syncComboSelectionByName(coleta);
            refreshItems(false);
        } catch (Exception e) {
            showError("Erro ao cadastrar coleta: " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleColetaBlock() {
        ColetaResponseDTO selected = getSelectedColetaForControl();
        if (selected == null) {
            showError("Selecione uma coleta na tela de controle.");
            return;
        }
        try {
            itemService.setColetaBloqueada(selected.getNome(), !selected.isBloqueada());
            loadColetas(true);
            syncComboSelectionByName(selected.getNome());
            refreshItems(false);
        } catch (Exception e) {
            showError("Erro ao alterar bloqueio da coleta: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteColeta() {
        ColetaResponseDTO selected = getSelectedColetaForControl();
        if (selected == null) {
            showError("Selecione uma coleta na tela de controle.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Excluir Coleta");
        confirm.setHeaderText("Deseja excluir a coleta " + selected.getNome() + "?");
        confirm.setContentText("A exclusao so e permitida para coleta sem itens.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            itemService.deleteColeta(selected.getNome());
            loadColetas(true);
            syncComboSelectionByName(DEFAULT_COLETA);
            refreshItems(false);
        } catch (Exception e) {
            showError("Erro ao excluir coleta: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar para TXT");
        String coleta = getSelectedColeta().replaceAll("[^a-zA-Z0-9_-]", "_");
        fileChooser.setInitialFileName("coleta_" + coleta + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("TXT Files", "*.txt")
        );

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            exportToTxt(file);
        }
    }

    private void exportToTxt(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (ItemResponseDTO item : itemsList) {
                writer.write(String.format("%s;%d%n",
                    safe(item.getCodigoReferencia()),
                    item.getQuantidade() == null ? 0 : item.getQuantidade()
                ));
            }
            showInfo("Exportacao concluida", "Dados exportados com sucesso para: " + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Erro ao exportar: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        ItemDialog dialog = new ItemDialog();
        Optional<ItemRequestDTO> result = showDialogAndPauseAutoRefresh(dialog);
        result.ifPresent(itemRequest -> {
            try {
                if (isSelectedColetaBlocked()) {
                    showError("Coleta bloqueada. Nao e permitido movimentar itens.");
                    return;
                }
                itemRequest.setColeta(getSelectedColeta());
                itemService.createItem(itemRequest);
                refreshItems(false);
            } catch (Exception e) {
                showError("Erro ao adicionar item: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        ItemResponseDTO selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        ItemDialog dialog = new ItemDialog(selected);
        Optional<ItemRequestDTO> result = showDialogAndPauseAutoRefresh(dialog);
        result.ifPresent(itemRequest -> {
            try {
                if (isSelectedColetaBlocked()) {
                    showError("Coleta bloqueada. Nao e permitido movimentar itens.");
                    return;
                }
                itemRequest.setColeta(getSelectedColeta());
                itemService.updateItem(selected.getId(), itemRequest);
                refreshItems(false);
            } catch (Exception e) {
                showError("Erro ao atualizar item: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        ItemResponseDTO selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusao");
        alert.setHeaderText("Deseja realmente excluir este item?");
        alert.setContentText(String.format("Codigo: %s\nDescricao: %s",
            selected.getCodigoReferencia(), selected.getDescricao()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (isSelectedColetaBlocked()) {
                    showError("Coleta bloqueada. Nao e permitido movimentar itens.");
                    return;
                }
                itemService.deleteItem(selected.getId());
                refreshItems(false);
                loadColetas(false);
            } catch (Exception e) {
                showError("Erro ao excluir item: " + e.getMessage());
            }
        }
    }

    private void refreshItems(boolean forceColetasRefresh) {
        if (itemService == null) {
            return;
        }

        Runnable task = () -> {
            try {
                maybeRefreshColetas(forceColetasRefresh);

                Long selectedId = null;
                ItemResponseDTO selected = itemsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectedId = selected.getId();
                }

                String searchTerm = searchField.getText();
                String coleta = getSelectedColeta();
                List<ItemResponseDTO> items;
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    items = itemService.searchItems(searchTerm.trim(), coleta);
                } else {
                    items = itemService.getAllItems(coleta);
                }

                itemsList.setAll(items);
                updateItemCount();
                footerLabel.setText("Ultima atualizacao: " + LocalDateTime.now().format(dateFormatter));

                if (selectedId != null) {
                    Long finalSelectedId = selectedId;
                    items.stream()
                        .filter(item -> finalSelectedId.equals(item.getId()))
                        .findFirst()
                        .ifPresent(item -> itemsTable.getSelectionModel().select(item));
                }
            } catch (Exception e) {
                showError("Erro ao carregar itens: " + e.getMessage());
            }
        };

        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    private void maybeRefreshColetas(boolean force) {
        long now = System.currentTimeMillis();
        if (force || (now - lastColetasLoadAt) > COLETAS_REFRESH_INTERVAL_MS) {
            loadColetas(force);
        }
    }

    private void loadColetas(boolean force) {
        if (itemService == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && (now - lastColetasLoadAt) <= COLETAS_REFRESH_INTERVAL_MS) {
            return;
        }

        List<ColetaResponseDTO> coletas = itemService.getColetas();
        if (coletas.isEmpty()) {
            coletas = List.of(new ColetaResponseDTO(DEFAULT_COLETA, false, 0));
        }

        String selectedName = getSelectedColeta();
        String preferred = configurationManager.getSelectedColeta();
        if (selectedName == null || selectedName.isBlank()) {
            selectedName = preferred;
        }

        loadingColetas = true;
        coletasList.setAll(coletas);
        List<ColetaOption> options = coletas.stream()
            .map(c -> new ColetaOption(c.getNome(), c.isBloqueada()))
            .collect(Collectors.toList());
        coletaComboBox.getItems().setAll(options);
        loadingColetas = false;

        syncComboSelectionByName(selectedName);
        syncTableSelectionByName(getSelectedColeta());
        updateColetaActionButtons();
        lastColetasLoadAt = now;
    }

    private void updateItemCount() {
        countLabel.setText("Total na coleta: " + itemsList.size() + " itens");
    }

    private ColetaResponseDTO getSelectedColetaForControl() {
        ColetaResponseDTO selected = coletasTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        String nome = getSelectedColeta();
        for (ColetaResponseDTO coleta : coletasList) {
            if (coleta.getNome().equalsIgnoreCase(nome)) {
                return coleta;
            }
        }
        return null;
    }

    private void updateColetaActionButtons() {
        ColetaResponseDTO selected = getSelectedColetaForControl();
        if (selected == null) {
            toggleBlockColetaButton.setDisable(true);
            deleteColetaButton.setDisable(true);
            updateItemActionButtons();
            return;
        }

        boolean isDefault = DEFAULT_COLETA.equalsIgnoreCase(selected.getNome());
        toggleBlockColetaButton.setDisable(isDefault);
        toggleBlockColetaButton.setText(selected.isBloqueada() ? "Desbloquear" : "Bloquear");
        deleteColetaButton.setDisable(isDefault);
        updateItemActionButtons();
    }

    private void updateItemActionButtons() {
        boolean blocked = isSelectedColetaBlocked();
        boolean hasSelection = itemsTable.getSelectionModel().getSelectedItem() != null;

        addButton.setDisable(blocked);
        editButton.setDisable(blocked || !hasSelection);
        deleteButton.setDisable(blocked || !hasSelection);
    }

    private boolean isSelectedColetaBlocked() {
        ColetaOption selected = coletaComboBox.getSelectionModel().getSelectedItem();
        return selected != null && selected.bloqueada;
    }

    private void syncComboSelectionByName(String coletaNome) {
        if (coletaNome == null || coletaNome.isBlank()) {
            coletaNome = DEFAULT_COLETA;
        }
        syncingSelection = true;
        for (ColetaOption option : coletaComboBox.getItems()) {
            if (option.nome.equalsIgnoreCase(coletaNome)) {
                coletaComboBox.getSelectionModel().select(option);
                syncingSelection = false;
                return;
            }
        }
        if (!coletaComboBox.getItems().isEmpty()) {
            coletaComboBox.getSelectionModel().select(0);
        }
        syncingSelection = false;
    }

    private void syncTableSelectionByName(String coletaNome) {
        if (coletaNome == null || coletaNome.isBlank()) {
            return;
        }
        syncingSelection = true;
        for (ColetaResponseDTO coleta : coletasList) {
            if (coleta.getNome().equalsIgnoreCase(coletaNome)) {
                coletasTable.getSelectionModel().select(coleta);
                coletaNameField.setText(coleta.getNome());
                syncingSelection = false;
                return;
            }
        }
        syncingSelection = false;
    }

    private Optional<ItemRequestDTO> showDialogAndPauseAutoRefresh(ItemDialog dialog) {
        dialogOpen = true;
        try {
            return dialog.showAndWait();
        } finally {
            dialogOpen = false;
        }
    }

    private String getSelectedColeta() {
        ColetaOption option = coletaComboBox.getSelectionModel().getSelectedItem();
        if (option == null || option.nome == null || option.nome.trim().isEmpty()) {
            return DEFAULT_COLETA;
        }
        return option.nome.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

    private static class ColetaOption {
        private final String nome;
        private final boolean bloqueada;

        private ColetaOption(String nome, boolean bloqueada) {
            this.nome = nome;
            this.bloqueada = bloqueada;
        }

        @Override
        public String toString() {
            return bloqueada ? nome + " [BLOQUEADA]" : nome;
        }
    }
}
