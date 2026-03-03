package tn.hounayda.services;

import tn.hounayda.entities.Users;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "hounaida.bensaid@esprit.tn"; // À configurer
    private static final String PASSWORD = "llen vpun xoni vlnq"; // À configurer
    private static final String BASE_URL = "http://localhost:8080"; // Pour le développement

    /**
     * Envoie un email de bienvenue à un nouvel utilisateur
     */
    public boolean sendWelcomeEmail(Users user, String token) {
        String subject = "Bienvenue sur AFK'Art !";
        String content = buildWelcomeEmailContent(user);

        return sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Envoie un email avec un code de réinitialisation
     */
    public boolean sendResetCodeEmail(String email, String code) {
        String subject = "Réinitialisation de votre mot de passe AFK'Art";
        String content = buildResetEmailContent(code);

        return sendEmail(email, subject, content);
    }

    private String buildVerificationEmailContent(Users user) {
        // Générer un token unique (tu peux utiliser l'ID + timestamp)
        String verificationLink = BASE_URL + "/verify?userId=" + user.getIdUser() + "&token=" + generateToken(user);

        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; background-color: #f8f5f0; padding: 20px; }" +
                        ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        ".header { text-align: center; margin-bottom: 30px; }" +
                        ".header h1 { color: #4A3B2F; }" +
                        ".content { color: #666; line-height: 1.6; }" +
                        ".button { display: inline-block; padding: 12px 30px; background: linear-gradient(to bottom, #E38792, #D16F7F); color: white; text-decoration: none; border-radius: 25px; margin: 20px 0; font-weight: bold; }" +
                        ".footer { margin-top: 30px; text-align: center; color: #999; font-size: 12px; }" +
                        ".note { background-color: #f8f5f0; padding: 15px; border-radius: 8px; margin: 20px 0; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>Bienvenue sur AFK'Art !</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Bonjour <strong>%s %s</strong>,</p>" +
                        "<p>Merci de vous être inscrit sur AFK'Art ! Pour profiter pleinement de toutes les fonctionnalités, veuillez vérifier votre adresse email en cliquant sur le bouton ci-dessous :</p>" +
                        "<div style='text-align: center;'>" +
                        "<a href='%s' class='button'>✅ VÉRIFIER MON COMPTE</a>" +
                        "</div>" +
                        "<div class='note'>" +
                        "<p><strong>Pourquoi vérifier votre compte ?</strong></p>" +
                        "<ul>" +
                        "<li>Participer aux actions artistiques</li>" +
                        "<li>Recevoir des notifications</li>" +
                        "<li>Accéder à toutes les fonctionnalités</li>" +
                        "</ul>" +
                        "</div>" +
                        "<p>Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :</p>" +
                        "<p style='word-break: break-all; font-size: 12px; color: #E38792;'>%s</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                        "<p>© 2024 AFK'Art - Tous droits réservés</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                user.getPrenom(),
                user.getNom(),
                verificationLink,
                verificationLink
        );
    }

    private String generateToken(Users user) {
        // Génère un token simple (à améliorer avec une vraie logique)
        String rawToken = user.getIdUser() + "-" + user.getEmail() + "-" + System.currentTimeMillis();
        return Integer.toHexString(rawToken.hashCode());
    }

    private boolean sendEmail(String to, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email de vérification envoyé à : " + to);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
            return false;
        }
    }

    private String buildWelcomeEmailContent(Users user) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; background-color: #f8f5f0; padding: 20px; }" +
                        ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        ".header { text-align: center; margin-bottom: 30px; }" +
                        ".header h1 { color: #4A3B2F; }" +
                        ".content { color: #666; line-height: 1.6; }" +
                        ".button { display: inline-block; padding: 12px 30px; background: linear-gradient(to bottom, #E38792, #D16F7F); color: white; text-decoration: none; border-radius: 25px; margin-top: 20px; }" +
                        ".footer { margin-top: 30px; text-align: center; color: #999; font-size: 12px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>Bienvenue sur AFK'Art !</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Bonjour <strong>%s %s</strong>,</p>" +
                        "<p>Nous sommes ravis de vous accueillir sur AFK'Art, votre plateforme de découverte artistique.</p>" +
                        "<p>Votre compte a été créé avec succès. Vous pouvez dès maintenant :</p>" +
                        "<ul>" +
                        "<li>Consulter les actions artistiques disponibles</li>" +
                        "<li>Participer aux événements qui vous intéressent</li>" +
                        "<li>Créer votre liste de favoris</li>" +
                        "</ul>" +
                        "<div style='text-align: center;'>" +
                        "<a href='http://localhost:8080' class='button'>Commencer l'aventure</a>" +
                        "</div>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                        "<p>© 2024 AFK'Art - Tous droits réservés</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                user.getPrenom(), user.getNom()
        );
    }

    private String buildResetEmailContent(String code) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; background-color: #f8f5f0; padding: 20px; }" +
                        ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        ".header { text-align: center; margin-bottom: 30px; }" +
                        ".header h1 { color: #4A3B2F; }" +
                        ".code { font-size: 32px; font-weight: bold; color: #E38792; text-align: center; padding: 20px; background-color: #f8f5f0; border-radius: 10px; margin: 20px 0; }" +
                        ".content { color: #666; line-height: 1.6; }" +
                        ".footer { margin-top: 30px; text-align: center; color: #999; font-size: 12px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>Réinitialisation de mot de passe</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Vous avez demandé la réinitialisation de votre mot de passe AFK'Art.</p>" +
                        "<p>Voici votre code de vérification :</p>" +
                        "<div class='code'>%s</div>" +
                        "<p>Ce code est valable pendant 10 minutes.</p>" +
                        "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>© 2024 AFK'Art - Tous droits réservés</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                code
        );
    }
}