package tn.hounayda.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.hounayda.entities.AdminAction;
import tn.hounayda.entities.ActionStatus;
import tn.hounayda.entities.Users;
import tn.hounayda.services.AdminActionService;
import tn.hounayda.services.UserService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalActionsLabel;
    @FXML
    private Label activeActionsLabel;
    @FXML
    private ListView<String> recentActionsList;

    private final UserService userService = new UserService();
    private final AdminActionService actionService = new AdminActionService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        if (Session.getInstance().isLoggedIn() && Session.getInstance().isAdmin()) {
            String fullName = Session.getInstance().getCurrentUser().getNom() + " " +
                    Session.getInstance().getCurrentUser().getPrenom();
            welcomeLabel.setText(fullName);

            // Charger les statistiques
            loadDashboardData();
        } else {
            redirectToLogin();
        }
    }

    private void loadDashboardData() {
        try {
            // Statistiques utilisateurs
            List<Users> allUsers = userService.getAllUsers();
            long totalUsers = allUsers != null ? allUsers.size() : 0;
            totalUsersLabel.setText(String.valueOf(totalUsers));

            // Statistiques actions
            List<AdminAction> allActions = actionService.getAllActions();
            long totalActions = allActions != null ? allActions.size() : 0;
            totalActionsLabel.setText(String.valueOf(totalActions));

            // Actions actives (non expirées)
            long activeActions = 0;
            if (allActions != null) {
                activeActions = allActions.stream()
                        .filter(action -> action.getStatus() == ActionStatus.VALIDE)
                        .count();
            }
            activeActionsLabel.setText(String.valueOf(activeActions));

            // Actions récentes (5 dernières)
            recentActionsList.getItems().clear();
            if (allActions != null && !allActions.isEmpty()) {
                allActions.stream()
                        .sorted((a1, a2) -> a2.getDateAction().compareTo(a1.getDateAction()))
                        .limit(5)
                        .forEach(action -> {
                            String status = action.getStatus() == ActionStatus.VALIDE ? "✅" : "❌";
                            recentActionsList.getItems().add(
                                    status + " " + action.getAction() +
                                            " (" + dateFormat.format(action.getDateAction()) + ")"
                            );
                        });
            } else {
                recentActionsList.getItems().add("Aucune action récente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les données du dashboard");
        }
    }

    @FXML
    private void goToDashboard() {
        loadDashboardData();
    }

    @FXML
    private void goToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_cards_management.fxml"));
            Node usersView = loader.load();
            contentArea.getChildren().setAll(usersView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la gestion des utilisateurs");
        }
    }

    @FXML
    private void goToActions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_action_cards.fxml"));
            Node actionsView = loader.load();
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

    public void gotoNewUser(ActionEvent actionEvent) {  try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_add_action_card.fxml"));
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