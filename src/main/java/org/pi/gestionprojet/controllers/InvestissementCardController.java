package org.pi.gestionprojet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.pi.gestionprojet.entities.Investissement;

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

    public void setData(Investissement investissement) {
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
    }

    public Investissement getInvestissement() {
        return investissement;
    }
}

