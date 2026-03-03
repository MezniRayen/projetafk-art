package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.hounayda.entities.Users;
import tn.hounayda.entities.UserRole;
import tn.hounayda.services.UserService;
import tn.hounayda.utils.Session;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        try {
            bundle = ResourceBundle.getBundle("bundles.messages");
            System.out.println("Bundle chargé : " + bundle.getLocale());
        } catch (Exception e) {
            System.err.println("Bundle introuvable → fallback français");
            bundle = ResourceBundle.getBundle("bundles.messages", java.util.Locale.FRENCH);
        }

        emailField.setPromptText(getString("login.email", "Email"));
        passwordField.setPromptText(getString("login.password", "Mot de passe"));
    }

    private String getString(String key, String defaultValue) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @FXML
    private void login() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        System.out.println("=== TENTATIVE DE CONNEXION ===");
        System.out.println("Email saisi: '" + email + "'");
        System.out.println("Mot de passe saisi: '" + password + "'");

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", getString("login.error.empty", "Email et mot de passe requis"));
            return;
        }

        // Récupérer l'utilisateur par email
        Users user = userService.getUserByEmail(email);

        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé avec l'email: " + email);
            showAlert("Erreur", getString("login.error.invalid", "Identifiants incorrects"));
            return;
        }

        // Afficher les infos de l'utilisateur trouvé
        System.out.println("✅ Utilisateur trouvé: " + user.getEmail());
        System.out.println("Hash en base: '" + user.getMotDePasse() + "'");
        System.out.println("Longueur du hash: " + user.getMotDePasse().length());

        // Vérifier que le hash commence bien par $2a$ (format BCrypt)
        if (!user.getMotDePasse().startsWith("$2a$")) {
            System.out.println("⚠️ Attention: Le hash ne semble pas être au format BCrypt!");
        }

        // IMPORTANT: Utiliser BCrypt.checkpw() pour comparer
        boolean passwordMatches = BCrypt.checkpw(password, user.getMotDePasse());
        System.out.println("Résultat BCrypt.checkpw: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("❌ Mot de passe incorrect");

            // Essayons de hasher le mot de passe saisi pour voir ce qu'il donne
            String testHash = BCrypt.hashpw(password, BCrypt.gensalt());
            System.out.println("Nouveau hash du mot de passe saisi: '" + testHash + "'");
            System.out.println("(À titre de comparaison uniquement)");

            showAlert("Erreur", getString("login.error.invalid", "Identifiants incorrects"));
            return;
        }

        System.out.println("✅ Mot de passe correct!");

        // Vérification de connexion suspecte (optionnel)
        if (userService.isSuspiciousLogin(user, "IP_test")) {
            System.out.println("⚠️ Connexion suspecte détectée");
            showAlert("Suspicion", getString("login.error.suspicious", "Connexion suspecte – contactez admin"));
            return;
        }

        // Mettre à jour la date de dernière connexion
        user.setLastLogin(new Date());
        userService.updateUser(user);

        // Connecter l'utilisateur dans la session
        Session.getInstance().login(user);
        System.out.println("✅ Utilisateur connecté avec succès!");

        // Rediriger vers le bon dashboard selon le rôle
        try {
            String fxmlPath;
            String title;

            if (user.getRole() == UserRole.ADMIN) {
                fxmlPath = "/views/admin_management.fxml";
                title = "Administration AFK'Art";
            } else {
                fxmlPath = "/views/user_management.fxml";
                title = "Tableau de bord AFK'Art";
            }

            System.out.println("Redirection vers: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec chargement interface principale");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/register.fxml"));
            Scene scene = new Scene(loader.load(), 600, 600);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription AFK'Art");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'inscription");
        }
    }

    @FXML
    private void goToResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reset_password.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Réinitialisation du mot de passe");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de réinitialisation");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}