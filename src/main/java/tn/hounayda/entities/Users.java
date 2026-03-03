package tn.hounayda.entities;

import java.util.Date;
import java.util.regex.Pattern;

public class Users {
    private long idUser;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;  // Hashé avec BCrypt
    private UserRole role;
    private Date dateCreation;
    private UserStatut statut;
    private Date lastLogin;
    private boolean isVerified;
    private String profilePicture;
    private String typeUser;
    // Constantes pour les validations
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 100;

    // Patterns de validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    public UserStatut getStatutSafe() {
        return statut != null ? statut : UserStatut.ACTIF;
    }
/*
   // patterns de validation de mot de passe
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{" + MIN_PASSWORD_LENGTH + ",}$"
    );*/


    // Pattern simple pour le développement (juste longueur minimale)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^\\S{" + MIN_PASSWORD_LENGTH + ",}$");

    // Extensions d'images autorisées
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    // Constructeur vide
    public Users() {}

    // Constructeur pour création avec validation
    public Users(String nom, String prenom, String email, String motDePasse, UserRole role) {
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setMotDePasse(motDePasse);
        setRole(role);
        this.statut = UserStatut.ACTIF;
        this.isVerified = false;
    }

    // Méthodes de validation
    private void validateIdUser(long idUser) {
        if (idUser < 0) {
            throw new IllegalArgumentException("L'ID utilisateur ne peut pas être négatif");
        }
    }

    private void validateNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }

        String trimmedNom = nom.trim();
        if (trimmedNom.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException("Le nom doit contenir au moins " + MIN_NAME_LENGTH + " caractères");
        }

        if (trimmedNom.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Le nom ne peut pas dépasser " + MAX_NAME_LENGTH + " caractères");
        }

        if (!NAME_PATTERN.matcher(trimmedNom).matches()) {
            throw new IllegalArgumentException("Le nom contient des caractères non autorisés. Utilisez lettres, espaces, apostrophes et tirets");
        }
    }

    private void validatePrenom(String prenom) {
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom ne peut pas être vide");
        }

        String trimmedPrenom = prenom.trim();
        if (trimmedPrenom.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException("Le prénom doit contenir au moins " + MIN_NAME_LENGTH + " caractères");
        }

        if (trimmedPrenom.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Le prénom ne peut pas dépasser " + MAX_NAME_LENGTH + " caractères");
        }

        if (!NAME_PATTERN.matcher(trimmedPrenom).matches()) {
            throw new IllegalArgumentException("Le prénom contient des caractères non autorisés. Utilisez lettres, espaces, apostrophes et tirets");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }

        String trimmedEmail = email.trim().toLowerCase();
        if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("L'email ne peut pas dépasser " + MAX_EMAIL_LENGTH + " caractères");
        }

        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
    }

    private void validateMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }

        if (motDePasse.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
        }

        if (motDePasse.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas dépasser " + MAX_PASSWORD_LENGTH + " caractères");
        }

        // Validation de base : pas d'espaces
        if (motDePasse.contains(" ")) {
            throw new IllegalArgumentException("Le mot de passe ne doit pas contenir d'espaces");
        }

        // Validation complexe COMMENTÉE pour le développement
        // Décommentez cette section pour activer la validation stricte en production
        /*
        if (!PASSWORD_PATTERN.matcher(motDePasse).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre, une minuscule, une majuscule et un caractère spécial (@#$%^&+=!)");
        }
        */
    }

    private void validateProfilePicture(String profilePicture) {
        if (profilePicture != null && !profilePicture.trim().isEmpty()) {
            String lowerCase = profilePicture.toLowerCase();
            boolean isValid = false;

            for (String ext : ALLOWED_IMAGE_EXTENSIONS) {
                if (lowerCase.endsWith(ext)) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                throw new IllegalArgumentException("Format d'image non supporté. Utilisez : jpg, jpeg, png, gif, bmp");
            }
        }
    }

    // Getters et Setters avec validation
    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        validateIdUser(idUser);
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        validateNom(nom);
        this.nom = nom.trim();
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        validatePrenom(prenom);
        this.prenom = prenom.trim();
    }
    public String getTypeUser() {
        return typeUser;
    }

    public void setTypeUser(String typeUser) {
        this.typeUser = typeUser;
    }

    // Méthodes utilitaires
    public boolean isArtiste() {
        return "ARTISTE".equals(typeUser) || "LES_DEUX".equals(typeUser);
    }

    public boolean isInvestisseur() {
        return "INVESTISSEUR".equals(typeUser) || "LES_DEUX".equals(typeUser);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email.trim().toLowerCase();
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        validateMotDePasse(motDePasse);
        this.motDePasse = motDePasse;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Le rôle ne peut pas être null");
        }
        this.role = role;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UserStatut getStatut() {
        return statut;
    }

    public void setStatut(UserStatut statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null");
        }
        this.statut = statut;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        validateProfilePicture(profilePicture);
        this.profilePicture = profilePicture;
    }

    // Méthode utilitaire pour validation complète
    public void validate() {
        validateNom(this.nom);
        validatePrenom(this.prenom);
        validateEmail(this.email);
        validateMotDePasse(this.motDePasse);
        if (this.role == null) {
            throw new IllegalArgumentException("Le rôle est requis");
        }
        if (this.statut == null) {
            throw new IllegalArgumentException("Le statut est requis");
        }
        validateProfilePicture(this.profilePicture);
    }

    // Méthodes utilitaires supplémentaires
    public String getFullName() {
        return prenom + " " + nom;
    }

    public boolean isActive() {
        return statut == UserStatut.ACTIF;
    }



    @Override
    public String toString() {
        return "Users{" +
                "idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", statut=" + statut +
                ", isVerified=" + isVerified +
                '}';
    }

    public Object isUser() {
    }
}