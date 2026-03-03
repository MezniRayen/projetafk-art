package org.pi.gestionprojet.service;

import org.pi.gestionprojet.entities.Investissement;
import org.pi.gestionprojet.tools.DBconnection;
import org.pi.gestionprojet.tools.EmailService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvestissementService implements ICrud<Investissement> {

    private final Connection cnx;

    public InvestissementService() {
        this.cnx = DBconnection.getInstance().getCnx();
    }

    @Override
    public void ajouterEntite(Investissement inv) {
        String sql = "INSERT INTO investissement (id_investisseur, id_projet, montant, moyen_paiement, statut, palier, message_soutien) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getIdInvestisseur());
            ps.setInt(2, inv.getIdProjet());
            ps.setBigDecimal(3, inv.getMontant());
            ps.setString(4, inv.getMoyenPaiement());
            ps.setString(5, inv.getStatut());
            ps.setString(6, inv.getPalier());
            ps.setString(7, inv.getMessageSoutien());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    inv.setIdInvestissement(rs.getInt(1));
                }
            }

            // mettre à jour le montant collecté du projet
            recalcMontantCollecte(inv.getIdProjet());

            // notifier les investisseurs qui ont ce projet en favori
            FavoriService favoriService = new FavoriService();
            if (favoriService.hasFavoriForProjet(inv.getIdProjet())) {
                // recharger quelques infos du projet pour le mail
                ProjetArtistiqueService projetService = new ProjetArtistiqueService();
                for (org.pi.gestionprojet.entities.ProjetArtistique p : projetService.afficherEntite()) {
                    if (p.getIdProjet() == inv.getIdProjet()) {
                        EmailService.sendNewInvestissement(p, inv);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Investissement> afficherEntite() {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Investissement> afficherParInvestisseur(int idInvestisseur) {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement WHERE id_investisseur = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Investissement> afficherParProjet(int idProjet) {
        List<Investissement> list = new ArrayList<>();
        String sql = "SELECT * FROM investissement WHERE id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idProjet);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void modifierEntite(Investissement inv) {
        String sql = "UPDATE investissement SET montant = ?, moyen_paiement = ?, statut = ?, palier = ?, message_soutien = ? " +
                "WHERE id_investissement = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBigDecimal(1, inv.getMontant());
            ps.setString(2, inv.getMoyenPaiement());
            ps.setString(3, inv.getStatut());
            ps.setString(4, inv.getPalier());
            ps.setString(5, inv.getMessageSoutien());
            ps.setInt(6, inv.getIdInvestissement());
            ps.executeUpdate();

            recalcMontantCollecte(inv.getIdProjet());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerEntite(Investissement inv) {
        String sql = "DELETE FROM investissement WHERE id_investissement = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, inv.getIdInvestissement());
            ps.executeUpdate();

            recalcMontantCollecte(inv.getIdProjet());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recalcule le montant_collecte du projet à partir de la somme des investissements.
     */
    private void recalcMontantCollecte(int idProjet) {
        String sumSql = "SELECT COALESCE(SUM(montant), 0) AS total FROM investissement WHERE id_projet = ?";
        String updateSql = "UPDATE projet_artistique SET montant_collecte = ? WHERE id_projet = ?";
        try (PreparedStatement psSum = cnx.prepareStatement(sumSql)) {
            psSum.setInt(1, idProjet);
            try (ResultSet rs = psSum.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    try (PreparedStatement psUpdate = cnx.prepareStatement(updateSql)) {
                        psUpdate.setBigDecimal(1, total);
                        psUpdate.setInt(2, idProjet);
                        psUpdate.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Investissement mapRow(ResultSet rs) throws SQLException {
        Investissement inv = new Investissement();
        inv.setIdInvestissement(rs.getInt("id_investissement"));
        inv.setIdInvestisseur(rs.getInt("id_investisseur"));
        inv.setIdProjet(rs.getInt("id_projet"));
        inv.setMontant(rs.getBigDecimal("montant"));
        Timestamp ts = rs.getTimestamp("date_investissement");
        if (ts != null) {
            inv.setDateInvestissement(ts.toLocalDateTime());
        }
        inv.setMoyenPaiement(rs.getString("moyen_paiement"));
        inv.setStatut(rs.getString("statut"));
        inv.setPalier(rs.getString("palier"));
        inv.setMessageSoutien(rs.getString("message_soutien"));
        return inv;
    }
}

