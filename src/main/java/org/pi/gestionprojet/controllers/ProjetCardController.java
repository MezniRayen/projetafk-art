package org.pi.gestionprojet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.pi.gestionprojet.entities.ProjetArtistique;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ProjetCardController {

    @FXML
    private Label titreLabel;
    @FXML
    private Label categorieLabel;
    @FXML
    private Label statutLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label objectifLabel;
    @FXML
    private Label collecteLabel;
    @FXML
    private Label datesLabel;
    @FXML
    private Label ownerLabel;
    @FXML
    private ProgressBar progressBar;

    private ProjetArtistique projet;

    public void setData(ProjetArtistique projet, boolean isOwnedByCurrentArtist) {
        this.projet = projet;

        titreLabel.setText(projet.getTitre());
        categorieLabel.setText(projet.getCategorie() != null ? projet.getCategorie() : "Sans catégorie");
        statutLabel.setText(projet.getStatut());
        descriptionLabel.setText(projet.getDescription() != null ? projet.getDescription() : "");

        BigDecimal objectif = projet.getObjectifFinancier() != null ? projet.getObjectifFinancier() : BigDecimal.ZERO;
        BigDecimal collecte = projet.getMontantCollecte() != null ? projet.getMontantCollecte() : BigDecimal.ZERO;
        objectifLabel.setText("Objectif: " + objectif + " €");
        collecteLabel.setText("Collecté: " + collecte + " €");

        double progress = 0.0;
        if (objectif.compareTo(BigDecimal.ZERO) > 0) {
            progress = collecte.divide(objectif, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
            progress = Math.max(0, Math.min(1, progress));
        }
        progressBar.setProgress(progress);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder db = new StringBuilder();
        if (projet.getDateCreation() != null) {
            db.append("Créé le ").append(projet.getDateCreation().format(fmt));
        }
        if (projet.getDateLimite() != null) {
            if (!db.isEmpty()) db.append(" • ");
            db.append("Jusqu'au ").append(projet.getDateLimite().format(fmt));
        }
        datesLabel.setText(db.toString());

        ownerLabel.setText(isOwnedByCurrentArtist ? "Mon projet" : "Autre artiste");
    }

    public ProjetArtistique getProjet() {
        return projet;
    }
}

