package tn.hounayda.services;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.mindrot.jbcrypt.BCrypt;
import tn.hounayda.utils.DatabaseConnection;
import tn.hounayda.entities.Users;
import tn.hounayda.entities.UserRole;
import tn.hounayda.entities.UserStatut;
import tn.hounayda.utils.Session;

import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    @FXML
    public void initialize() {
        if (!Session.getInstance().isAdmin()) {
            // Redirige ou bloque
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès interdit");
            alert.setContentText("Seuls les admins peuvent accéder à cette page");
            alert.showAndWait();
            // Ferme ou redirige vers user dashboard
            return;
        }
        getAllUsers();
    }

    // Create (avec hashage mot de passe)
    public void createUser(Users user) {
        // Hasher le mot de passe avec BCrypt
        String hashedPass = BCrypt.hashpw(user.getMotDePasse(), BCrypt.gensalt());
        user.setMotDePasse(hashedPass);

        String sql = "INSERT INTO users (nom, prenom, email, motDePasse, role, statut, isVerified, type_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getMotDePasse()); // C'est le mot de passe hashé
            pstmt.setString(5, user.getRole().name());
            pstmt.setString(6, user.getStatut().name());
            pstmt.setBoolean(7, user.isVerified());
            pstmt.setString(8, user.getTypeUser()); // Ajout du type_user

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                user.setIdUser(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    // Read all
    public List<Users> getAllUsers() {
        List<Users> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Users user = new Users();
                user.setIdUser(rs.getLong("idUser"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("motDePasse"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setDateCreation(rs.getTimestamp("dateCreation"));
                user.setStatut(UserStatut.valueOf(rs.getString("statut")));
                user.setLastLogin(rs.getTimestamp("lastLogin"));
                user.setVerified(rs.getBoolean("isVerified"));
                user.setProfilePicture(rs.getString("profilePicture"));
                user.setTypeUser(rs.getString("type_user")); // Ajout du type_user

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Update
    public void updateUser(Users user) {
        String sql = "UPDATE users SET nom = ?, prenom = ?, email = ?, role = ?, statut = ?, " +
                "isVerified = ?, profilePicture = ?, lastLogin = ?, type_user = ? WHERE idUser = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole() != null ? user.getRole().name() : null);
            pstmt.setString(5, user.getStatut() != null ? user.getStatut().name() : "ACTIF");
            pstmt.setBoolean(6, user.isVerified());
            pstmt.setString(7, user.getProfilePicture());
            pstmt.setTimestamp(8, user.getLastLogin() != null ? new Timestamp(user.getLastLogin().getTime()) : null);
            pstmt.setString(9, user.getTypeUser()); // Ajout du type_user
            pstmt.setLong(10, user.getIdUser());

            pstmt.executeUpdate();

            System.out.println("Utilisateur mis à jour (ID " + user.getIdUser() + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String uploadProfilePicture(Users user, File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }

        try {
            // Créer le dossier s'il n'existe pas
            String uploadDir = "src/main/resources/images/profiles/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Générer un nom de fichier unique
            String extension = getFileExtension(imageFile.getName());
            String fileName = user.getIdUser() + "_" + System.currentTimeMillis() + "." + extension;
            String filePath = uploadDir + fileName;

            // Copier le fichier
            Files.copy(imageFile.toPath(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

            // URL relative pour l'application
            String url = "/images/profiles/" + fileName;

            // Mettre à jour l'utilisateur
            user.setProfilePicture(url);
            updateUser(user);

            return url;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "jpg"; // extension par défaut
    }

    public Users getUserById(long id) {
        Users user = null;
        String sql = "SELECT * FROM users WHERE idUser = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new Users();
                user.setIdUser(rs.getLong("idUser"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("motDePasse"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setDateCreation(rs.getTimestamp("dateCreation"));
                user.setStatut(UserStatut.valueOf(rs.getString("statut")));
                user.setLastLogin(rs.getTimestamp("lastLogin"));
                user.setVerified(rs.getBoolean("isVerified"));
                user.setProfilePicture(rs.getString("profilePicture"));
                user.setTypeUser(rs.getString("type_user")); // Ajout du type_user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public boolean changePassword(long userId, String oldPassword, String newPassword, String confirmPassword) {
        // Validation des entrées
        if (oldPassword == null || oldPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        if (newPassword.length() < 8) {
            return false;
        }

        // Récupérer l'utilisateur
        Users user = getUserById(userId);
        if (user == null) {
            return false;
        }

        // Vérifier l'ancien mot de passe avec BCrypt
        if (!BCrypt.checkpw(oldPassword, user.getMotDePasse())) {
            return false;
        }

        // Hasher le nouveau mot de passe
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Mettre à jour dans la base de données
        String sql = "UPDATE users SET motDePasse = ? WHERE idUser = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedNewPassword);
            pstmt.setLong(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Mettre à jour l'objet user
                user.setMotDePasse(hashedNewPassword);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Utilisé pour la réinitialisation par email/SMS
     */
    public boolean resetPassword(long userId, String newPassword, String confirmPassword) {
        System.out.println("=== RÉINITIALISATION DE MOT DE PASSE ===");
        System.out.println("User ID: " + userId);

        // Validation des entrées
        if (newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            System.out.println("❌ Champs vides");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("❌ Les mots de passe ne correspondent pas");
            return false;
        }

        if (newPassword.length() < 8) {
            System.out.println("❌ Mot de passe trop court");
            return false;
        }

        // Hasher le nouveau mot de passe
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        System.out.println("✅ Nouveau mot de passe hashé: " + hashedNewPassword);

        // Mettre à jour dans la base de données
        String sql = "UPDATE users SET motDePasse = ? WHERE idUser = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedNewPassword);
            pstmt.setLong(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Lignes affectées: " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("✅ Mot de passe réinitialisé avec succès !");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Delete
    public void deleteUser(long id) {
        String sql = "DELETE FROM users WHERE idUser = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSuspiciousLogin(Users user, String ip) {
        // Vérifie si l'utilisateur a déjà une date de dernier login
        Date lastLogin = user.getLastLogin();
        if (lastLogin == null) {
            return false;  // Pas de suspicion si première connexion
        }

        // Date actuelle
        Date now = new Date();

        // Différence en millisecondes
        long diffInMillis = now.getTime() - lastLogin.getTime();

        // Moins de 1 minute → suspicion de tentative multiple (brute force)
        if (diffInMillis < 60 * 1000) {
            System.out.println("Connexion suspecte détectée : délai trop court (" + diffInMillis + " ms)");
            return true;
        }

        return false;
    }

    public Users getUserByEmail(String email) {
        Users user = null;
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new Users();
                user.setIdUser(rs.getLong("idUser"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("motDePasse"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setDateCreation(rs.getTimestamp("dateCreation"));
                user.setStatut(UserStatut.valueOf(rs.getString("statut")));
                user.setLastLogin(rs.getTimestamp("lastLogin"));
                user.setVerified(rs.getBoolean("isVerified"));
                user.setProfilePicture(rs.getString("profilePicture"));
                user.setTypeUser(rs.getString("type_user")); // Ajout du type_user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Met à jour uniquement le type_user d'un utilisateur
     */
    public void updateUserType(long userId, String typeUser) {
        String sql = "UPDATE users SET type_user = ? WHERE idUser = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeUser);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

            System.out.println("Type d'utilisateur mis à jour (ID " + userId + ") : " + typeUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère tous les utilisateurs d'un type spécifique (ARTISTE, INVESTISSEUR, LES_DEUX)
     */
    public List<Users> getUsersByType(String typeUser) {
        List<Users> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE type_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Users user = new Users();
                user.setIdUser(rs.getLong("idUser"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("motDePasse"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setDateCreation(rs.getTimestamp("dateCreation"));
                user.setStatut(UserStatut.valueOf(rs.getString("statut")));
                user.setLastLogin(rs.getTimestamp("lastLogin"));
                user.setVerified(rs.getBoolean("isVerified"));
                user.setProfilePicture(rs.getString("profilePicture"));
                user.setTypeUser(rs.getString("type_user"));

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}