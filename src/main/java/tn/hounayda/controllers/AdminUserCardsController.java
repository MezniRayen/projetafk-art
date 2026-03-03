package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.hounayda.entities.Users;
import tn.hounayda.entities.UserRole;
import tn.hounayda.entities.UserStatut;
import tn.hounayda.services.UserService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUserCardsController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private FlowPane usersGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private Label noUsersLabel;

    private final UserService userService = new UserService();
    private List<Users> allUsers;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn() && Session.getInstance().isAdmin()) {
            String fullName = Session.getInstance().getCurrentUser().getNom() + " " +
                    Session.getInstance().getCurrentUser().getPrenom();
            welcomeLabel.setText(fullName);

            // Initialiser les filtres
            roleFilter.getItems().addAll("Tous", "ADMIN", "USER");
            roleFilter.setValue("Tous");

            statusFilter.getItems().addAll("Tous", "ACTIF", "SUSPENDU", "BANNI");
            statusFilter.setValue("Tous");

            // Listeners pour les filtres
            roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
            statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());

            // Charger les utilisateurs
            loadUsers();

        } else {
            showError("Accès refusé", "Vous devez être administrateur pour accéder à cette page");
            try {
                Thread.sleep(2000);
                redirectToLogin();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUsers() {
        try {
            allUsers = userService.getAllUsers();
            System.out.println("Utilisateurs chargés depuis la base : " + allUsers.size());

            if (allUsers != null && !allUsers.isEmpty()) {
                displayUsers(allUsers);
            } else {
                showError("Erreur", "La liste des utilisateurs est vide");
                noUsersLabel.setVisible(true);
                noUsersLabel.setManaged(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la liste des utilisateurs: " + e.getMessage());
        }
    }

    private void filterUsers() {
        if (allUsers == null || allUsers.isEmpty()) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedRole = roleFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        List<Users> filtered = allUsers.stream()
                .filter(user -> {
                    // Filtre par recherche
                    if (searchText.isEmpty()) return true;
                    return user.getNom().toLowerCase().contains(searchText) ||
                            user.getPrenom().toLowerCase().contains(searchText) ||
                            user.getEmail().toLowerCase().contains(searchText);
                })
                .filter(user -> {
                    // Filtre par rôle
                    if ("Tous".equals(selectedRole)) return true;
                    return user.getRole().name().equals(selectedRole);
                })
                .filter(user -> {
                    // Filtre par statut
                    if ("Tous".equals(selectedStatus)) return true;
                    return user.getStatut().name().equals(selectedStatus);
                })
                .collect(Collectors.toList());

        displayUsers(filtered);
    }

    private void displayUsers(List<Users> users) {
        usersGrid.getChildren().clear();

        if (users == null || users.isEmpty()) {
            noUsersLabel.setVisible(true);
            noUsersLabel.setManaged(true);
            return;
        }

        noUsersLabel.setVisible(false);
        noUsersLabel.setManaged(false);

        for (Users user : users) {
            VBox card = createUserCard(user);
            usersGrid.getChildren().add(card);
        }
    }

    private VBox createUserCard(Users user) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPrefHeight(280); // Réduit car moins de boutons
        card.setPadding(new Insets(15));
        card.getStyleClass().add("user-card");

        // Style selon le statut
        String borderColor;
        switch (user.getStatut()) {
            case ACTIF:
                borderColor = "#27ae60";
                break;
            case SUSPENDU:
                borderColor = "#e67e22";
                break;
            case BANNI:
                borderColor = "#c0392b";
                break;
            default:
                borderColor = "#E38792";
        }
        card.setStyle("-fx-background-color: white; -fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        // En-tête avec avatar et rôle
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar (placeholder)
        Label avatarLabel = new Label("👤");
        avatarLabel.setFont(Font.font(30));
        avatarLabel.setMinWidth(50);
        avatarLabel.setAlignment(Pos.CENTER);

        VBox userInfo = new VBox(5);
        Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);

        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setStyle(user.getRole() == UserRole.ADMIN ?
                "-fx-background-color: #E38792; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;" :
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;");

        userInfo.getChildren().addAll(nameLabel, roleLabel);
        header.getChildren().addAll(avatarLabel, userInfo);

        // Email
        Label emailLabel = new Label("✉️ " + user.getEmail());
        emailLabel.setWrapText(true);
        emailLabel.setStyle("-fx-text-fill: #666;");

        // Statut et vérification
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label(user.getStatut().toString());
        statusLabel.setStyle(getStatusStyle(user.getStatut()));

        Label verifiedLabel = new Label(user.isVerified() ? "✅ Vérifié" : "❌ Non vérifié");
        verifiedLabel.setStyle(user.isVerified() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #c0392b;");

        statusBox.getChildren().addAll(statusLabel, verifiedLabel);

        // Date de création
        Label dateLabel = new Label("📅 Inscrit le: " + (user.getDateCreation() != null ?
                dateFormat.format(user.getDateCreation()) : "N/A"));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

        // Bouton unique pour changer le statut
        Button statusBtn = new Button("🔄 Changer le statut");
        statusBtn.setPrefWidth(200);
        statusBtn.setStyle("-fx-background-color: #E38792; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8;");
        statusBtn.setOnAction(e -> changeUserStatus(user));

        // Désactiver le bouton pour l'admin connecté (ne peut pas changer son propre statut)
        if (user.getIdUser() == Session.getInstance().getCurrentUser().getIdUser()) {
            statusBtn.setDisable(true);
            statusBtn.setStyle("-fx-background-color: #cccccc;");
        }

        card.getChildren().addAll(header, new Separator(), emailLabel, statusBox, dateLabel, statusBtn);
        return card;
    }

    private String getStatusStyle(UserStatut statut) {
        switch (statut) {
            case ACTIF:
                return "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;";
            case SUSPENDU:
                return "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;";
            case BANNI:
                return "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;";
            default:
                return "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;";
        }
    }

    /**
     * Méthode pour changer le statut d'un utilisateur
     */
    private void changeUserStatus(Users user) {
        // Boîte de dialogue pour changer le statut
        ChoiceDialog<UserStatut> dialog = new ChoiceDialog<>(user.getStatut(), UserStatut.values());
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Utilisateur : " + user.getPrenom() + " " + user.getNom());
        dialog.setContentText("Nouveau statut :");

        Optional<UserStatut> result = dialog.showAndWait();
        result.ifPresent(newStatut -> {
            try {
                user.setStatut(newStatut);
                userService.updateUser(user);
                loadUsers(); // Recharger pour mettre à jour l'affichage
                showInfo("Succès", "Statut mis à jour avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de mettre à jour le statut");
            }
        });
    }

    // Méthodes de navigation
    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_management.fxml"));
            Node dashboard = loader.load();

            // Récupérer le BorderPane parent
            BorderPane parentBorderPane = (BorderPane) usersGrid.getScene().getRoot();
            StackPane contentArea = (StackPane) parentBorderPane.getCenter();
            contentArea.getChildren().setAll(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le tableau de bord");
        }
    }

    @FXML
    private void goToUsers() {
        // Déjà sur la page utilisateurs
    }

    @FXML
    private void goToActions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_action_management.fxml"));
            Node actionsView = loader.load();

            BorderPane parentBorderPane = (BorderPane) usersGrid.getScene().getRoot();
            StackPane contentArea = (StackPane) parentBorderPane.getCenter();
            contentArea.getChildren().setAll(actionsView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la gestion des actions");
        }
    }

    @FXML
    private void goToStats() {
        showInfo("Information", "Module Statistiques - En cours de développement");
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Node profileView = loader.load();

            BorderPane parentBorderPane = (BorderPane) usersGrid.getScene().getRoot();
            StackPane contentArea = (StackPane) parentBorderPane.getCenter();
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

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Session.getInstance().logout();
            redirectToLogin();
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}