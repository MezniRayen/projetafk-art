package tn.hounayda.services;

import tn.hounayda.entities.Users;
import tn.hounayda.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class VerificationService {

    private final UserService userService = new UserService();
    private static final Map<String, Long> validTokens = new HashMap<>(); // Stockage temporaire

    /**
     * Vérifie le token et active le compte
     */
    public boolean verifyAccount(String token) {
        Long userId = validTokens.get(token);
        if (userId == null) {
            System.out.println("❌ Token invalide ou expiré");
            return false;
        }

        Users user = userService.getUserById(userId);
        if (user == null) {
            return false;
        }

        // Mettre à jour le statut vérifié
        user.setVerified(true);

        String sql = "UPDATE users SET isVerified = true WHERE idUser = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            int result = pstmt.executeUpdate();

            if (result > 0) {
                validTokens.remove(token); // Token utilisé, on le supprime
                System.out.println("✅ Compte vérifié pour l'utilisateur ID: " + userId);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Génère et stocke un token pour un utilisateur
     */
    public String generateTokenForUser(Users user) {
        String rawToken = user.getIdUser() + "-" + user.getEmail() + "-" + System.currentTimeMillis();
        String token = Integer.toHexString(rawToken.hashCode());
        validTokens.put(token, user.getIdUser());
        return token;
    }
}