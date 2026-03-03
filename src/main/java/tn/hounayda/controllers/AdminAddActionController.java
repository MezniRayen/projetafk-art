package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.hounayda.entities.AdminAction;
import tn.hounayda.services.AdminActionService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class AdminAddActionController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField actionField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private DatePicker expirationPicker;
    @FXML
    private Label actionError;
    @FXML
    private Label descriptionError;
    @FXML
    private Label expirationError;
    @FXML
    private Label previewAction;
    @FXML
    private Label previewDate;

    private final AdminActionService actionService = new AdminActionService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn() && Session.getInstance().isAdmin()) {
            String fullName = Session.getInstance().getCurrentUser().getNom() + " " +
                    Session.getInstance().getCurrentUser().getPrenom();
            welcomeLabel.setText(fullName);

            // Validation en temps réel
            actionField.textProperty().addListener((obs, old, newVal) -> {
                validateAction();
                updatePreview();
            });

            descriptionArea.textProperty().addListener((obs, old, newVal) -> {
                validateDescription();
            });

            expirationPicker.valueProperty().addListener((obs, old, newVal) -> {
                validateExpiration();
                updatePreview();
            });
        }
        System.out.println("Chemin absolu du FXML: " + getClass().getResource("/views/admin_action_management.fxml"));
        if (getClass().getResource("/views/admin_action_cards.fxml") == null) {
            System.err.println("ERREUR: Fichier admin_action_cards.fxml introuvable dans resources/views/");
        }
    }

    private void validateAction() {
        String action = actionField.getText();
        if (action == null || action.trim().isEmpty()) {
            showError(actionError, "Le type d'action est requis");
        } else if (action.trim().length() < 3) {
            showError(actionError, "Minimum 3 caractères");
        } else if (action.trim().length() > 100) {
            showError(actionError, "Maximum 100 caractères");
        } else {
            hideError(actionError);
        }
    }

    private void validateDescription() {
        String desc = descriptionArea.getText();
        if (desc == null || desc.trim().isEmpty()) {
            showError(descriptionError, "La description est requise");
        } else if (desc.trim().length() < 10) {
            showError(descriptionError, "Minimum 10 caractères");
        } else if (desc.trim().length() > 500) {
            showError(descriptionError, "Maximum 500 caractères");
        } else {
            hideError(descriptionError);
        }
    }

    private void validateExpiration() {
        LocalDate date = expirationPicker.getValue();
        if (date == null) {
            showError(expirationError, "La date d'expiration est requise");
        } else if (date.isBefore(LocalDate.now())) {
            showError(expirationError, "La date doit être dans le futur");
        } else {
            hideError(expirationError);
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void updatePreview() {
        String action = actionField.getText();
        previewAction.setText(action != null && !action.trim().isEmpty() ? action : "-");

        LocalDate date = expirationPicker.getValue();
        previewDate.setText(date != null ? date.format(dateFormatter) : "-");
    }

    private boolean isFormValid() {
        validateAction();
        validateDescription();
        validateExpiration();

        return !actionError.isVisible() &&
                !descriptionError.isVisible() &&
                !expirationError.isVisible();
    }

    @FXML
    private void saveAction() {
        if (!isFormValid()) {
            showAlert("Formulaire incomplet", "Veuillez corriger les erreurs", Alert.AlertType.WARNING);
            return;
        }

        try {
            Date expiration = java.sql.Date.valueOf(expirationPicker.getValue());
            long currentAdminId = Session.getInstance().getCurrentUser().getIdUser();

            AdminAction action = new AdminAction();
            action.setIdAdmin(currentAdminId);
            action.setAction(actionField.getText().trim());
            action.setDescription(descriptionArea.getText().trim());
            action.setNewExpirationDate(expiration);

            actionService.createAction(action);

            showAlert("Succès", "Action créée avec succès !", Alert.AlertType.INFORMATION);

            // Rediriger vers la liste des actions
            navigateTo("/views/admin_action_management.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de la création: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment annuler ? Les données saisies seront perdues.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            navigateTo("/views/admin_action_cards.fxml");
        }
    }

    /**
     * Méthode générique de navigation
     */
    private void navigateTo(String fxmlPath) {
        try {
            System.out.println("Navigation vers: " + fxmlPath);

            // Vérifier si la ressource existe
            if (getClass().getResource(fxmlPath) == null) {
                showAlert("Erreur", "Fichier introuvable: " + fxmlPath, Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Méthode 1: Chercher le BorderPane parent et son centre
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                Parent root = welcomeLabel.getScene().getRoot();
                if (root instanceof BorderPane) {
                    BorderPane borderPane = (BorderPane) root;
                    borderPane.setCenter(view);
                    return;
                }
            }

            // Méthode 2: Chercher un StackPane avec fx:id="contentArea" dans la scène
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                StackPane contentArea = (StackPane) welcomeLabel.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(view);
                    return;
                }
            }

            // Méthode 3: Fallback - changer toute la scène
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene((Parent) view, 1280, 800);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger: " + fxmlPath + "\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToDashboard() {
        navigateTo("/views/admin_management.fxml");
    }

    @FXML
    private void goToUsers() {
        navigateTo("/views/user_cards_management.fxml");
    }

    @FXML
    private void goToActions() {
        navigateTo("/views/admin_action_cards.fxml");
    }

    @FXML
    private void openProfile() {
        navigateTo("/views/profile.fxml");
    }

    @FXML
    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Session.getInstance().logout();
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
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}