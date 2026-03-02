package org.pi.gestionprojet.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.pi.gestionprojet.entities.Investissement;
import org.pi.gestionprojet.entities.ProjetArtistique;
import org.pi.gestionprojet.service.InvestissementService;
import org.pi.gestionprojet.service.ProjetArtistiqueService;
import org.pi.gestionprojet.tools.GroqAIService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UIController {

    private enum Role {
        ARTISTE,
        INVESTISSEUR
    }

    private static final int ARTISTE_ID = 1;
    private static final int INVESTISSEUR_ID = 2;

    private final ProjetArtistiqueService projetService = new ProjetArtistiqueService();
    private final InvestissementService investissementService = new InvestissementService();

    private Role currentRole = Role.INVESTISSEUR;

    @FXML
    private Label roleLabel;

    @FXML
    private ToggleButton btnViewProjets;
    @FXML
    private ToggleButton btnViewInvestissements;
    @FXML
    private ToggleButton btnViewFavoris;

    private final ToggleGroup viewToggleGroup = new ToggleGroup();

    @FXML
    private FlowPane projetsContainer;
    @FXML
    private FlowPane investissementsContainer;
    @FXML
    private FlowPane favorisContainer;

    @FXML
    private ScrollPane projetsScroll;
    @FXML
    private ScrollPane investissementsScroll;
    @FXML
    private ScrollPane favorisScroll;

    @FXML
    private Button btnNewProjet;
    @FXML
    private Button btnEditProjet;
    @FXML
    private Button btnDeleteProjet;
    @FXML
    private Button btnInvestir;
    @FXML
    private Button btnAddFavori;
    @FXML
    private Button btnRemoveFavori;

    @FXML
    private Button btnEditInvestissement;
    @FXML
    private Button btnDeleteInvestissement;

    private final List<ProjetArtistique> projetsData = new ArrayList<>();
    private final List<Investissement> investissementsData = new ArrayList<>();
    private final List<ProjetArtistique> favorisData = new ArrayList<>();

    private Node selectedProjetCard;
    private ProjetArtistique selectedProjet;

    private Node selectedInvestissementCard;
    private Investissement selectedInvestissement;

    @FXML
    public void initialize() {
        // configure toggle group in code
        btnViewProjets.setToggleGroup(viewToggleGroup);
        btnViewInvestissements.setToggleGroup(viewToggleGroup);
        if (btnViewFavoris != null) {
            btnViewFavoris.setToggleGroup(viewToggleGroup);
        }
        chooseInitialRole();
        showProjetsView();
    }

    private void chooseInitialRole() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choix du rôle");
        alert.setHeaderText("Connectez-vous en tant que :");
        ButtonType artisteBtn = new ButtonType("Artiste");
        ButtonType investisseurBtn = new ButtonType("Investisseur");
        alert.getButtonTypes().setAll(artisteBtn, investisseurBtn, ButtonType.CANCEL);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait().ifPresent(result -> {
            if (result == artisteBtn) {
                setRole(Role.ARTISTE);
            } else if (result == investisseurBtn) {
                setRole(Role.INVESTISSEUR);
            } else {
                setRole(Role.INVESTISSEUR);
            }
        });
    }

    private void setRole(Role role) {
        this.currentRole = role;
        if (roleLabel != null) {
            if (role == Role.ARTISTE) {
                roleLabel.setText("Connecté comme Artiste (ID=1)");
            } else {
                roleLabel.setText("Connecté comme Investisseur (ID=2)");
            }
        }
        configurePermissions();
        reloadData();
    }

    private void configurePermissions() {
        boolean isArtiste = currentRole == Role.ARTISTE;

        // Artiste : ne voit que les actions liées au projet
        if (isArtiste) {
            btnNewProjet.setVisible(true);
            btnNewProjet.setManaged(true);
            btnEditProjet.setVisible(true);
            btnEditProjet.setManaged(true);
            btnDeleteProjet.setVisible(true);
            btnDeleteProjet.setManaged(true);

            btnInvestir.setVisible(false);
            btnInvestir.setManaged(false);
            btnAddFavori.setVisible(false);
            btnAddFavori.setManaged(false);
            btnRemoveFavori.setVisible(false);
            btnRemoveFavori.setManaged(false);

            // L'artiste ne fait que consulter les investissements
            btnEditInvestissement.setVisible(false);
            btnEditInvestissement.setManaged(false);
            btnDeleteInvestissement.setVisible(false);
            btnDeleteInvestissement.setManaged(false);
        } else {
            // Investisseur : ne gère que ses investissements et l'action "Investir"
            btnNewProjet.setVisible(false);
            btnNewProjet.setManaged(false);
            btnEditProjet.setVisible(false);
            btnEditProjet.setManaged(false);
            btnDeleteProjet.setVisible(false);
            btnDeleteProjet.setManaged(false);

            btnInvestir.setVisible(true);
            btnInvestir.setManaged(true);
            btnAddFavori.setVisible(true);
            btnAddFavori.setManaged(true);
            btnRemoveFavori.setVisible(true);
            btnRemoveFavori.setManaged(true);

            btnEditInvestissement.setVisible(true);
            btnEditInvestissement.setManaged(true);
            btnDeleteInvestissement.setVisible(true);
            btnDeleteInvestissement.setManaged(true);
        }
    }

    private void reloadData() {
        projetsData.clear();
        investissementsData.clear();
        favorisData.clear();

        if (currentRole == Role.ARTISTE) {
            projetsData.addAll(projetService.afficherParArtiste(ARTISTE_ID));
            // investissements liés aux projets de l'artiste
            for (ProjetArtistique p : projetsData) {
                investissementsData.addAll(investissementService.afficherParProjet(p.getIdProjet()));
            }
        } else {
            // investisseur voit tous les projets visibles
            projetsData.addAll(projetService.afficherEntite());
            investissementsData.addAll(investissementService.afficherParInvestisseur(INVESTISSEUR_ID));
            favorisData.addAll(new org.pi.gestionprojet.service.FavoriService()
                    .afficherFavorisParInvestisseur(INVESTISSEUR_ID));
        }

        refreshProjetCards();
        refreshInvestissementCards();
        refreshFavorisCards();
    }

    private void refreshProjetCards() {
        projetsContainer.getChildren().clear();
        selectedProjet = null;
        selectedProjetCard = null;

        for (ProjetArtistique p : projetsData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/pi/gestionprojet/ProjetCard.fxml"));
                Node card = loader.load();
                ProjetCardController controller = loader.getController();
                boolean isOwned = p.getIdArtiste() == ARTISTE_ID;
                controller.setData(p, isOwned);

                card.getStyleClass().add("clickable-card");
                card.setOnMouseClicked(e -> selectProjetCard(card, p));

                projetsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshInvestissementCards() {
        investissementsContainer.getChildren().clear();
        selectedInvestissement = null;
        selectedInvestissementCard = null;

        for (Investissement inv : investissementsData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/pi/gestionprojet/InvestissementCard.fxml"));
                Node card = loader.load();
                InvestissementCardController controller = loader.getController();
                controller.setData(inv);

                card.getStyleClass().add("clickable-card");
                card.setOnMouseClicked(e -> selectInvestissementCard(card, inv));

                investissementsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshFavorisCards() {
        if (favorisContainer == null) return;
        favorisContainer.getChildren().clear();

        for (ProjetArtistique p : favorisData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/pi/gestionprojet/ProjetCard.fxml"));
                Node card = loader.load();
                ProjetCardController controller = loader.getController();
                boolean isOwned = p.getIdArtiste() == ARTISTE_ID;
                controller.setData(p, isOwned);

                card.getStyleClass().add("clickable-card");
                card.setOnMouseClicked(e -> selectProjetCard(card, p));

                favorisContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectProjetCard(Node card, ProjetArtistique projet) {
        if (selectedProjetCard != null) {
            selectedProjetCard.getStyleClass().remove("card-selected");
        }
        selectedProjetCard = card;
        selectedProjet = projet;
        if (!card.getStyleClass().contains("card-selected")) {
            card.getStyleClass().add("card-selected");
        }
    }

    private void selectInvestissementCard(Node card, Investissement inv) {
        if (selectedInvestissementCard != null) {
            selectedInvestissementCard.getStyleClass().remove("card-selected");
        }
        selectedInvestissementCard = card;
        selectedInvestissement = inv;
        if (!card.getStyleClass().contains("card-selected")) {
            card.getStyleClass().add("card-selected");
        }
    }

    @FXML
    private void onChangeRoleClicked(ActionEvent event) {
        chooseInitialRole();
    }

    @FXML
    private void onNewProjet(ActionEvent event) {
        if (currentRole != Role.ARTISTE) {
            showWarning("Seul un artiste peut créer un projet.");
            return;
        }
        ProjetArtistique p = showProjetDialog(null);
        if (p != null) {
            p.setIdArtiste(ARTISTE_ID);
            projetService.ajouterEntite(p);
            reloadData();
        }
    }

    @FXML
    private void onEditProjet(ActionEvent event) {
        ProjetArtistique selected = selectedProjet;
        if (selected == null) {
            showWarning("Veuillez sélectionner un projet à modifier.");
            return;
        }
        if (currentRole == Role.ARTISTE && selected.getIdArtiste() != ARTISTE_ID) {
            showWarning("Vous ne pouvez modifier que vos propres projets.");
            return;
        }
        ProjetArtistique updated = showProjetDialog(selected);
        if (updated != null) {
            projetService.modifierEntite(updated);
            reloadData();
        }
    }

    @FXML
    private void onDeleteProjet(ActionEvent event) {
        ProjetArtistique selected = selectedProjet;
        if (selected == null) {
            showWarning("Veuillez sélectionner un projet à supprimer.");
            return;
        }
        if (currentRole == Role.ARTISTE && selected.getIdArtiste() != ARTISTE_ID) {
            showWarning("Vous ne pouvez supprimer que vos propres projets.");
            return;
        }
        if (confirm("Supprimer ce projet ? Les investissements associés seront aussi supprimés.")) {
            projetService.supprimerEntite(selected);
            reloadData();
        }
    }

    @FXML
    private void onInvestir(ActionEvent event) {
        if (currentRole != Role.INVESTISSEUR) {
            showWarning("Seul un investisseur peut investir.");
            return;
        }
        ProjetArtistique projet = selectedProjet;
        if (projet == null) {
            showWarning("Veuillez sélectionner un projet pour investir.");
            return;
        }
        Investissement inv = showInvestissementDialog(null, projet);
        if (inv != null) {
            inv.setIdInvestisseur(INVESTISSEUR_ID);
            inv.setIdProjet(projet.getIdProjet());
            investissementService.ajouterEntite(inv);
            reloadData();
        }
    }

    @FXML
    private void onEditInvestissement(ActionEvent event) {
        Investissement selected = selectedInvestissement;
        if (selected == null) {
            showWarning("Veuillez sélectionner un investissement à modifier.");
            return;
        }
        if (currentRole == Role.INVESTISSEUR && selected.getIdInvestisseur() != INVESTISSEUR_ID) {
            showWarning("Vous ne pouvez modifier que vos propres investissements.");
            return;
        }
        if (currentRole == Role.ARTISTE) {
            showWarning("Un artiste ne peut pas modifier un investissement.");
            return;
        }
        // For simplicity allow investor to change only montant, moyen, statut, palier, message
        Investissement updated = showInvestissementDialog(selected, null);
        if (updated != null) {
            investissementService.modifierEntite(updated);
            reloadData();
        }
    }

    @FXML
    private void onDeleteInvestissement(ActionEvent event) {
        Investissement selected = selectedInvestissement;
        if (selected == null) {
            showWarning("Veuillez sélectionner un investissement à supprimer.");
            return;
        }
        if (currentRole == Role.INVESTISSEUR && selected.getIdInvestisseur() != INVESTISSEUR_ID) {
            showWarning("Vous ne pouvez supprimer que vos propres investissements.");
            return;
        }
        if (currentRole == Role.ARTISTE) {
            showWarning("Un artiste ne peut pas supprimer un investissement.");
            return;
        }
        if (confirm("Supprimer cet investissement ?")) {
            investissementService.supprimerEntite(selected);
            reloadData();
        }
    }

    @FXML
    private void onAddFavori(ActionEvent event) {
        if (currentRole != Role.INVESTISSEUR) {
            return;
        }
        if (selectedProjet == null) {
            showWarning("Veuillez sélectionner un projet à ajouter aux favoris.");
            return;
        }
        org.pi.gestionprojet.service.FavoriService favoriService = new org.pi.gestionprojet.service.FavoriService();
        favoriService.ajouterFavori(INVESTISSEUR_ID, selectedProjet.getIdProjet());
        reloadData();
    }

    @FXML
    private void onRemoveFavori(ActionEvent event) {
        if (currentRole != Role.INVESTISSEUR) {
            return;
        }
        if (selectedProjet == null) {
            showWarning("Veuillez sélectionner un projet à retirer des favoris.");
            return;
        }
        org.pi.gestionprojet.service.FavoriService favoriService = new org.pi.gestionprojet.service.FavoriService();
        favoriService.supprimerFavori(INVESTISSEUR_ID, selectedProjet.getIdProjet());
        reloadData();
    }

    @FXML
    private void onShowProjets(ActionEvent event) {
        showProjetsView();
    }

    @FXML
    private void onShowInvestissements(ActionEvent event) {
        showInvestissementsView();
    }

    @FXML
    private void onShowFavoris(ActionEvent event) {
        showFavorisView();
    }

    private void showProjetsView() {
        projetsScroll.setVisible(true);
        investissementsScroll.setVisible(false);
        favorisScroll.setVisible(false);
        if (btnViewProjets != null) {
            btnViewProjets.setSelected(true);
        }
        if (btnViewInvestissements != null) {
            btnViewInvestissements.setSelected(false);
        }
        if (btnViewFavoris != null) {
            btnViewFavoris.setSelected(false);
        }
    }

    private void showInvestissementsView() {
        projetsScroll.setVisible(false);
        investissementsScroll.setVisible(true);
        favorisScroll.setVisible(false);
        if (btnViewProjets != null) {
            btnViewProjets.setSelected(false);
        }
        if (btnViewInvestissements != null) {
            btnViewInvestissements.setSelected(true);
        }
        if (btnViewFavoris != null) {
            btnViewFavoris.setSelected(false);
        }
    }

    private void showFavorisView() {
        projetsScroll.setVisible(false);
        investissementsScroll.setVisible(false);
        favorisScroll.setVisible(true);
        if (btnViewProjets != null) {
            btnViewProjets.setSelected(false);
        }
        if (btnViewInvestissements != null) {
            btnViewInvestissements.setSelected(false);
        }
        if (btnViewFavoris != null) {
            btnViewFavoris.setSelected(true);
        }
    }

    private ProjetArtistique showProjetDialog(ProjetArtistique existing) {
        Dialog<ProjetArtistique> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau projet artistique" : "Modifier le projet");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(5);
        TextField objectifField = new TextField();
        DatePicker dateCreationPicker = new DatePicker(LocalDate.now());
        DatePicker dateLimitePicker = new DatePicker();
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("EN_ATTENTE", "EN_COURS", "FINANCE", "ECHEC");
        CheckBox visibleCheck = new CheckBox("Visible au public");
        TextField categorieField = new TextField();

        if (existing != null) {
            titreField.setText(existing.getTitre());
            descriptionArea.setText(existing.getDescription());
            objectifField.setText(existing.getObjectifFinancier() != null ? existing.getObjectifFinancier().toPlainString() : "");
            dateCreationPicker.setValue(existing.getDateCreation());
            dateLimitePicker.setValue(existing.getDateLimite());
            statutBox.setValue(existing.getStatut());
            visibleCheck.setSelected(existing.isVisibilite());
            categorieField.setText(existing.getCategorie());
        } else {
            statutBox.setValue("EN_ATTENTE");
            visibleCheck.setSelected(true);
        }

        Button aiButton = new Button("Suggérer la description avec l'IA");
        aiButton.setOnAction(e -> {
            String titre = titreField.getText().trim();
            String draft = descriptionArea.getText().trim();
            if (titre.isEmpty() && draft.isEmpty()) {
                showWarning("Veuillez saisir au moins un titre ou un début de description avant d'utiliser l'IA.");
                return;
            }
            try {
                String suggestion = GroqAIService.suggestDescription(titre, draft);
                descriptionArea.setText(suggestion);
            } catch (Exception ex) {
                showWarning("Impossible d'obtenir une suggestion de l'IA : " + ex.getMessage());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Titre*"), titreField);
        grid.addRow(1, new Label("Description"), new javafx.scene.layout.VBox(5, descriptionArea, aiButton));
        grid.addRow(2, new Label("Objectif financier*"), objectifField);
        grid.addRow(3, new Label("Date création*"), dateCreationPicker);
        grid.addRow(4, new Label("Date limite"), dateLimitePicker);
        grid.addRow(5, new Label("Statut"), statutBox);
        grid.addRow(6, new Label("Catégorie"), categorieField);
        grid.addRow(7, new Label("Visibilité"), visibleCheck);
        pane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // validations simples
                if (titreField.getText().isBlank()) {
                    showWarning("Le titre est obligatoire.");
                    return null;
                }
                BigDecimal objectif;
                try {
                    objectif = new BigDecimal(objectifField.getText().trim());
                    if (objectif.compareTo(BigDecimal.ZERO) <= 0) {
                        showWarning("L'objectif financier doit être positif.");
                        return null;
                    }
                } catch (Exception e) {
                    showWarning("Objectif financier invalide.");
                    return null;
                }
                if (dateCreationPicker.getValue() == null) {
                    showWarning("La date de création est obligatoire.");
                    return null;
                }

                ProjetArtistique p = existing != null ? existing : new ProjetArtistique();
                p.setTitre(titreField.getText().trim());
                p.setDescription(descriptionArea.getText().trim());
                p.setObjectifFinancier(objectif);
                if (p.getMontantCollecte() == null) {
                    p.setMontantCollecte(BigDecimal.ZERO);
                }
                p.setDateCreation(dateCreationPicker.getValue());
                p.setDateLimite(dateLimitePicker.getValue());
                p.setStatut(statutBox.getValue());
                p.setVisibilite(visibleCheck.isSelected());
                p.setCategorie(categorieField.getText().trim());
                return p;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private Investissement showInvestissementDialog(Investissement existing, ProjetArtistique projetContext) {
        Dialog<Investissement> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvel investissement" : "Modifier l'investissement");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label projetLabel = new Label();
        if (projetContext != null) {
            projetLabel.setText("Projet: " + projetContext.getTitre());
        } else if (existing != null) {
            projetLabel.setText("Projet #" + existing.getIdProjet());
        }

        TextField montantField = new TextField();
        ComboBox<String> moyenBox = new ComboBox<>();
        moyenBox.getItems().addAll("CARTE", "VIREMENT", "PAYPAL");
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("EN_ATTENTE", "VALIDE", "ANNULE");
        TextField palierField = new TextField();
        TextArea messageArea = new TextArea();

        if (existing != null) {
            montantField.setText(existing.getMontant() != null ? existing.getMontant().toPlainString() : "");
            moyenBox.setValue(existing.getMoyenPaiement());
            statutBox.setValue(existing.getStatut());
            palierField.setText(existing.getPalier());
            messageArea.setText(existing.getMessageSoutien());
        } else {
            statutBox.setValue("VALIDE");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int row = 0;
        grid.addRow(row++, projetLabel);
        grid.addRow(row++, new Label("Montant*"), montantField);
        grid.addRow(row++, new Label("Moyen de paiement*"), moyenBox);
        grid.addRow(row++, new Label("Statut"), statutBox);
        grid.addRow(row++, new Label("Palier"), palierField);
        grid.addRow(row, new Label("Message de soutien"), messageArea);
        pane.setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                BigDecimal montant;
                try {
                    montant = new BigDecimal(montantField.getText().trim());
                    if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                        showWarning("Le montant doit être positif.");
                        return null;
                    }
                } catch (Exception e) {
                    showWarning("Montant invalide.");
                    return null;
                }
                if (moyenBox.getValue() == null) {
                    showWarning("Veuillez choisir un moyen de paiement.");
                    return null;
                }

                Investissement inv = existing != null ? existing : new Investissement();
                inv.setMontant(montant);
                inv.setMoyenPaiement(moyenBox.getValue());
                inv.setStatut(statutBox.getValue());
                inv.setPalier(palierField.getText().trim());
                inv.setMessageSoutien(messageArea.getText().trim());
                if (inv.getDateInvestissement() == null) {
                    inv.setDateInvestissement(LocalDateTime.now());
                }
                return inv;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
    }
}

