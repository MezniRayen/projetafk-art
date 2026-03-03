package tn.hounayda.services;

import tn.hounayda.entities.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PasswordResetService {

    private final Map<String, ResetRequest> resetRequests = new HashMap<>();
    private final EmailService emailService = new EmailService();
    private final SmsService smsService = new SmsService(); // On ajoute le service SMS
    private final UserService userService = new UserService();

    // Constante pour définir la durée de validité (en minutes)
    private static final long CODE_VALIDITY_MINUTES = 30; // 30 minutes au lieu de 10

    private static class ResetRequest {
        String code;
        long timestamp;
        String email;

        ResetRequest(String code, String email) {
            this.code = code;
            this.timestamp = System.currentTimeMillis();
            this.email = email;
        }

        boolean isValid() {
            long validityDuration = TimeUnit.MINUTES.toMillis(CODE_VALIDITY_MINUTES);
            long elapsedTime = System.currentTimeMillis() - timestamp;

            System.out.println("⏱️ Temps écoulé: " + elapsedTime + " ms");
            System.out.println("⏱️ Durée max: " + validityDuration + " ms");
            System.out.println("⏱️ Reste: " + (validityDuration - elapsedTime) + " ms");

            return elapsedTime < validityDuration;
        }
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // --- Méthodes existantes ---
    public boolean requestResetByEmail(String email) {
        Users user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        String code = generateCode();
        System.out.println("🔑 Code généré pour " + email + " : " + code); // AJOUTE CE LOG
        resetRequests.put(email, new ResetRequest(code, email));
        return emailService.sendResetCodeEmail(email, code);
    }

    // --- NOUVELLE Méthode pour la réinitialisation par SMS ---
    public boolean requestResetBySms(String phoneNumber, String email) {
        Users user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }

        String code = generateCode();
        resetRequests.put(email, new ResetRequest(code, email));

        // Appel à ton service SMS (tu peux choisir entre mode test et réel)
        // return smsService.sendResetCodeSms(phoneNumber, code); // Pour le mode réel
        return smsService.sendResetCodeSmsTest(phoneNumber, code); // Pour le mode test (sans crédits)
    }

    // --- Méthode de validation améliorée avec logs de débogage ---
    public boolean resetPassword(String email, String code, String newPassword) {
        ResetRequest request = resetRequests.get(email);

        System.out.println("=== VÉRIFICATION DU CODE ===");
        System.out.println("Email: " + email);
        System.out.println("Code saisi: " + code);

        if (request == null) {
            System.out.println("❌ Aucune demande trouvée pour cet email");
            return false;
        }

        System.out.println("Code stocké: " + request.code);
        System.out.println("Temps écoulé: " + (System.currentTimeMillis() - request.timestamp) + " ms");

        if (!request.isValid()) {
            System.out.println("❌ Code expiré");
            return false;
        }

        if (!request.code.equals(code)) {
            System.out.println("❌ Code incorrect");
            return false;
        }

        Users user = userService.getUserByEmail(email);
        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé");
            return false;
        }

        // UTILISER LA NOUVELLE MÉTHODE resetPassword au lieu de changePassword
        boolean success = userService.resetPassword(user.getIdUser(), newPassword, newPassword);

        System.out.println("Résultat resetPassword: " + success);

        if (success) {
            resetRequests.remove(email);
            System.out.println("✅ Mot de passe réinitialisé avec succès !");
        }

        return success;
    }
}