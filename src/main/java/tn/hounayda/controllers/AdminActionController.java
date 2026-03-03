package tn.hounayda.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.hounayda.entities.AdminAction;
import tn.hounayda.entities.ActionStatus;
import tn.hounayda.services.AdminActionService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AdminActionController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private FlowPane actionsGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> sortFilter;
    @FXML
    private Label noActionsLabel;

    // Formulaire
    @FXML
    private VBox formContainer;
    @FXML
    private Label formTitle;
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
    private Button saveButton;
    @FXML
    private Button updateButton;

    private final AdminActionService actionService = new AdminActionService();
    private List<AdminAction> allActions;
    private AdminAction selectedAction = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn() && Session.getInstance().isAdmin()) {
            String fullName = Session.getInstance().getCurrentUser().getNom() + " " +
                    Session.getInstance().getCurrentUser().getPrenom();
            welcomeLabel.setText(fullName);

            // Initialiser les filtres
            statusFilter.getItems().addAll("Toutes", "VALIDE", "EXPIREE");
            statusFilter.setValue("Toutes");

            sortFilter.getItems().addAll("Date (récent → ancien)", "Date (ancien → récent)", "Nom (A → Z)", "Nom (Z → A)");
            sortFilter.setValue("Date (récent → ancien)");

            // Listeners
            statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndSortActions());
            sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndSortActions());
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSortActions());

            // Charger les actions
            loadActions();

        } else {
            showError("Accès refusé", "Vous devez être administrateur");
        }
    }

    private void loadActions() {
        try {
            allActions = actionService.getAllActions();
            System.out.println("Actions chargées : " + allActions.size());
            filterAndSortActions();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les actions");
        }
    }

    private void filterAndSortActions() {
        if (allActions == null) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        // Filtrage
        List<AdminAction> filtered = allActions.stream()
                .filter(action -> {
                    if (searchText.isEmpty()) return true;
                    return action.getAction().toLowerCase().contains(searchText) ||
                            action.getDescription().toLowerCase().contains(searchText);
                })
                .filter(action -> {
                    if ("Toutes".equals(selectedStatus)) return true;
                    return action.getStatus().name().equals(selectedStatus);
                })
                .collect(Collectors.toList());

        // Tri
        String sortType = sortFilter.getValue();
        if (sortType != null) {
            switch (sortType) {
                case "Date (récent → ancien)":
                    filtered.sort((a1, a2) -> a2.getDateAction().compareTo(a1.getDateAction()));
                    break;
                case "Date (ancien → récent)":
                    filtered.sort((a1, a2) -> a1.getDateAction().compareTo(a2.getDateAction()));
                    break;
                case "Nom (A → Z)":
                    filtered.sort((a1, a2) -> a1.getAction().compareToIgnoreCase(a2.getAction()));
                    break;
                case "Nom (Z → A)":
                    filtered.sort((a1, a2) -> a2.getAction().compareToIgnoreCase(a1.getAction()));
                    break;
            }
        }

        displayActions(filtered);
    }

    private void displayActions(List<AdminAction> actions) {
        actionsGrid.getChildren().clear();

        if (actions == null || actions.isEmpty()) {
            noActionsLabel.setVisible(true);
            noActionsLabel.setManaged(true);
            return;
        }

        noActionsLabel.setVisible(false);
        noActionsLabel.setManaged(false);

        for (AdminAction action : actions) {
            VBox card = createActionCard(action);
            actionsGrid.getChildren().add(card);
        }
    }

    private VBox createActionCard(AdminAction action) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPrefHeight(250);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("action-card");

        // Style selon le statut
        String borderColor = action.getStatus() == ActionStatus.VALIDE ? "#27ae60" : "#c0392b";
        card.setStyle("-fx-background-color: white; -fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(action.getAction());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(action.getStatus().name());
        statusLabel.setStyle(action.getStatus() == ActionStatus.VALIDE ?
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;" :
                "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;");

        header.getChildren().addAll(titleLabel, statusLabel);

        // Description (tronquée)
        String desc = action.getDescription();
        if (desc.length() > 100) {
            desc = desc.substring(0, 97) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #666;");

        // Dates
        VBox datesBox = new VBox(5);
        datesBox.setPadding(new Insets(5, 0, 5, 0));

        Label createdLabel = new Label("📅 Créée: " + dateFormat.format(action.getDateAction()));
        createdLabel.setStyle("-fx-font-size: 11px;");

        Label expiresLabel = new Label("⏰ Expire: " + dateFormat.format(action.getExpirationDate()));
        expiresLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (action.isExpired() ? "#c0392b;" : "#27ae60;"));

        datesBox.getChildren().addAll(createdLabel, expiresLabel);

        // Boutons d'action
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setPrefWidth(90);
        editBtn.setStyle("-fx-background-color: #E38792; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> editAction(action));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setPrefWidth(90);
        deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> deleteAction(action));

        Button viewBtn = new Button("👥 Participants");
        viewBtn.setPrefWidth(100);
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        viewBtn.setOnAction(e -> showParticipants(action));

        actionsBox.getChildren().addAll(editBtn, deleteBtn, viewBtn);

        card.getChildren().addAll(header, new Separator(), descLabel, datesBox, actionsBox);
        return card;
    }

    @FXML
    private void showAddForm() {
        try {
            // Vérifier que le fichier FXML existe
            String fxmlPath = "/views/admin_add_action_card.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                showError("Erreur", "Fichier FXML introuvable: " + fxmlPath);
                return;
            }

            // Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent addActionView = loader.load();

            // Remplacer le contenu de la scène actuelle
            Stage stage = (Stage) actionsGrid.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // Garder les mêmes dimensions
            Scene newScene = new Scene(addActionView, currentScene.getWidth(), currentScene.getHeight());

            // Copier le CSS si nécessaire
            newScene.getStylesheets().addAll(currentScene.getStylesheets());

            stage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le formulaire d'ajout d'action: " + e.getMessage());
        }
    }

    private void editAction(AdminAction action) {
        selectedAction = action;
        formTitle.setText("Modifier l'action");
        actionField.setText(action.getAction());
        descriptionArea.setText(action.getDescription());
        expirationPicker.setValue(action.getExpirationDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        saveButton.setVisible(false);
        saveButton.setManaged(false);
        updateButton.setVisible(true);
        updateButton.setManaged(true);
        formContainer.setVisible(true);
        formContainer.setManaged(true);

        hideAllErrors();
    }

    @FXML
    private void hideForm() {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        clearForm();
        selectedAction = null;
    }

    private void clearForm() {
        actionField.clear();
        descriptionArea.clear();
        expirationPicker.setValue(null);
        hideAllErrors();
    }

    private void hideAllErrors() {
        actionError.setVisible(false);
        actionError.setManaged(false);
        descriptionError.setVisible(false);
        descriptionError.setManaged(false);
        expirationError.setVisible(false);
        expirationError.setManaged(false);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation action
        String action = actionField.getText();
        if (action == null || action.trim().isEmpty()) {
            actionError.setText("Le type d'action est requis");
            actionError.setVisible(true);
            actionError.setManaged(true);
            isValid = false;
        } else if (action.trim().length() < 3) {
            actionError.setText("Minimum 3 caractères");
            actionError.setVisible(true);
            actionError.setManaged(true);
            isValid = false;
        } else {
            actionError.setVisible(false);
            actionError.setManaged(false);
        }

        // Validation description
        String desc = descriptionArea.getText();
        if (desc == null || desc.trim().isEmpty()) {
            descriptionError.setText("La description est requise");
            descriptionError.setVisible(true);
            descriptionError.setManaged(true);
            isValid = false;
        } else if (desc.trim().length() < 10) {
            descriptionError.setText("Minimum 10 caractères");
            descriptionError.setVisible(true);
            descriptionError.setManaged(true);
            isValid = false;
        } else {
            descriptionError.setVisible(false);
            descriptionError.setManaged(false);
        }

        // Validation date
        LocalDate date = expirationPicker.getValue();
        if (date == null) {
            expirationError.setText("La date est requise");
            expirationError.setVisible(true);
            expirationError.setManaged(true);
            isValid = false;
        } else if (date.isBefore(LocalDate.now())) {
            expirationError.setText("La date doit être dans le futur");
            expirationError.setVisible(true);
            expirationError.setManaged(true);
            isValid = false;
        } else {
            expirationError.setVisible(false);
            expirationError.setManaged(false);
        }

        return isValid;
    }

    @FXML
    private void saveAction() {
        if (!validateForm()) return;

        try {
            Date expiration = java.sql.Date.valueOf(expirationPicker.getValue());
            long currentAdminId = Session.getInstance().getCurrentUser().getIdUser();

            AdminAction action = new AdminAction();
            action.setIdAdmin(currentAdminId);
            action.setAction(actionField.getText().trim());
            action.setDescription(descriptionArea.getText().trim());
            action.setNewExpirationDate(expiration);

            actionService.createAction(action);
            loadActions();
            hideForm();
            showInfo("Succès", "Action créée avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Échec de la création: " + e.getMessage());
        }
    }

    @FXML
    private void updateAction() {
        if (selectedAction == null) return;
        if (!validateForm()) return;

        try {
            selectedAction.setAction(actionField.getText().trim());
            selectedAction.setDescription(descriptionArea.getText().trim());
            selectedAction.setExpirationDate(java.sql.Date.valueOf(expirationPicker.getValue()));

            actionService.updateAction(selectedAction);
            loadActions();
            hideForm();
            showInfo("Succès", "Action modifiée avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Échec de la modification");
        }
    }

    private void deleteAction(AdminAction action) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'action");
        confirm.setContentText("Voulez-vous vraiment supprimer : " + action.getAction() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                actionService.deleteAction(action.getIdAction());
                loadActions();
                showInfo("Succès", "Action supprimée avec succès !");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Échec de la suppression");
            }
        }
    }

    private void showParticipants(AdminAction action) {
        // À implémenter avec la logique des participants
        showInfo("Participants", "Liste des participants pour : " + action.getAction() + "\n(Fonctionnalité à venir)");
    }

    // Navigation
    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_management.fxml"));
            Node dashboard = loader.load();
            StackPane contentArea = (StackPane) actionsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_cards_management.fxml"));
            Node usersView = loader.load();
            StackPane contentArea = (StackPane) actionsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(usersView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToActions() {
        // Déjà sur la page
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Node profileView = loader.load();
            StackPane contentArea = (StackPane) actionsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(profileView);
        } catch (IOException e) {
            e.printStackTrace();
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}