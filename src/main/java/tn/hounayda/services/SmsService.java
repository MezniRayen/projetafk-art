package tn.hounayda.services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    // ⚠️ REMPLACE CES VALEURS PAR TES PROPRES IDENTIFIANTS TWILIO
    private static final String ACCOUNT_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String AUTH_TOKEN = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String FROM_NUMBER = "+33123456789"; // Ton numéro Twilio acheté

    // Bloc d'initialisation statique : Twilio n'est initialisé qu'une seule fois
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        System.out.println("✅ Service Twilio initialisé.");
    }

    /**
     * Envoie un code de réinitialisation de mot de passe par SMS.
     *
     * @param phoneNumber Le numéro de téléphone du destinataire (peut être avec ou sans indicatif).
     * @param code        Le code de réinitialisation à 6 chiffres.
     * @return true si l'envoi a réussi, false en cas d'échec.
     */
    public boolean sendResetCodeSms(String phoneNumber, String code) {
        try {
            // 1. Formater le numéro de téléphone
            String formattedNumber = formatPhoneNumber(phoneNumber);

            // 2. Construire le corps du message
            String messageBody = String.format(
                    "🔐 AFK'Art - Votre code de réinitialisation est : %s\n" +
                            "Ce code est valable pendant 10 minutes.", code
            );

            // 3. Envoyer le message via l'API Twilio
            Message message = Message.creator(
                    new PhoneNumber(formattedNumber), // Numéro du destinataire
                    new PhoneNumber(FROM_NUMBER),     // Ton numéro Twilio (expéditeur)
                    messageBody                        // Le contenu du SMS
            ).create();

            // 4. Log de succès
            System.out.println("✅ SMS envoyé avec succès à : " + formattedNumber);
            System.out.println("   SID du message : " + message.getSid());
            return true;

        } catch (ApiException e) {
            // Gère les erreurs spécifiques de l'API Twilio [citation:1]
            System.err.println("❌ Erreur Twilio (" + e.getCode() + ") : " + e.getMessage());
            if (e.getCode() == 21408) {
                System.err.println("   -> Votre numéro n'est pas autorisé à envoyer des SMS. Vérifiez vos capacités SMS.");
            } else if (e.getCode() == 21211) {
                System.err.println("   -> Le numéro de téléphone '" + phoneNumber + "' est invalide.");
            } else if (e.getCode() == 20404) {
                System.err.println("   -> Le numéro d'expéditeur '" + FROM_NUMBER + "' n'est pas valide pour ce compte.");
            }
            return false;
        } catch (Exception e) {
            // Gère les autres erreurs
            System.err.println("❌ Erreur inattendue lors de l'envoi du SMS : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Version de test (pour le développement sans consommer de crédits).
     * Simule l'envoi d'un SMS et l'affiche dans la console.
     */
    public boolean sendResetCodeSmsTest(String phoneNumber, String code) {
        System.out.println("\n=== [SIMULATION SMS - MODE TEST] ===");
        System.out.println("À : " + formatPhoneNumber(phoneNumber));
        System.out.println("Code : " + code);
        System.out.println("Message : AFK'Art - Votre code de réinitialisation est : " + code);
        System.out.println("Expéditeur (Twilio) : " + FROM_NUMBER);
        System.out.println("=====================================\n");
        return true;
    }

    /**
     * Formate un numéro de téléphone pour Twilio (ajoute l'indicatif tunisien par défaut).
     */
    private String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("\\s+", "").replaceAll("-", "");
        if (!cleaned.startsWith("+")) {
            // Si le numéro ne commence pas par '+', on suppose qu'il est tunisien.
            return "+216" + cleaned;
        }
        return cleaned;
    }
}