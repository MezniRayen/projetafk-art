package tn.hounayda.services;

import tn.hounayda.entities.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PasswordResetService {

    private final Map<String, ResetRequest> resetRequests = new HashMap<>();
    private final EmailService emailService = new EmailService();
    private final SmsService smsService = new SmsService();
    private final UserService userService = new UserService();

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
            return System.currentTimeMillis() - timestamp < TimeUnit.MINUTES.toMillis(10);
        }
    }

    /**
     * Génère un code aléatoire à 6 chiffres
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Demande de réinitialisation par email
     */
    public boolean requestResetByEmail(String email) {
        Users user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }

        String code = generateCode();
        resetRequests.put(email, new ResetRequest(code, email));

        return emailService.sendResetCodeEmail(email, code);
    }

    /**
     * Demande de réinitialisation par SMS
     */
    public boolean requestResetBySms(String phoneNumber, String email) {
        Users user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }

        String code = generateCode();
        resetRequests.put(email, new ResetRequest(code, email));

        // Utiliser sendResetCodeSmsTest pour les tests sans vrai SMS
        return smsService.sendResetCodeSmsTest(phoneNumber, code);
    }

    /**
     * Vérifie le code et réinitialise le mot de passe
     */
    public boolean resetPassword(String email, String code, String newPassword) {
        ResetRequest request = resetRequests.get(email);

        if (request == null || !request.isValid() || !request.code.equals(code)) {
            return false;
        }

        Users user = userService.getUserByEmail(email);
        if (user == null) {
            return false;
        }

        // Mettre à jour le mot de passe
        boolean success = userService.changePassword(
                user.getIdUser(),
                user.getMotDePasse(), // Pour le changement, on utilise l'ancien hash
                newPassword,
                newPassword
        );

        if (success) {
            resetRequests.remove(email);
        }

        return success;
    }
}