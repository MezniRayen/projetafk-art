package tn.hounayda.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.hounayda.entities.Users;
import tn.hounayda.entities.UserRole;
import tn.hounayda.entities.UserStatut;
import tn.hounayda.services.UserService;

import java.io.IOException;
import java.util.List;

public class UserController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<UserRole> roleCombo;
    @FXML private ComboBox<UserStatut> statutCombo;
    @FXML private CheckBox verifiedCheck;
    @FXML private TextField profilePictureField;
    @FXML private TableView<Users> userTable;
    @FXML private TableColumn<Users, String> nomCol;
    @FXML private TableColumn<Users, String> prenomCol;
    @FXML private TableColumn<Users, String> emailCol;
    @FXML private TableColumn<Users, UserRole> roleCol;
    @FXML private TableColumn<Users, UserStatut> statutCol;

    // Labels d'erreur
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label roleError;
    @FXML private Label statutError;
    @FXML private Label photoError;

    private final UserService userService = new UserService();
    private ObservableList<Users> userList = FXCollections.observableArrayList();

    // Extensions d'images autorisées
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation UserController ===");

        // Configuration des ComboBox
        roleCombo.setItems(FXCollections.observableArrayList(UserRole.values()));
        statutCombo.setItems(FXCollections.observableArrayList(UserStatut.values()));

        // Configuration des colonnes
        configurerColonnes();

        // Lier la liste observable au TableView
        userTable.setItems(userList);

        // Initialiser les validateurs dynamiques
        initialiserValidateurs();

        // Chargement des utilisateurs
        loadUsers();

        System.out.println("=== Initialisation terminée ===");
    }

    private void configurerColonnes() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage visuel pour le statut avec 3 couleurs distinctes
        statutCol.setCellFactory(column -> new TableCell<Users, UserStatut>() {
            @Override
            protected void updateItem(UserStatut item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case ACTIF:
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Vert
                            break;
                        case BANNI:
                            setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); // Rouge brique
                            break;
                        case SUSPENDU:
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Orange
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;"); // Gris
                    }
                }
            }
        });

        // Listener pour la sélection
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectUser();
            }
        });
    }

    private void initialiserValidateurs() {
        // Validation en temps réel pour chaque champ
        nomField.textProperty().addListener((observable, oldValue, newValue) -> validateNom());
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> validatePrenom());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validatePassword());
        profilePictureField.textProperty().addListener((observable, oldValue, newValue) -> validatePhoto());

        // Validation pour les ComboBox
        roleCombo.valueProperty().addListener((observable, oldValue, newValue) -> validateRole());
        statutCombo.valueProperty().addListener((observable, oldValue, newValue) -> validateStatut());
    }

    private void validateNom() {
        String nom = nomField.getText();
        if (nom == null || nom.trim().isEmpty()) {
            showError(nomError, "Le nom est requis");
        } else if (nom.trim().length() < 2) {
            showError(nomError, "Le nom doit contenir au moins 2 caractères");
        } else {
            hideError(nomError);
        }
    }
    @FXML
    private void goToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_management.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = new Stage();
            stage.setTitle("Gestion Utilisateurs");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToActions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_action_management.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = new Stage();
            stage.setTitle("Actions Admin");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void validatePrenom() {
        String prenom = prenomField.getText();
        if (prenom == null || prenom.trim().isEmpty()) {
            showError(prenomError, "Le prénom est requis");
        } else if (prenom.trim().length() < 2) {
            showError(prenomError, "Le prénom doit contenir au moins 2 caractères");
        } else {
            hideError(prenomError);
        }
    }
    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = new Stage();
            stage.setTitle("Mon Profil");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void validateEmail() {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            showError(emailError, "L'email est requis");
        } else if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$")) {
            showError(emailError, "Format d'email invalide");
        } else {
            hideError(emailError);
        }
    }

    private void validatePassword() {
        String password = passwordField.getText();
        if (password == null || password.trim().isEmpty()) {
            showError(passwordError, "Le mot de passe est requis");
        } else if (password.length() < 8) {
            showError(passwordError, "Le mot de passe doit contenir au moins 8 caractères");
        } else if (password.contains(" ")) {
            showError(passwordError, "Le mot de passe ne doit pas contenir d'espaces");
        } else {
            hideError(passwordError);
        }
    }

    private void validateRole() {
        if (roleCombo.getValue() == null) {
            showError(roleError, "Le rôle est requis");
        } else {
            hideError(roleError);
        }
    }

    private void validateStatut() {
        if (statutCombo.getValue() == null) {
            showError(statutError, "Le statut est requis");
        } else {
            hideError(statutError);
        }
    }
    private void validatePhoto() {
        String photo = profilePictureField.getText();

        // Si le champ est vide, c'est valide (optionnel)
        if (photo == null || photo.trim().isEmpty()) {
            hideError(photoError);
            return;
        }

        String trimmedPhoto = photo.trim();

        // Vérifier les caractères dangereux
        if (trimmedPhoto.contains("..") || trimmedPhoto.contains("//") || trimmedPhoto.contains("\\\\")) {
            showError(photoError, "Chemin d'image invalide (caractères non autorisés)");
            return;
        }

        // Pattern pour URL d'image (optionnel mais plus précis)
        String urlPattern = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$";
        String filePattern = "^[^\\s]+\\.(jpg|jpeg|png|gif|bmp)$";

        if (trimmedPhoto.matches(urlPattern) || trimmedPhoto.matches(filePattern)) {
            hideError(photoError);
        } else {
            // Analyse détaillée pour message d'erreur précis
            if (!trimmedPhoto.matches(".*\\.(jpg|jpeg|png|gif|bmp).*")) {
                showError(photoError, "Le fichier doit avoir une extension image valide : jpg, jpeg, png, gif, bmp");
            } else {
                showError(photoError, "Format d'URL ou de chemin invalide");
            }
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private boolean isFormValid() {
        // Valider tous les champs
        validateNom();
        validatePrenom();
        validateEmail();
        validatePassword();
        validateRole();
        validateStatut();
        validatePhoto();

        // Vérifier si un label d'erreur est visible
        return !nomError.isVisible() && !prenomError.isVisible() &&
                !emailError.isVisible() && !passwordError.isVisible() &&
                !roleError.isVisible() && !statutError.isVisible() &&
                !photoError.isVisible();
    }

    private void loadUsers() {
        try {
            userList.clear();
            List<Users> usersFromDB = userService.getAllUsers();

            if (!usersFromDB.isEmpty()) {
                userList.addAll(usersFromDB);
                System.out.println("Utilisateurs chargés : " + usersFromDB.size());
            }

            userTable.refresh();

        } catch (Exception e) {
            System.err.println("ERREUR lors du chargement des utilisateurs:");
            e.printStackTrace();
        }
    }

    @FXML
    private void saveUser() {
        if (!isFormValid()) {
            // Afficher un message global si nécessaire
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulaire incomplet");
            alert.setHeaderText("Veuillez corriger les erreurs");
            alert.setContentText("Certains champs contiennent des erreurs (surlignés en rouge)");
            alert.showAndWait();
            return;
        }

        try {
            Users user = new Users(
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    emailField.getText().trim().toLowerCase(),
                    passwordField.getText(),
                    roleCombo.getValue()
            );

            user.setStatut(statutCombo.getValue());
            user.setVerified(verifiedCheck.isSelected());

            String profilePic = profilePictureField.getText();
            if (profilePic != null && !profilePic.trim().isEmpty()) {
                user.setProfilePicture(profilePic.trim());
            }

            userService.createUser(user);
            loadUsers();
            clearFields();

            // Message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur créé avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la création");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void updateUser() {
        Users selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à modifier");
            alert.showAndWait();
            return;
        }

        if (!isFormValid()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulaire incomplet");
            alert.setHeaderText("Veuillez corriger les erreurs");
            alert.setContentText("Certains champs contiennent des erreurs (surlignés en rouge)");
            alert.showAndWait();
            return;
        }

        try {
            selected.setNom(nomField.getText().trim());
            selected.setPrenom(prenomField.getText().trim());
            selected.setEmail(emailField.getText().trim().toLowerCase());
            selected.setRole(roleCombo.getValue());
            selected.setStatut(statutCombo.getValue());
            selected.setVerified(verifiedCheck.isSelected());

            String profilePic = profilePictureField.getText();
            if (profilePic != null && !profilePic.trim().isEmpty()) {
                selected.setProfilePicture(profilePic.trim());
            }

            userService.updateUser(selected);
            loadUsers();
            clearFields();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur modifié avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la modification");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void deleteUser() {
        Users selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Voulez-vous vraiment supprimer " +
                selected.getPrenom() + " " + selected.getNom() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                userService.deleteUser(selected.getIdUser());
                loadUsers();
                clearFields();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Utilisateur supprimé avec succès !");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de la suppression");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void selectUser() {
        Users selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            nomField.setText(selected.getNom());
            prenomField.setText(selected.getPrenom());
            emailField.setText(selected.getEmail());
            passwordField.clear();
            roleCombo.setValue(selected.getRole());
            statutCombo.setValue(selected.getStatut());
            verifiedCheck.setSelected(selected.isVerified());
            profilePictureField.setText(selected.getProfilePicture());

            // Cacher tous les messages d'erreur lors de la sélection
            hideError(nomError);
            hideError(prenomError);
            hideError(emailError);
            hideError(passwordError);
            hideError(roleError);
            hideError(statutError);
            hideError(photoError);
        }
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        statutCombo.setValue(null);
        verifiedCheck.setSelected(false);
        profilePictureField.clear();
        userTable.getSelectionModel().clearSelection();

        // Cacher tous les messages d'erreur
        hideError(nomError);
        hideError(prenomError);
        hideError(emailError);
        hideError(passwordError);
        hideError(roleError);
        hideError(statutError);
        hideError(photoError);
    }
}