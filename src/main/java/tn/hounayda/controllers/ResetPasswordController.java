package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.hounayda.services.PasswordResetService;

import java.io.IOException;

public class ResetPasswordController {

    @FXML
    private VBox step1Box;
    @FXML
    private VBox step2Box;
    @FXML
    private VBox step3Box;

    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label requestStatus;
    @FXML
    private Label codeSentInfo;
    @FXML
    private Label codeStatus;
    @FXML
    private Label resetStatus;
    @FXML
    private Label successMessage;

    private final PasswordResetService resetService = new PasswordResetService();
    private String currentEmail;

    @FXML
    public void initialize() {
        // Initialisation
    }

    @FXML
    private void requestReset() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            requestStatus.setText("Veuillez entrer votre email");
            requestStatus.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        boolean success = resetService.requestResetByEmail(email);

        if (success) {
            currentEmail = email;
            codeSentInfo.setText("Un code a été envoyé à : " + email);

            step1Box.setVisible(false);
            step1Box.setManaged(false);
            step2Box.setVisible(true);
            step2Box.setManaged(true);

            requestStatus.setText("");
        } else {
            requestStatus.setText("Email non trouvé");
            requestStatus.setStyle("-fx-text-fill: #c0392b;");
        }
    }

    @FXML
    private void verifyCode() {
        String code = codeField.getText().trim();

        if (code.isEmpty() || code.length() != 6) {
            codeStatus.setText("Code invalide (6 chiffres requis)");
            codeStatus.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        codeStatus.setText("Code valide !");
        codeStatus.setStyle("-fx-text-fill: #27ae60;");

        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step3Box.setVisible(true);
        step3Box.setManaged(true);
    }

    @FXML
    private void resetPassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();
        String code = codeField.getText().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            resetStatus.setText("Veuillez remplir tous les champs");
            resetStatus.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            resetStatus.setText("Les mots de passe ne correspondent pas");
            resetStatus.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        if (newPass.length() < 8) {
            resetStatus.setText("Le mot de passe doit contenir au moins 8 caractères");
            resetStatus.setStyle("-fx-text-fill: #c0392b;");
            return;
        }

        boolean success = resetService.resetPassword(currentEmail, code, newPass);

        if (success) {
            // Afficher le message de succès
            step3Box.setVisible(false);
            step3Box.setManaged(false);

            successMessage.setVisible(true);
            successMessage.setManaged(true);
            successMessage.setText("✓ Mot de passe réinitialisé avec succès !\nRedirection vers la page de connexion...");

            // Redirection automatique après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            goToLogin();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            resetStatus.setText("Code invalide ou expiré");
            resetStatus.setStyle("-fx-text-fill: #c0392b;");
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion AFK'Art");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion");
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