package org.pi.gestionprojet.tools;

import org.pi.gestionprojet.entities.Investissement;
import org.pi.gestionprojet.entities.ProjetArtistique;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USER = "meznirayen1506@gmail.com";
    private static final String APP_PASSWORD = "veoaqucqugcbjtxc";
    private static final String TO = "meznirayen1506@gmail.com";

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, APP_PASSWORD);
            }
        });
    }

    private static void sendEmail(String subject, String htmlContent) {
        try {
            Session session = createSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USER, "Plateforme Artistique"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendProjetUpdated(ProjetArtistique projet) {
        String subject = "✨ Mise à jour du projet : " + projet.getTitre();
        String body = buildHeader("Le projet que vous suivez a été mis à jour") +
                "<p style=\"margin:0 0 8px 0;\">Titre du projet : <strong>" + escape(projet.getTitre()) + "</strong></p>" +
                "<p style=\"margin:0 0 8px 0;\">Statut : <strong>" + escape(projet.getStatut()) + "</strong></p>" +
                "<p style=\"margin:0 0 8px 0;\">Objectif : <strong>" + projet.getObjectifFinancier() + " €</strong></p>" +
                "<p style=\"margin:0 0 16px 0;\">Montant collecté : <strong>" + projet.getMontantCollecte() + " €</strong></p>" +
                buildFooter();
        sendEmail(subject, body);
    }

    public static void sendProjetDeleted(ProjetArtistique projet) {
        String subject = "🎭 Projet supprimé : " + projet.getTitre();
        String body = buildHeader("Un projet de votre liste de favoris a été supprimé") +
                "<p style=\"margin:0 0 8px 0;\">Titre du projet : <strong>" + escape(projet.getTitre()) + "</strong></p>" +
                "<p style=\"margin:0 0 16px 0;\">Il n'est désormais plus disponible sur la plateforme.</p>" +
                buildFooter();
        sendEmail(subject, body);
    }

    public static void sendNewInvestissement(ProjetArtistique projet, Investissement inv) {
        String subject = "💸 Nouveau soutien pour : " + projet.getTitre();
        String body = buildHeader("Un nouvel investissement a été réalisé sur un projet de vos favoris") +
                "<p style=\"margin:0 0 8px 0;\">Titre du projet : <strong>" + escape(projet.getTitre()) + "</strong></p>" +
                "<p style=\"margin:0 0 8px 0;\">Montant du nouvel investissement : <strong>" +
                inv.getMontant() + " €</strong></p>" +
                "<p style=\"margin:0 0 16px 0;\">Méthode de paiement : <strong>" +
                escape(inv.getMoyenPaiement()) + "</strong></p>" +
                buildFooter();
        sendEmail(subject, body);
    }

    private static String buildHeader(String title) {
        return "<div style=\"font-family:Segoe UI,system-ui,-apple-system,sans-serif;" +
                "background:linear-gradient(135deg,#fffaf0,#ffe4f3);padding:24px;\">" +
                "<div style=\"max-width:600px;margin:0 auto;background:#ffffff;" +
                "border-radius:16px;box-shadow:0 10px 30px rgba(0,0,0,0.08);padding:24px 28px;\">" +
                "<h2 style=\"margin:0 0 12px 0;color:#2b2d42;font-size:20px;\">" + escape(title) + "</h2>" +
                "<p style=\"margin:0 0 20px 0;color:#6b6b83;font-size:13px;\">" +
                "Vous recevez cet email parce que vous avez ajouté ce projet à vos favoris.</p>";
    }

    private static String buildFooter() {
        return "<hr style=\"border:none;border-top:1px solid #f1f1f1;margin:20px 0;\"/>" +
                "<p style=\"margin:0;color:#a0a0b2;font-size:11px;\">" +
                "Plateforme de financement artistique – notification automatique.</p>" +
                "</div></div>";
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

