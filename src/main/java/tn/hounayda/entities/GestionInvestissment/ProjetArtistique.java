package tn.hounayda.entities.GestionInvestissment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjetArtistique {
    private int idProjet;
    private long idArtiste;
    private String titre;
    private String description;
    private BigDecimal objectifFinancier;
    private BigDecimal montantCollecte;
    private LocalDate dateCreation;
    private LocalDate dateLimite;
    private String statut;
    private boolean visibilite;
    private String categorie;

    public ProjetArtistique() {
    }

    public ProjetArtistique(int idProjet, long idArtiste, String titre, String description,
                            BigDecimal objectifFinancier, BigDecimal montantCollecte,
                            LocalDate dateCreation, LocalDate dateLimite,
                            String statut, boolean visibilite, String categorie) {
        this.idProjet = idProjet;
        this.idArtiste = idArtiste;
        this.titre = titre;
        this.description = description;
        this.objectifFinancier = objectifFinancier;
        this.montantCollecte = montantCollecte;
        this.dateCreation = dateCreation;
        this.dateLimite = dateLimite;
        this.statut = statut;
        this.visibilite = visibilite;
        this.categorie = categorie;
    }

    public int getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(int idProjet) {
        this.idProjet = idProjet;
    }

    public int getIdArtiste() {
        return Math.toIntExact(idArtiste);
    }

    public void setIdArtiste(int idArtiste) {
        this.idArtiste = idArtiste;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getObjectifFinancier() {
        return objectifFinancier;
    }

    public void setObjectifFinancier(BigDecimal objectifFinancier) {
        this.objectifFinancier = objectifFinancier;
    }

    public BigDecimal getMontantCollecte() {
        return montantCollecte;
    }

    public void setMontantCollecte(BigDecimal montantCollecte) {
        this.montantCollecte = montantCollecte;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isVisibilite() {
        return visibilite;
    }

    public void setVisibilite(boolean visibilite) {
        this.visibilite = visibilite;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
}