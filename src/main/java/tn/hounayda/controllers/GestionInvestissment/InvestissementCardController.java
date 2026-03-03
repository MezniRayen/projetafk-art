package tn.hounayda.controllers.GestionInvestissment;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.hounayda.entities.GestionInvestissment.Investissement;

import java.time.format.DateTimeFormatter;

public class InvestissementCardController {

    @FXML
    private Label projetLabel;
    @FXML
    private Label statutLabel;
    @FXML
    private Label montantLabel;
    @FXML
    private Label moyenLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label palierLabel;
    @FXML
    private Label messageLabel;

    private Investissement investissement;

    public void setInvestissement(Investissement investissement) {
        this.investissement = investissement;

        projetLabel.setText("Projet #" + investissement.getIdProjet());
        statutLabel.setText(investissement.getStatut());
        montantLabel.setText("Montant: " + investissement.getMontant() + " €");
        moyenLabel.setText("Paiement: " + investissement.getMoyenPaiement());

        if (investissement.getDateInvestissement() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateLabel.setText("Le " + investissement.getDateInvestissement().format(fmt));
        } else {
            dateLabel.setText("");
        }

        if (investissement.getPalier() != null && !investissement.getPalier().isBlank()) {
            palierLabel.setText("Palier: " + investissement.getPalier());
        } else {
            palierLabel.setText("");
        }

        messageLabel.setText(investissement.getMessageSoutien() != null ? investissement.getMessageSoutien() : "");

        // Style selon le statut
        switch (investissement.getStatut()) {
            case "VALIDE":
                statutLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "EN_ATTENTE":
                statutLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            case "ANNULE":
                statutLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                break;
        }
    }

    public Investissement getInvestissement() {
        return investissement;
    }
}