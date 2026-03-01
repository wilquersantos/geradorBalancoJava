package com.emissor;

import com.emissor.config.ConfigurationManager;
import com.emissor.service.ItemService;
import com.emissor.ui.MainViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class EmissorApplication extends Application {
    
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;
    private MainViewController mainViewController;
    
    public static void main(String[] args) {
        // Iniciar JavaFX Application
        Application.launch(EmissorApplication.class, args);
    }
    
    @Override
    public void init() throws Exception {
        // Aplicar porta persistida antes de subir o Spring Boot
        ConfigurationManager configurationManager = new ConfigurationManager();
        System.setProperty("server.port", String.valueOf(configurationManager.getServerPort()));

        // Iniciar Spring Boot context
        springContext = SpringApplication.run(EmissorApplication.class);
        
        // Carregar FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        rootNode = fxmlLoader.load();
        mainViewController = fxmlLoader.getController();
        
        // Injetar o service do Spring no controller JavaFX
        ItemService itemService = springContext.getBean(ItemService.class);
        mainViewController.setItemService(itemService);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("EmissorJava - Recebimento de Itens");
        
        Scene scene = new Scene(rootNode, 1000, 600);
        String currentStyle = rootNode.getStyle() == null ? "" : rootNode.getStyle();
        rootNode.setStyle(currentStyle + "; -fx-font-size: 14px;");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        
        // Configurar comportamento ao fechar
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            springContext.close();
            System.exit(0);
        });
        
        primaryStage.show();
        
        // Atualizar status e carregar itens iniciais
        Platform.runLater(() -> {
            String serverPort = springContext.getEnvironment().getProperty("server.port", "8084");
            mainViewController.updateStatus("Rodando na porta " + serverPort);
            mainViewController.loadItems();
        });
        
        System.out.println("==============================================");
        System.out.println("EmissorJava iniciado com sucesso!");
        System.out.println("Interface JavaFX: Aberta");
        System.out.println("Servidor REST: http://localhost:" + 
            springContext.getEnvironment().getProperty("server.port", "8084"));
        System.out.println("Token de autenticação: " + 
            springContext.getEnvironment().getProperty("app.security.token"));
        System.out.println("==============================================");
    }
    
    @Override
    public void stop() throws Exception {
        springContext.close();
        Platform.exit();
    }
}
