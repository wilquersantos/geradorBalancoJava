package com.emissor.ui;

import com.emissor.config.ConfigurationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SettingsViewController {
    private static SettingsViewController instance;

    @FXML private TextField ipTextField;
    @FXML private TextField portTextField;
    @FXML private Button saveButton;
    @FXML private Label statusMessageLabel;
    @FXML private CheckBox autoUpdateCheckBox;
    @FXML private TextField intervalTextField;
    @FXML private Button manualUpdateButton;
    @FXML private Label updateStatusLabel;

    private ConfigurationManager configManager;
    private AutoUpdateListener autoUpdateListener;
    private Runnable manualUpdateListener;

    public static SettingsViewController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        configManager = new ConfigurationManager();

        ipTextField.setEditable(false);
        ipTextField.setStyle("-fx-opacity: 1.0;");

        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(oldValue);
            }
        });

        saveButton.setOnAction(event -> handleSave());
        manualUpdateButton.setOnAction(e -> {
            if (manualUpdateListener != null) {
                manualUpdateListener.run();
            }
            updateStatusLabel.setText("Lista atualizada manualmente");
        });

        autoUpdateCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> notifyAutoUpdateListener());
        intervalTextField.textProperty().addListener((obs, oldVal, newVal) -> notifyAutoUpdateListener());

        loadCurrentConfiguration();
        statusMessageLabel.setText("");
    }

    public interface AutoUpdateListener {
        void onAutoUpdateChanged(boolean enabled, int intervalSeconds);
    }

    public void setAutoUpdateListener(AutoUpdateListener listener) {
        this.autoUpdateListener = listener;
        notifyAutoUpdateListener();
    }

    public void setManualUpdateListener(Runnable listener) {
        this.manualUpdateListener = listener;
    }

    private void notifyAutoUpdateListener() {
        boolean enabled = autoUpdateCheckBox.isSelected();
        int interval = parseInterval();

        configManager.setAutoUpdateEnabled(enabled);
        configManager.setAutoUpdateInterval(interval);
        try {
            configManager.saveConfiguration();
        } catch (IOException ignored) {
        }

        if (autoUpdateListener != null) {
            autoUpdateListener.onAutoUpdateChanged(enabled, interval);
        }
    }

    private int parseInterval() {
        try {
            return Math.max(1, Integer.parseInt(intervalTextField.getText().trim()));
        } catch (Exception ignored) {
            return 5;
        }
    }

    private void loadCurrentConfiguration() {
        Platform.runLater(() -> {
            configManager.refreshServerIp();
            ipTextField.setText(configManager.getServerIp());
            portTextField.setText(String.valueOf(configManager.getServerPort()));
            autoUpdateCheckBox.setSelected(configManager.isAutoUpdateEnabled());
            intervalTextField.setText(String.valueOf(configManager.getAutoUpdateInterval()));
            notifyAutoUpdateListener();
        });
    }

    @FXML
    private void handleSave() {
        try {
            String portText = portTextField.getText().trim();
            if (portText.isEmpty()) {
                showError("Por favor, informe a porta do servidor.");
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                showError("Porta deve ser um numero valido.");
                return;
            }

            if (port < 1024 || port > 65535) {
                showError("Porta deve estar entre 1024 e 65535.");
                return;
            }

            configManager.setServerPort(port);
            configManager.setAutoUpdateEnabled(autoUpdateCheckBox.isSelected());
            configManager.setAutoUpdateInterval(parseInterval());
            configManager.refreshServerIp();
            configManager.saveConfiguration();

            showSuccess("Configuracoes salvas com sucesso! Reinicie o app para aplicar nova porta.");
            loadCurrentConfiguration();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Erro ao salvar configuracoes: " + e.getMessage());
        }
    }

    private void showError(String message) {
        statusMessageLabel.setText("Erro: " + message);
        statusMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        statusMessageLabel.setText(message);
        statusMessageLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
    }
}
