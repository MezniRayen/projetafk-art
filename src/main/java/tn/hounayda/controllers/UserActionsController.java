package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.hounayda.entities.AdminAction;
import tn.hounayda.entities.ActionStatus;
import tn.hounayda.services.AdminActionService;
import tn.hounayda.utils.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UserActionsController {

    @FXML
    private FlowPane actionsGrid;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private TextField searchField;
    @FXML
    private Label noActionsLabel;

    private final AdminActionService actionService = new AdminActionService();
    private List<AdminAction> allActions;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation UserActionsController ===");

        // Configuration des filtres
        filterCombo.getSelectionModel().select(0); // "Toutes les actions"

        // Listeners pour les filtres
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterActions());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterActions());

        // Charger les actions
        loadActions();
    }

    private void loadActions() {
        try {
            allActions = actionService.getAllActions();
            displayActions(allActions);
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les actions");
            e.printStackTrace();
        }
    }

    private void filterActions() {
        if (allActions == null) return;

        String filter = filterCombo.getValue();
        String search = searchField.getText().toLowerCase();

        List<AdminAction> filtered = allActions.stream()
                .filter(action -> {
                    // Filtre par statut
                    if ("Valides".equals(filter)) {
                        return action.getStatus() == ActionStatus.VALIDE;
                    } else if ("Expirées".equals(filter)) {
                        return action.getStatus() == ActionStatus.EXPIREE;
                    }
                    return true;
                })
                .filter(action -> {
                    // Filtre par recherche
                    if (search.isEmpty()) return true;
                    return action.getAction().toLowerCase().contains(search) ||
                            action.getDescription().toLowerCase().contains(search);
                })
                .collect(Collectors.toList());

        displayActions(filtered);
    }

    private void displayActions(List<AdminAction> actions) {
        actionsGrid.getChildren().clear();

        if (actions.isEmpty()) {
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
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPrefHeight(220);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("action-card");

        // Style selon le statut
        if (action.getStatus() == ActionStatus.EXPIREE) {
            card.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #ff6b6b;");
        } else {
            card.setStyle("-fx-background-color: white; -fx-border-color: #4d0a0b;");
        }

        // En-tête avec titre et statut
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(action.getAction());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(160);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(action.getStatus().name());
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setPadding(new Insets(3, 8, 3, 8));

        if (action.getStatus() == ActionStatus.VALIDE) {
            statusLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 12;");
        } else {
            statusLabel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 12;");
        }

        header.getChildren().addAll(titleLabel, statusLabel);

        // Description
        Label descLabel = new Label(action.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(80);
        descLabel.setStyle("-fx-text-fill: #666;");

        // Dates
        GridPane datesGrid = new GridPane();
        datesGrid.setHgap(10);
        datesGrid.setVgap(5);

        Label dateLabel = new Label("Créée le:");
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label dateValue = new Label(dateFormat.format(action.getDateAction()));
        dateValue.setStyle("-fx-text-fill: #333;");

        Label expireLabel = new Label("Expire le:");
        expireLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label expireValue = new Label(dateFormat.format(action.getExpirationDate()));
        expireValue.setStyle("-fx-text-fill: #333;");

        datesGrid.add(dateLabel, 0, 0);
        datesGrid.add(dateValue, 1, 0);
        datesGrid.add(expireLabel, 0, 1);
        datesGrid.add(expireValue, 1, 1);

        // Bouton d'action
        Button participateBtn = new Button("Participer");
        participateBtn.setPrefWidth(250);
        participateBtn.setStyle("-fx-background-color: #4d0a0b; -fx-text-fill: white; -fx-font-weight: bold;");
        participateBtn.setOnAction(e -> showParticipationDialog(action));

        if (action.getStatus() == ActionStatus.EXPIREE) {
            participateBtn.setDisable(true);
            participateBtn.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666;");
        }

        card.getChildren().addAll(header, new Separator(), descLabel, datesGrid, participateBtn);
        return card;
    }

    private void showParticipationDialog(AdminAction action) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation de participation");
        dialog.setHeaderText("Participer à : " + action.getAction());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label info = new Label("Voulez-vous vraiment participer à cette action artistique ?");
        info.setWrapText(true);

        Label dateInfo = new Label("Date d'expiration : " + dateFormat.format(action.getExpirationDate()));
        dateInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #4d0a0b;");

        content.getChildren().addAll(info, dateInfo);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Logique de participation (à implémenter)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Participation enregistrée");
                alert.setHeaderText(null);
                alert.setContentText("Votre participation a été enregistrée avec succès !");
                alert.showAndWait();
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}