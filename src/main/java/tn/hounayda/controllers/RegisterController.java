package tn.hounayda.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.hounayda.entities.Users;
import tn.hounayda.entities.UserRole;
import tn.hounayda.entities.UserStatut;
import tn.hounayda.services.EmailService;
import tn.hounayda.services.UserService;
import tn.hounayda.services.VerificationService;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    @FXML
    private void register() {

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if(nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()){
            showAlert("Erreur","Tous les champs sont obligatoires");
            return;
        }

        if(!password.equals(confirm)){
            showAlert("Erreur","Les mots de passe ne correspondent pas");
            return;
        }

        try{
            if(userService.getUserByEmail(email) != null){
                showAlert("Erreur","Email déjà utilisé");
                return;
            }

            Users user = new Users(
                    nom,
                    prenom,
                    email,
                    password,
                    UserRole.USER
            );

            user.setStatut(UserStatut.ACTIF);
            user.setVerified(false);

            userService.createUser(user);

// Générer le token et envoyer l'email
            VerificationService verificationService = new VerificationService();
            String token = verificationService.generateTokenForUser(user);

// Tu peux passer le token dans l'email (modifie EmailService en conséquence)
            boolean emailSent = emailService.sendWelcomeEmail(user, token);

            if (emailSent) {
                showInfo("Succès",
                        "Compte créé avec succès !\n" +
                                "Un email de vérification a été envoyé à : " + email + "\n" +
                                "Cliquez sur le lien dans l'email pour activer votre compte.");
            } else {
                showInfo("Succès",
                        "Compte créé avec succès !\n" +
                                "(L'email de vérification n'a pas pu être envoyé)");
            }

            goToLogin();

        }catch(Exception e){
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion AFK'Art");
            stage.show();
        }catch(IOException e){
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion");
        }
    }

    private void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}