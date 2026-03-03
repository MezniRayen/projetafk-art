package tn.hounayda.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    // Compte Twilio de test (à remplacer par vos vraies clés)
    private static final String ACCOUNT_SID = "votre_account_sid"; // À configurer
    private static final String AUTH_TOKEN = "votre_auth_token"; // À configurer
    private static final String FROM_NUMBER = "+1234567890"; // Numéro Twilio

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un code de réinitialisation par SMS
     */
    public boolean sendResetCodeSms(String phoneNumber, String code) {
        try {
            String message = String.format(
                    "🔐 AFK'Art - Votre code de réinitialisation est : %s\n" +
                            "Ce code est valable pendant 10 minutes.", code
            );

            Message sms = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(FROM_NUMBER),
                    message
            ).create();

            System.out.println("SMS envoyé avec succès à : " + phoneNumber);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
            return false;
        }
    }

    /**
     * Version de test (simulation sans vrai SMS)
     */
    public boolean sendResetCodeSmsTest(String phoneNumber, String code) {
        System.out.println("=== SIMULATION SMS ===");
        System.out.println("À : " + phoneNumber);
        System.out.println("Code : " + code);
        System.out.println("Message : AFK'Art - Votre code de réinitialisation est : " + code);
        System.out.println("=====================");
        return true;
    }
}