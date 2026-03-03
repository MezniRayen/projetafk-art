package tn.hounayda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Démarrage de AFK'Art - Gestion...");

        // 1. Charge le FXML principal
        // Chemin corrigé : depuis src/main/resources/views/user_management.fxml
        // Si ton FXML est ailleurs, ajuste seulement ici
        String fxmlPath = "/views/login.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxmlPath));

        if (fxmlLoader.getLocation() == null) {
            System.err.println("ERREUR : FXML NON TROUVÉ !");
            System.err.println("Vérifie que le fichier existe exactement ici :");
            System.err.println("→ src/main/resources/views/user_managementUser.fxml");
            return;
        }

        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);

        // 2. Charge le CSS
        String cssPath = "/css/art-style.css";
        var cssResource = MainApp.class.getResource(cssPath);

        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
            System.out.println("CSS chargé avec succès : " + cssResource.toExternalForm());
        } else {
            System.err.println("ERREUR : CSS NON TROUVÉ !");
            System.err.println("Vérifie que le fichier existe exactement ici :");
            System.err.println("→ src/main/resources/css/art-style.css");
        }

        // 3. Configuration de la fenêtre
        stage.setTitle("AFK'Art - Gestion des Utilisateurs & Actions Admin");
        stage.setScene(scene);
        stage.setResizable(true);

        // Option : maximiser pour la présentation
        // stage.setMaximized(true);

        stage.show();
        System.out.println("Fenêtre AFK'Art ouverte avec succès !");
    }

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("  AFK'Art - Application de gestion");
        System.out.println("  Projet PI JAVA - Esprit - 2026");
        System.out.println("  Démarrage...");
        System.out.println("======================================\n");

        launch(args);
    }
}