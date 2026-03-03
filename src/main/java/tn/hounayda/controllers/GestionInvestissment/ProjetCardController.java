package tn.hounayda.controllers.GestionInvestissment;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import tn.hounayda.entities.GestionInvestissment.ProjetArtistique;
import tn.hounayda.services.GestionInvestissment.FavoriService;
import tn.hounayda.utils.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @FXML
    private Button favoriButton;  // Nouveau bouton

    private ProjetArtistique projet;
    private boolean isInvestisseur;
    private FavoriService favoriService = new FavoriService();

    public void setProjet(ProjetArtistique projet, boolean isOwnedByCurrentArtist, boolean isAdmin, boolean isInvestisseur) {
        this.projet = projet;
        this.isInvestisseur = isInvestisseur;

        titreLabel.setText(projet.getTitre());
        categorieLabel.setText(projet.getCategorie() != null ? projet.getCategorie() : "Sans catégorie");
        statutLabel.setText(projet.getStatut());
        descriptionLabel.setText(projet.getDescription() != null ? projet.getDescription() : "");

        BigDecimal objectif = projet.getObjectifFinancier() != null ? projet.getObjectifFinancier() : BigDecimal.ZERO;
        BigDecimal collecte = projet.getMontantCollecte() != null ? projet.getMontantCollecte() : BigDecimal.ZERO;
        objectifLabel.setText(String.format("Objectif: %.2f €", objectif));
        collecteLabel.setText(String.format("Collecté: %.2f €", collecte));

        double progress = 0.0;
        if (objectif.compareTo(BigDecimal.ZERO) > 0) {
            progress = collecte.divide(objectif, 4, RoundingMode.HALF_UP).doubleValue();
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

        // Gestion de l'affichage du propriétaire
        if (isAdmin) {
            ownerLabel.setText("Artiste ID: " + projet.getIdArtiste());
        } else if (isOwnedByCurrentArtist) {
            ownerLabel.setText("Mon projet");
            ownerLabel.setStyle("-fx-text-fill: #E38792; -fx-font-weight: bold;");
        } else {
            ownerLabel.setText("");
        }

        // Configurer le bouton favori
        configureFavoriButton();

        // Style selon le statut
        styleSelonStatut();
    }

    private void configureFavoriButton() {
        if (!isInvestisseur) {
            favoriButton.setVisible(false);
            favoriButton.setManaged(false);
            return;
        }

        favoriButton.setVisible(true);
        favoriButton.setManaged(true);

        long userId = Session.getInstance().getCurrentUser().getIdUser();

        // Vérifier si le projet est déjà en favori
        boolean estFavori = favoriService.estFavori((int) userId, projet.getIdProjet());

        if (estFavori) {
            favoriButton.setText("★ Retirer des favoris");
            favoriButton.setStyle("-fx-background-color: #E38792; -fx-text-fill: white;");
            favoriButton.setOnAction(e -> retirerDesFavoris());
        } else {
            favoriButton.setText("☆ Ajouter aux favoris");
            favoriButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            favoriButton.setOnAction(e -> ajouterAuxFavoris());
        }
    }

    private void ajouterAuxFavoris() {
        long userId = Session.getInstance().getCurrentUser().getIdUser();
        boolean success = favoriService.ajouterFavori((int) userId, projet.getIdProjet());

        if (success) {
            favoriButton.setText("★ Retirer des favoris");
            favoriButton.setStyle("-fx-background-color: #E38792; -fx-text-fill: white;");
            favoriButton.setOnAction(e -> retirerDesFavoris());

            // Notification
            showNotification("Projet ajouté aux favoris");
        }
    }

    private void retirerDesFavoris() {
        long userId = Session.getInstance().getCurrentUser().getIdUser();
        boolean success = favoriService.supprimerFavori((int) userId, projet.getIdProjet());

        if (success) {
            favoriButton.setText("☆ Ajouter aux favoris");
            favoriButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            favoriButton.setOnAction(e -> ajouterAuxFavoris());

            // Notification
            showNotification("Projet retiré des favoris");
        }
    }

    private void styleSelonStatut() {
        switch (projet.getStatut()) {
            case "EN_COURS":
                statutLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "EN_ATTENTE":
                statutLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            case "FINANCE":
                statutLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                break;
            case "ECHEC":
                statutLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                break;
        }
    }

    private void showNotification(String message) {
        // Simple notification via tooltip
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        Tooltip tooltip = new Tooltip(message);
        Tooltip.install(favoriButton, tooltip);
        tooltip.show(favoriButton.getScene().getWindow());
        pt.setOnFinished(e -> tooltip.hide());
        pt.play();
    }

    public ProjetArtistique getProjet() {
        return projet;
    }
}