package tn.hounayda.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.hounayda.utils.Session;

import java.io.IOException;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private ListView<String> recentActionsList; // À connecter avec vos données

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn()) {
            String fullName = Session.getInstance().getCurrentUser().getNom() + " " +
                    Session.getInstance().getCurrentUser().getPrenom();
            welcomeLabel.setText(fullName);

            // Charger les actions récentes
            loadRecentActions();
        } else {
            redirectToLogin();
        }
    }

    private void loadRecentActions() {
        // À implémenter avec votre logique métier
        // Exemple temporaire :
        recentActionsList.getItems().addAll(
                "Exposition de peinture - Nouvelle",
                "Atelier de sculpture - En cours",
                "Concert de jazz - À venir"
        );
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_management.fxml"));
            Node actionsView = loader.load();
            contentArea.getChildren().setAll(actionsView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les actions artistiques");
        }
    }

    @FXML
    private void goToArtisticActions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_actions.fxml"));
            Node actionsView = loader.load();
            contentArea.getChildren().setAll(actionsView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les actions artistiques");
        }
    }

    @FXML
    private void goToFavorites() {
        showInfo("Information", "Module Mes Favoris - En cours de développement");
    }

    @FXML
    private void goToParticipations() {
        showInfo("Information", "Module Mes Participations - En cours de développement");
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Node profileView = loader.load();
            contentArea.getChildren().setAll(profileView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le profil");
        }
    }

    @FXML
    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            Session.getInstance().logout();
            redirectToLogin();
        }
    }

    @FXML
    private void goToWorks() {
        // Redirige vers les actions artistiques
        goToArtisticActions();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion AFK'Art");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goToWorks(ActionEvent actionEvent) {
        goToWorks();
    }
}