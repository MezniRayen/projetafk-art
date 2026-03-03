package tn.hounayda.controllers.GestionInvestissment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import tn.hounayda.entities.GestionInvestissment.Investissement;
import tn.hounayda.entities.GestionInvestissment.ProjetArtistique;
import tn.hounayda.entities.Users;
import tn.hounayda.services.GestionInvestissment.FavoriService;
import tn.hounayda.services.GestionInvestissment.InvestissementService;
import tn.hounayda.services.GestionInvestissment.ProjetArtistiqueService;
import tn.hounayda.utils.GestionInvestissment.GroqAIService;
import tn.hounayda.utils.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UIController implements Initializable {

    private enum Role {
        ARTISTE,
        INVESTISSEUR
    }

    private Users currentUser;
    private long userId;
    private final ProjetArtistiqueService projetService = new ProjetArtistiqueService();
    private final InvestissementService investissementService = new InvestissementService();
    private final FavoriService favoriService = new FavoriService();

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
    private javafx.scene.layout.VBox projetsViewBox;
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
    @FXML
    private Button btnExportInvestissementsCSV;

    @FXML
    private TextField searchProjetsField;
    @FXML
    private javafx.scene.layout.HBox searchBarBox;

    private final List<ProjetArtistique> projetsData = new ArrayList<>();
    private final List<Investissement> investissementsData = new ArrayList<>();
    private final List<ProjetArtistique> favorisData = new ArrayList<>();

    private Node selectedProjetCard;
    private ProjetArtistique selectedProjet;

    private Node selectedInvestissementCard;
    private Investissement selectedInvestissement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = Session.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getIdUser();
            roleLabel.setText("Connecté en tant que : " + currentUser.getRole() +
                    (currentUser.getTypeUser() != null ? " (" + currentUser.getTypeUser() + ")" : ""));

            // Définir le rôle par défaut basé sur typeUser
            if (currentUser.isArtiste()) {
                currentRole = Role.ARTISTE;
            } else if (currentUser.isInvestisseur()) {
                currentRole = Role.INVESTISSEUR;
            } else if (!currentUser.isAdmin()) {
                // Si non défini et pas admin, demander à l'utilisateur
                promptForUserTypeIfNeeded();
            }

            configureUIForRole();
        } else {
            redirectToLogin();
            return;
        }

        btnViewProjets.setToggleGroup(viewToggleGroup);
        btnViewInvestissements.setToggleGroup(viewToggleGroup);
        if (btnViewFavoris != null) {
            btnViewFavoris.setToggleGroup(viewToggleGroup);
        }

        if (searchProjetsField != null) {
            searchProjetsField.textProperty().addListener((o, oldVal, newVal) -> refreshProjetCards());
        }

        // Si le type n'a pas été défini et que l'utilisateur n'est pas admin, on lance la demande
        if (currentUser.getTypeUser() == null && !currentUser.isAdmin()) {
            promptForUserTypeIfNeeded();
        } else {
            configurePermissions();
            reloadData();
        }

        showProjetsView();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 600, 400);
            javafx.stage.Stage stage = (javafx.stage.Stage) roleLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion AFK'Art");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void promptForUserTypeIfNeeded() {
        if (currentUser.getTypeUser() == null && !currentUser.isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Bienvenue sur le module de financement");
            alert.setHeaderText("Comment souhaitez-vous utiliser ce module ?");

            ButtonType artisteBtn = new ButtonType("Créer des projets (Artiste)");
            ButtonType investisseurBtn = new ButtonType("Investir dans des projets (Investisseur)");
            ButtonType lesDeuxBtn = new ButtonType("Les deux");

            alert.getButtonTypes().setAll(artisteBtn, investisseurBtn, lesDeuxBtn);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alert.showAndWait().ifPresent(result -> {
                if (result == artisteBtn) {
                    currentUser.setTypeUser("ARTISTE");
                    currentRole = Role.ARTISTE;
                } else if (result == investisseurBtn) {
                    currentUser.setTypeUser("INVESTISSEUR");
                    currentRole = Role.INVESTISSEUR;
                } else if (result == lesDeuxBtn) {
                    currentUser.setTypeUser("LES_DEUX");
                    currentRole = Role.ARTISTE; // Choix par défaut, pourra être changé via le bouton
                }

                // Sauvegarder dans la base de données
                tn.hounayda.services.UserService userService = new tn.hounayda.services.UserService();
                userService.updateUserType(currentUser.getIdUser(), currentUser.getTypeUser());

                roleLabel.setText("Connecté en tant que : " + currentUser.getRole() +
                        " (" + currentUser.getTypeUser() + ")");

                configurePermissions();
                reloadData();
            });
        }
    }

    private void configureUIForRole() {
        // Si c'est un ADMIN, il voit tout
        if (currentUser.isAdmin()) {
            btnNewProjet.setVisible(true);
            btnEditProjet.setVisible(true);
            btnDeleteProjet.setVisible(true);
            btnInvestir.setVisible(true);
            btnAddFavori.setVisible(true);
            btnRemoveFavori.setVisible(true);
            btnEditInvestissement.setVisible(true);
            btnDeleteInvestissement.setVisible(true);
            btnExportInvestissementsCSV.setVisible(true);
            if (searchBarBox != null) {
                searchBarBox.setVisible(true);
                searchBarBox.setManaged(true);
            }
        } else {
            // Pour les utilisateurs normaux, la configuration est gérée par configurePermissions()
            configurePermissions();
        }
    }

    private void configurePermissions() {
        boolean isAdmin = currentUser.isAdmin();
        boolean isArtiste = isAdmin || (currentUser.getTypeUser() != null &&
                (currentUser.getTypeUser().equals("ARTISTE") || currentUser.getTypeUser().equals("LES_DEUX")));
        boolean isInvestisseur = isAdmin || (currentUser.getTypeUser() != null &&
                (currentUser.getTypeUser().equals("INVESTISSEUR") || currentUser.getTypeUser().equals("LES_DEUX")));

        // Artiste : peut créer/modifier/supprimer des projets
        btnNewProjet.setVisible(isArtiste);
        btnEditProjet.setVisible(isArtiste);
        btnDeleteProjet.setVisible(isArtiste);

        // Investisseur : peut investir et gérer les favoris
        btnInvestir.setVisible(isInvestisseur);
        btnAddFavori.setVisible(isInvestisseur);
        btnRemoveFavori.setVisible(isInvestisseur);

        // Investisseur : peut gérer ses investissements
        btnEditInvestissement.setVisible(isInvestisseur);
        btnDeleteInvestissement.setVisible(isInvestisseur);

        // Export CSV : seulement pour les artistes (pour voir les investissements sur leurs projets)
        btnExportInvestissementsCSV.setVisible(isArtiste);

        // Barre de recherche : pour les investisseurs
        if (searchBarBox != null) {
            searchBarBox.setVisible(isInvestisseur);
        }
    }

    private void reloadData() {
        projetsData.clear();
        investissementsData.clear();
        favorisData.clear();

        if (currentUser.isAdmin()) {
            // Admin voit tout
            projetsData.addAll(projetService.afficherEntite());
            investissementsData.addAll(investissementService.afficherEntite());
            favorisData.addAll(favoriService.afficherEntite());
        } else {
            // Utilisateur normal
            if (currentUser.isArtiste()) {
                projetsData.addAll(projetService.afficherParArtiste((int) userId));
                // Investissements liés aux projets de l'artiste
                for (ProjetArtistique p : projetsData) {
                    investissementsData.addAll(investissementService.afficherParProjet(p.getIdProjet()));
                }
            }

            if (currentUser.isInvestisseur()) {
                // Ajouter les projets visibles si pas déjà ajoutés
                if (!currentUser.isArtiste()) {
                    projetsData.addAll(projetService.afficherEntite());
                }
                // Ses propres investissements
                investissementsData.addAll(investissementService.afficherParInvestisseur((int) userId));
                // Ses favoris
                favorisData.addAll(favoriService.afficherFavorisParInvestisseur((int) userId));
            }
        }

        refreshProjetCards();
        refreshInvestissementCards();
        refreshFavorisCards();
    }

    private List<ProjetArtistique> getFilteredProjetsForDisplay() {
        if (searchProjetsField == null || searchProjetsField.getText().isEmpty()) {
            return projetsData;
        }
        String q = searchProjetsField.getText().trim().toLowerCase();
        List<ProjetArtistique> filtered = new ArrayList<>();
        for (ProjetArtistique p : projetsData) {
            String titre = p.getTitre() != null ? p.getTitre().toLowerCase() : "";
            String cat = p.getCategorie() != null ? p.getCategorie().toLowerCase() : "";
            String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
            if (titre.contains(q) || cat.contains(q) || desc.contains(q)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private void refreshProjetCards() {
        projetsContainer.getChildren().clear();
        selectedProjet = null;
        selectedProjetCard = null;

        List<ProjetArtistique> toShow = getFilteredProjetsForDisplay();
        for (ProjetArtistique p : toShow) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProjetCard.fxml"));
                Node card = loader.load();
                ProjetCardController controller = loader.getController();
                boolean isOwned = p.getIdArtiste() == (int) userId;
                boolean isInvestisseur = currentUser.isInvestisseur();

                // Appel correct de setProjet avec 4 paramètres
                controller.setProjet(p, isOwned, currentUser.isAdmin(), isInvestisseur);

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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InvestissementCard.fxml"));
                Node card = loader.load();
                InvestissementCardController controller = loader.getController();
                controller.setInvestissement(inv);

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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProjetCard.fxml"));
                Node card = loader.load();
                ProjetCardController controller = loader.getController();
                boolean isOwned = p.getIdArtiste() == (int) userId;
                boolean isInvestisseur = currentUser.isInvestisseur();

                controller.setProjet(p, isOwned, currentUser.isAdmin(), isInvestisseur);

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
        card.getStyleClass().add("card-selected");
    }

    private void selectInvestissementCard(Node card, Investissement inv) {
        if (selectedInvestissementCard != null) {
            selectedInvestissementCard.getStyleClass().remove("card-selected");
        }
        selectedInvestissementCard = card;
        selectedInvestissement = inv;
        card.getStyleClass().add("card-selected");
    }

    @FXML
    private void onChangeRoleClicked(ActionEvent event) {
        // Permettre de changer temporairement le rôle sans modifier la base
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Changer de rôle");
        alert.setHeaderText("Passer en mode :");

        ButtonType artisteBtn = new ButtonType("Artiste");
        ButtonType investisseurBtn = new ButtonType("Investisseur");

        alert.getButtonTypes().setAll(artisteBtn, investisseurBtn, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(result -> {
            if (result == artisteBtn && (currentUser.isAdmin() || currentUser.isArtiste())) {
                currentRole = Role.ARTISTE;
            } else if (result == investisseurBtn && (currentUser.isAdmin() || currentUser.isInvestisseur())) {
                currentRole = Role.INVESTISSEUR;
            } else {
                return;
            }

            configurePermissions();
            reloadData();
            showProjetsView();
        });
    }

    @FXML
    private void onNewProjet(ActionEvent event) {
        if (!currentUser.isAdmin() && !currentUser.isArtiste()) {
            showWarning("Seul un artiste peut créer un projet.");
            return;
        }
        ProjetArtistique p = showProjetDialog(null);
        if (p != null) {
            p.setIdArtiste((int) userId);
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
        if (!currentUser.isAdmin() && selected.getIdArtiste() != (int) userId) {
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
        if (!currentUser.isAdmin() && selected.getIdArtiste() != (int) userId) {
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
        if (!currentUser.isAdmin() && !currentUser.isInvestisseur()) {
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
            inv.setIdInvestisseur((int) userId);
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
        if (!currentUser.isAdmin() && selected.getIdInvestisseur() != (int) userId) {
            showWarning("Vous ne pouvez modifier que vos propres investissements.");
            return;
        }
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
        if (!currentUser.isAdmin() && selected.getIdInvestisseur() != (int) userId) {
            showWarning("Vous ne pouvez supprimer que vos propres investissements.");
            return;
        }
        if (confirm("Supprimer cet investissement ?")) {
            investissementService.supprimerEntite(selected);
            reloadData();
        }
    }

    @FXML
    private void onAddFavori(ActionEvent event) {
        if (!currentUser.isAdmin() && !currentUser.isInvestisseur()) {
            return;
        }
        if (selectedProjet == null) {
            showWarning("Veuillez sélectionner un projet à ajouter aux favoris.");
            return;
        }
        favoriService.ajouterFavori((int) userId, selectedProjet.getIdProjet());
        reloadData();
    }

    @FXML
    private void onRemoveFavori(ActionEvent event) {
        if (!currentUser.isAdmin() && !currentUser.isInvestisseur()) {
            return;
        }
        if (selectedProjet == null) {
            showWarning("Veuillez sélectionner un projet à retirer des favoris.");
            return;
        }
        favoriService.supprimerFavori((int) userId, selectedProjet.getIdProjet());
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

    @FXML
    private void onExportInvestissementsCSV(ActionEvent event) {
        if (investissementsData.isEmpty()) {
            showWarning("Aucun investissement à exporter.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les investissements en CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("investissements.csv");

        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        Map<Integer, String> projetTitres = new HashMap<>();
        for (ProjetArtistique p : projetsData) {
            projetTitres.put(p.getIdProjet(), p.getTitre() != null ? p.getTitre() : "");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            w.write("\uFEFF"); // BOM for Excel UTF-8
            w.write(escapeCsv("id_investissement") + "," +
                    escapeCsv("id_projet") + "," +
                    escapeCsv("titre_projet") + "," +
                    escapeCsv("montant") + "," +
                    escapeCsv("date_investissement") + "," +
                    escapeCsv("moyen_paiement") + "," +
                    escapeCsv("statut") + "," +
                    escapeCsv("palier") + "," +
                    escapeCsv("message_soutien"));
            w.newLine();

            for (Investissement inv : investissementsData) {
                String titre = projetTitres.getOrDefault(inv.getIdProjet(), "");
                String dateStr = inv.getDateInvestissement() != null ? inv.getDateInvestissement().format(dtf) : "";
                String montantStr = inv.getMontant() != null ? inv.getMontant().toPlainString() : "";

                w.write(escapeCsv(String.valueOf(inv.getIdInvestissement())) + "," +
                        escapeCsv(String.valueOf(inv.getIdProjet())) + "," +
                        escapeCsv(titre) + "," +
                        escapeCsv(montantStr) + "," +
                        escapeCsv(dateStr) + "," +
                        escapeCsv(inv.getMoyenPaiement()) + "," +
                        escapeCsv(inv.getStatut()) + "," +
                        escapeCsv(inv.getPalier()) + "," +
                        escapeCsv(inv.getMessageSoutien()));
                w.newLine();
            }

            showInfo("Export réussi", "Fichier enregistré : " + file.getAbsolutePath());

        } catch (Exception e) {
            showWarning("Erreur lors de l'export : " + e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains("\"") || value.contains(",") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showProjetsView() {
        if (projetsViewBox != null) {
            projetsViewBox.setVisible(true);
            projetsViewBox.setManaged(true);
        }
        investissementsScroll.setVisible(false);
        if (favorisScroll != null) {
            favorisScroll.setVisible(false);
        }
        btnViewProjets.setSelected(true);
        btnViewInvestissements.setSelected(false);
        if (btnViewFavoris != null) {
            btnViewFavoris.setSelected(false);
        }
    }

    private void showInvestissementsView() {
        if (projetsViewBox != null) {
            projetsViewBox.setVisible(false);
            projetsViewBox.setManaged(false);
        }
        investissementsScroll.setVisible(true);
        if (favorisScroll != null) {
            favorisScroll.setVisible(false);
        }
        btnViewProjets.setSelected(false);
        btnViewInvestissements.setSelected(true);
        if (btnViewFavoris != null) {
            btnViewFavoris.setSelected(false);
        }
    }

    private void showFavorisView() {
        if (projetsViewBox != null) {
            projetsViewBox.setVisible(false);
            projetsViewBox.setManaged(false);
        }
        investissementsScroll.setVisible(false);
        if (favorisScroll != null) {
            favorisScroll.setVisible(true);
        }
        btnViewProjets.setSelected(false);
        btnViewInvestissements.setSelected(false);
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
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
    }
}