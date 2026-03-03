package tn.hounayda.entities.GestionInvestissment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Investissement {
    private int idInvestissement;
    private long idInvestisseur;
    private long idProjet;
    private BigDecimal montant;
    private LocalDateTime dateInvestissement;
    private String moyenPaiement;
    private String statut;
    private String palier;
    private String messageSoutien;

    public Investissement() {
    }

    public int getIdInvestissement() {
        return idInvestissement;
    }

    public void setIdInvestissement(int idInvestissement) {
        this.idInvestissement = idInvestissement;
    }

    public int getIdInvestisseur() {
        return Math.toIntExact(idInvestisseur);
    }

    public void setIdInvestisseur(long idInvestisseur) {
        this.idInvestisseur = idInvestisseur;
    }

    public int getIdProjet() {
        return Math.toIntExact(idProjet);
    }

    public void setIdProjet(long idProjet) {
        this.idProjet = idProjet;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateInvestissement() {
        return dateInvestissement;
    }

    public void setDateInvestissement(LocalDateTime dateInvestissement) {
        this.dateInvestissement = dateInvestissement;
    }

    public String getMoyenPaiement() {
        return moyenPaiement;
    }

    public void setMoyenPaiement(String moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPalier() {
        return palier;
    }

    public void setPalier(String palier) {
        this.palier = palier;
    }

    public String getMessageSoutien() {
        return messageSoutien;
    }

    public void setMessageSoutien(String messageSoutien) {
        this.messageSoutien = messageSoutien;
    }
}