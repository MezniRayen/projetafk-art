package tn.hounayda.entities;

import java.util.Date;
import java.util.regex.Pattern;

public class AdminAction {

    private long idAction;
    private long idAdmin;
    private String action;
    private Date dateAction;
    private String description;
    private Date expirationDate;
    private ActionStatus status; // Gardé mais non utilisé pour forcer la valeur

    // Constructeur vide
    public AdminAction() {}

    // Constructeur création (statut automatique = VALIDE)
    public AdminAction(long idAdmin, String action, String description, Date expirationDate) {
        this.idAdmin = idAdmin;
        this.action = action;
        this.description = description;
        this.expirationDate = expirationDate;
        this.status = ActionStatus.VALIDE; // Toujours VALIDE à la création
    }

    // Validation stricte pour NOUVELLES actions seulement
    public void setNewExpirationDate(Date expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("La date d'expiration est requise");
        }
        Date now = new Date();
        if (expirationDate.before(now)) {
            throw new IllegalArgumentException("La date d'expiration doit être dans le futur");
        }
        Date maxDate = new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000); // 1 an max
        if (expirationDate.after(maxDate)) {
            throw new IllegalArgumentException("La date d'expiration ne peut pas dépasser 1 an");
        }
        this.expirationDate = expirationDate;
    }

    // Setter permissif (lecture / modification depuis BD)
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    // Statut calculé dynamiquement (c'est la logique que tu veux)
    public ActionStatus getStatus() {
        if (expirationDate == null) return ActionStatus.VALIDE;
        return isExpired() ? ActionStatus.EXPIREE : ActionStatus.VALIDE;
    }

    public boolean isExpired() {
        if (expirationDate == null) return false;
        return new Date().after(expirationDate);
    }

    // Getters / Setters inchangés (seulement ceux utiles)
    public long getIdAction() { return idAction; }
    public void setIdAction(long idAction) { this.idAction = idAction; }
    public long getIdAdmin() { return idAdmin; }
    public void setIdAdmin(long idAdmin) { this.idAdmin = idAdmin; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Date getDateAction() { return dateAction; }
    public void setDateAction(Date dateAction) { this.dateAction = dateAction; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getExpirationDate() { return expirationDate; }

    @Override
    public String toString() {
        return "AdminAction{id=" + idAction + ", action='" + action + "', status=" + getStatus() + ", expire=" + expirationDate + '}';
    }
}