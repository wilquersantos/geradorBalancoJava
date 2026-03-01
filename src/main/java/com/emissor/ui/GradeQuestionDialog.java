package com.emissor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GradeQuestionDialog {
    
    private static boolean result = false;
    
    /**
     * Shows a dialog asking if the report contains grade items
     * @return true if user clicked "SIM", false if clicked "NÃO"
     */
    public static boolean show() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Importação com Grades");
        stage.setWidth(400);
        stage.setHeight(180);
        stage.setResizable(false);
        
        result = false;
        
        // Title
        Label titleLabel = new Label("Importação de Relatório");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Question
        Label questionLabel = new Label("O relatório contém itens de grade?\n\n" +
                "Se SIM: o código da grade (antes do '-') será usado como referência\n" +
                "Se NÃO: a coluna de referência padrão será usada");
        questionLabel.setStyle("-fx-font-size: 11px; -fx-text-alignment: center; -fx-wrap-text: true;");
        questionLabel.setWrapText(true);
        
        // Buttons
        Button yesButton = new Button("SIM");
        yesButton.setPrefWidth(80);
        yesButton.setStyle("-fx-font-size: 11px;");
        yesButton.setOnAction(e -> {
            result = true;
            stage.close();
        });
        
        Button noButton = new Button("NÃO");
        noButton.setPrefWidth(80);
        noButton.setStyle("-fx-font-size: 11px;");
        noButton.setOnAction(e -> {
            result = false;
            stage.close();
        });
        
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(yesButton, noButton);
        
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.getChildren().addAll(titleLabel, questionLabel, buttonBox);
        
        Scene scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.showAndWait();
        
        return result;
    }
}
