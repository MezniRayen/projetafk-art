package tn.hounayda.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.hounayda.entities.Users;
import tn.hounayda.services.UserService;
import tn.hounayda.utils.Session;

import java.io.File;
import java.io.IOException;

public class ProfileController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label typeUserLabel; // Nouveau label pour afficher le type (artiste/investisseur)
    @FXML
    private TextField newNameField;
    @FXML
    private TextField newPrenomField;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label photoStatusLabel;
    @FXML
    private ComboBox<String> typeUserCombo; // Pour modifier le type (admin seulement)

    private final UserService userService = new UserService();
    private Users currentUser;

    @FXML
    public void initialize() {
        currentUser = Session.getInstance().getCurrentUser();

        if (!Session.getInstance().isLoggedIn()) {
            showAlert("Erreur", "Vous devez être connecté");
            return;
        }

        // Affiche les infos actuelles
        fullNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText("Rôle : " + currentUser.getRole());
        statusLabel.setText("Statut : " + currentUser.getStatut());

        // Afficher le type d'utilisateur pour le module investissement
        String typeText = "Type : ";
        if (currentUser.getTypeUser() != null) {
            switch (currentUser.getTypeUser()) {
                case "ARTISTE": typeText += "Artiste"; break;
                case "INVESTISSEUR": typeText += "Investisseur"; break;
                case "LES_DEUX": typeText += "Artiste & Investisseur"; break;
                default: typeText += "Non défini";
            }
        } else {
            typeText += "Non défini";
        }

        if (typeUserLabel != null) {
            typeUserLabel.setText(typeText);
        }

        // Si l'utilisateur est admin, afficher le ComboBox pour modifier le type
        if (Session.getInstance().isAdmin() && typeUserCombo != null) {
            typeUserCombo.setVisible(true);
            typeUserCombo.getItems().addAll("ARTISTE", "INVESTISSEUR", "LES_DEUX");
            typeUserCombo.setValue(currentUser.getTypeUser());
        }

        // Charge la photo de profil si elle existe
        loadProfileImage();

        // Pré-remplit les champs de modification
        newNameField.setText(currentUser.getNom());
        newPrenomField.setText(currentUser.getPrenom());

        // Efface le message de statut photo au démarrage
        photoStatusLabel.setText("");
    }

    private void loadProfileImage() {
        if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
            try {
                String imagePath = currentUser.getProfilePicture();
                Image image = null;

                if (imagePath.startsWith("http")) {
                    image = new Image(imagePath, true);
                } else if (imagePath.startsWith("file:")) {
                    image = new Image(imagePath, true);
                } else if (imagePath.startsWith("/")) {
                    image = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    image = new Image(getClass().getResourceAsStream("/" + imagePath));
                }

                if (image != null && !image.isError()) {
                    profileImageView.setImage(image);
                } else {
                    setDefaultImage();
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + e.getMessage());
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-profile.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            profileImageView.setImage(null);
        }
    }

    @FXML
    private void uploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                photoStatusLabel.setText("Upload en cours...");
                photoStatusLabel.setStyle("-fx-text-fill: blue;");

                String url = userService.uploadProfilePicture(currentUser, selectedFile);

                if (url != null) {
                    Image newImage = new Image(selectedFile.toURI().toString());
                    profileImageView.setImage(newImage);

                    photoStatusLabel.setText("✓ Photo mise à jour avec succès !");
                    photoStatusLabel.setStyle("-fx-text-fill: green;");

                    showInfo("Succès", "Photo de profil changée");
                } else {
                    photoStatusLabel.setText("✗ Échec de l'upload");
                    photoStatusLabel.setStyle("-fx-text-fill: red;");
                    showError("Erreur", "Échec de l'upload de la photo");
                }
            } catch (Exception e) {
                photoStatusLabel.setText("✗ Erreur lors de l'upload");
                photoStatusLabel.setStyle("-fx-text-fill: red;");
                showError("Erreur", "Format d'image non supporté");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void saveProfile() {
        try {
            // Mise à jour nom/prénom
            String nouveauNom = newNameField.getText().trim();
            String nouveauPrenom = newPrenomField.getText().trim();

            if (nouveauNom.isEmpty() || nouveauPrenom.isEmpty()) {
                showError("Erreur", "Le nom et le prénom ne peuvent pas être vides");
                return;
            }

            currentUser.setNom(nouveauNom);
            currentUser.setPrenom(nouveauPrenom);

            // Mise à jour du type d'utilisateur (si admin et combo visible)
            if (Session.getInstance().isAdmin() && typeUserCombo != null && typeUserCombo.getValue() != null) {
                currentUser.setTypeUser(typeUserCombo.getValue());
            }

            // Mise à jour mot de passe (si rempli)
            String oldPass = oldPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            boolean passwordChanged = false;

            if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    showError("Erreur", "Tous les champs de mot de passe doivent être remplis");
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    showError("Erreur", "Les nouveaux mots de passe ne correspondent pas");
                    return;
                }

                if (newPass.length() < 8) {
                    showError("Erreur", "Le nouveau mot de passe doit contenir au moins 8 caractères");
                    return;
                }

                passwordChanged = userService.changePassword(
                        currentUser.getIdUser(),
                        oldPass,
                        newPass,
                        confirmPass
                );

                if (!passwordChanged) {
                    showError("Erreur", "Ancien mot de passe incorrect");
                    return;
                }
            }

            userService.updateUser(currentUser);

            fullNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());

            String message = "Profil mis à jour";
            if (passwordChanged) {
                message += " et mot de passe changé";
            }
            showInfo("Succès", message);

            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            Session.getInstance().login(currentUser);

        } catch (IllegalArgumentException e) {
            showError("Erreur de validation", e.getMessage());
        } catch (Exception e) {
            showError("Erreur", "Échec mise à jour profil");
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        Session.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion AFK'Art");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion");
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            String fxmlPath;
            String title;

            if (Session.getInstance().isAdmin()) {
                fxmlPath = "/views/admin_management.fxml";
                title = "Administration AFK'Art";
            } else {
                fxmlPath = "/views/user_management.fxml";
                title = "Tableau de bord AFK'Art";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord");
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}