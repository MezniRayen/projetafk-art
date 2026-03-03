package tn.hounayda.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.hounayda.services.UserService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userTypeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private ListView<String> recentActionsList;

    @FXML
    private Button dynamicButton;
    @FXML
    private Button financementButton;
    @FXML
    private Button btnSetRoleType;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn()) {
            var currentUser = Session.getInstance().getCurrentUser();
            String fullName = currentUser.getNom() + " " + currentUser.getPrenom();
            welcomeLabel.setText(fullName);

            // Afficher le type d'utilisateur
            updateUserTypeDisplay();

            // Configurer les boutons selon le type
            configureButtonsByType();

            loadRecentActions();
        } else {
            redirectToLogin();
        }
    }

    private void updateUserTypeDisplay() {
        var currentUser = Session.getInstance().getCurrentUser();
        String typeText = "Utilisateur";

        if (currentUser.getTypeUser() != null) {
            switch (currentUser.getTypeUser()) {
                case "ARTISTE":
                    typeText = "Artiste";
                    break;
                case "INVESTISSEUR":
                    typeText = "Investisseur";
                    break;
                case "LES_DEUX":
                    typeText = "Artiste & Investisseur";
                    break;
                default:
                    typeText = "Non défini";
            }
        } else {
            typeText = "Non défini";
        }

        if (userTypeLabel != null) {
            userTypeLabel.setText("Type: " + typeText);
        }
    }

    private void configureButtonsByType() {
        if (dynamicButton == null) {
            System.err.println("⚠️ dynamicButton est null dans le FXML");
            return;
        }

        var currentUser = Session.getInstance().getCurrentUser();
        String typeUser = currentUser.getTypeUser();

        // Par défaut, masquer le bouton financement
        if (financementButton != null) {
            financementButton.setVisible(false);
            financementButton.setManaged(false);
        }

        if (typeUser != null) {
            switch (typeUser) {
                case "ARTISTE":
                    dynamicButton.setText("🎨 Mes Projets");
                    dynamicButton.setOnAction(e -> goToMesProjets());
                    break;

                case "INVESTISSEUR":
                    dynamicButton.setText("💰 Mes Investissements");
                    dynamicButton.setOnAction(e -> goToMesInvestissements());
                    break;

                case "LES_DEUX":
                    dynamicButton.setText("🎨 Mes Projets");
                    dynamicButton.setOnAction(e -> goToMesProjets());

                    if (financementButton != null) {
                        financementButton.setText("💰 Mes Investissements");
                        financementButton.setOnAction(e -> goToMesInvestissements());
                        financementButton.setVisible(true);
                        financementButton.setManaged(true);
                    }
                    break;

                default:
                    dynamicButton.setText("⭐ Définir mon rôle");
                    dynamicButton.setOnAction(e -> showRoleTypeDialog());
            }
        } else {
            dynamicButton.setText("⭐ Définir mon rôle");
            dynamicButton.setOnAction(e -> showRoleTypeDialog());
        }
    }

    @FXML
    private void showRoleTypeDialog() {
        // Utiliser Dialog<ButtonType> au lieu de Dialog<String>
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Choisir votre rôle");
        dialog.setHeaderText("Comment souhaitez-vous utiliser la plateforme ?");

        // Boutons
        ButtonType artisteBtn = new ButtonType("Artiste", ButtonBar.ButtonData.OK_DONE);
        ButtonType investisseurBtn = new ButtonType("Investisseur", ButtonBar.ButtonData.OK_DONE);
        ButtonType lesDeuxBtn = new ButtonType("Les deux", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(artisteBtn, investisseurBtn, lesDeuxBtn, annulerBtn);

        // Contenu explicatif
        Label infoLabel = new Label(
                "🎨 Artiste : Créez et gérez vos projets artistiques\n" +
                        "💰 Investisseur : Investissez dans des projets et suivez vos investissements\n" +
                        "👥 Les deux : Profitez des deux fonctionnalités"
        );
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-padding: 20; -fx-line-spacing: 5;");

        dialog.getDialogPane().setContent(infoLabel);
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMinHeight(200);

        // Traitement du résultat - Maintenant Optional<ButtonType>
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedType = null;
            if (result.get() == artisteBtn) {
                selectedType = "ARTISTE";
            } else if (result.get() == investisseurBtn) {
                selectedType = "INVESTISSEUR";
            } else if (result.get() == lesDeuxBtn) {
                selectedType = "LES_DEUX";
            } else {
                return; // Annulation
            }

            // Sauvegarder le type
            saveUserType(selectedType);
        }
    }

    private void saveUserType(String type) {
        var currentUser = Session.getInstance().getCurrentUser();

        // Mettre à jour l'objet en mémoire
        currentUser.setTypeUser(type);

        // Mettre à jour dans la base de données
        boolean success = updateUserTypeInDatabase(currentUser.getIdUser(), type);

        if (success) {
            // Mettre à jour l'affichage
            updateUserTypeDisplay();
            configureButtonsByType();

            showInfo("Succès", "Votre rôle a été défini avec succès !");
        } else {
            showError("Erreur", "Impossible de sauvegarder votre choix. Veuillez réessayer.");
        }
    }

    private boolean updateUserTypeInDatabase(long userId, String typeUser) {
        String sql = "UPDATE users SET type_user = ? WHERE idUser = ?";
        try (Connection conn = tn.hounayda.utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeUser);
            pstmt.setLong(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadRecentActions() {
        if (recentActionsList != null) {
            recentActionsList.getItems().addAll(
                    "Exposition de peinture - Nouvelle",
                    "Atelier de sculpture - En cours",
                    "Concert de jazz - À venir"
            );
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_management.fxml"));
            Node dashboardView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(dashboardView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger l'interface principale");
        }
    }

    @FXML
    private void goToArtisticActions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_actions.fxml"));
            Node actionsView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(actionsView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les actions artistiques");
        }
    }

    @FXML
    private void goToMesProjets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GestionInvestissment/UIController.fxml"));
            Node investView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(investView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos projets");
        }
    }

    @FXML
    private void goToMesInvestissements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GestionInvestissment/UIController.fxml"));
            Node investView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(investView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos investissements");
        }
    }

    @FXML
    private void goToInvestissement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GestionInvestissment/UIController.fxml"));
            Node investView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(investView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le module de financement");
        }
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Node profileView = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(profileView);
            }
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

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Session.getInstance().logout();
            redirectToLogin();
        }
    }

    @FXML
    private void goToWorks() {
        goToArtisticActions();
    }

    @FXML
    private void handleDynamicButton(ActionEvent event) {
        // Cette méthode est vide car l'action est définie dynamiquement
        // Mais elle doit exister pour éviter les erreurs FXML
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
}