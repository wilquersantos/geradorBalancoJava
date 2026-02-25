package com.emissor.ui;

import com.emissor.dto.ItemRequestDTO;
import com.emissor.dto.ItemResponseDTO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ItemDialog extends Dialog<ItemRequestDTO> {
    
    private TextField codigoField;
    private TextField quantidadeField;
    private TextArea descricaoField;
    
    public ItemDialog() {
        this(null);
    }
    
    public ItemDialog(ItemResponseDTO existingItem) {
        setTitle(existingItem == null ? "Adicionar Item" : "Editar Item");
        setHeaderText(existingItem == null ? "Preencha os dados do novo item" : "Edite os dados do item");
        
        // Botões
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Criar campos
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        codigoField = new TextField();
        codigoField.setPromptText("Código de Referência");
        
        quantidadeField = new TextField();
        quantidadeField.setPromptText("Quantidade");
        
        descricaoField = new TextArea();
        descricaoField.setPromptText("Descrição do item");
        descricaoField.setPrefRowCount(3);
        
        // Preencher se for edição
        if (existingItem != null) {
            codigoField.setText(existingItem.getCodigoReferencia());
            quantidadeField.setText(String.valueOf(existingItem.getQuantidade()));
            descricaoField.setText(existingItem.getDescricao());
        }
        
        grid.add(new Label("Código:"), 0, 0);
        grid.add(codigoField, 1, 0);
        grid.add(new Label("Quantidade:"), 0, 1);
        grid.add(quantidadeField, 1, 1);
        grid.add(new Label("Descrição:"), 0, 2);
        grid.add(descricaoField, 1, 2);
        
        getDialogPane().setContent(grid);
        
        // Validação
        javafx.scene.Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Habilitar/desabilitar botão salvar
        codigoField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || 
                                 quantidadeField.getText().trim().isEmpty() ||
                                 descricaoField.getText().trim().isEmpty());
        });
        
        quantidadeField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(codigoField.getText().trim().isEmpty() || 
                                 newValue.trim().isEmpty() ||
                                 descricaoField.getText().trim().isEmpty());
        });
        
        descricaoField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(codigoField.getText().trim().isEmpty() || 
                                 quantidadeField.getText().trim().isEmpty() ||
                                 newValue.trim().isEmpty());
        });
        
        // Converter resultado
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Integer quantidade = Integer.parseInt(quantidadeField.getText());
                    if (quantidade <= 0) {
                        showError("Quantidade deve ser maior que zero");
                        return null;
                    }
                    return new ItemRequestDTO(
                        codigoField.getText().trim(),
                        quantidade,
                        descricaoField.getText().trim()
                    );
                } catch (NumberFormatException e) {
                    showError("Quantidade deve ser um número válido");
                    return null;
                }
            }
            return null;
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
