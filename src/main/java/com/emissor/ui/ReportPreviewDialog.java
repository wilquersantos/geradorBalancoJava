package com.emissor.ui;

import com.emissor.dto.ReportItemDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class ReportPreviewDialog {
    
    public static boolean show(List<ReportItemDTO> items, Window owner) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        Stage stage = new Stage();
        stage.setTitle("Preview Importacao do Relatorio");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        
        Label title = new Label("Itens encontrados no relatorio");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Mostrando apenas codigo de referencia, descricao e quantidade.");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        
        TableView<ReportItemDTO> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<ReportItemDTO, String> codigoColumn = new TableColumn<>("Codigo Referencia");
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigoReferencia"));
        
        TableColumn<ReportItemDTO, String> descricaoColumn = new TableColumn<>("Descricao");
        descricaoColumn.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        
        TableColumn<ReportItemDTO, Integer> quantidadeColumn = new TableColumn<>("Quantidade");
        quantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        
        tableView.getColumns().addAll(codigoColumn, descricaoColumn, quantidadeColumn);
        ObservableList<ReportItemDTO> data = FXCollections.observableArrayList(items);
        tableView.setItems(data);
        
        Label countLabel = new Label("Total de itens: " + items.size());
        countLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        Button cancelButton = new Button("Cancelar");
        Button importButton = new Button("Importar");
        importButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        final boolean[] confirmed = {false};
        cancelButton.setOnAction(e -> {
            confirmed[0] = false;
            stage.close();
        });
        importButton.setOnAction(e -> {
            confirmed[0] = true;
            stage.close();
        });
        
        HBox buttons = new HBox(10, cancelButton, importButton);
        buttons.setStyle("-fx-alignment: center-right;");
        
        VBox header = new VBox(5, title, subtitle);
        
        VBox footer = new VBox(8, countLabel, buttons);
        footer.setPadding(new Insets(10, 0, 0, 0));
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setTop(header);
        root.setCenter(tableView);
        root.setBottom(footer);
        
        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);
        stage.showAndWait();
        
        return confirmed[0];
    }
}
