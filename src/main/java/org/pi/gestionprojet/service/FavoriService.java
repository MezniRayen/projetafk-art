package org.pi.gestionprojet.service;

import org.pi.gestionprojet.entities.ProjetArtistique;
import org.pi.gestionprojet.tools.DBconnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FavoriService {

    private final Connection cnx;

    public FavoriService() {
        this.cnx = DBconnection.getInstance().getCnx();
    }

    public void ajouterFavori(int idInvestisseur, int idProjet) {
        // éviter les doublons
        String existsSql = "SELECT COUNT(*) FROM favori_projet WHERE id_investisseur = ? AND id_projet = ?";
        String insertSql = "INSERT INTO favori_projet (id_investisseur, id_projet) VALUES (?, ?)";
        try (PreparedStatement psExists = cnx.prepareStatement(existsSql)) {
            psExists.setInt(1, idInvestisseur);
            psExists.setInt(2, idProjet);
            try (ResultSet rs = psExists.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement psInsert = cnx.prepareStatement(insertSql)) {
            psInsert.setInt(1, idInvestisseur);
            psInsert.setInt(2, idProjet);
            psInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerFavori(int idInvestisseur, int idProjet) {
        String sql = "DELETE FROM favori_projet WHERE id_investisseur = ? AND id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            ps.setInt(2, idProjet);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ProjetArtistique> afficherFavorisParInvestisseur(int idInvestisseur) {
        List<ProjetArtistique> list = new ArrayList<>();
        String sql = "SELECT p.* FROM projet_artistique p " +
                "JOIN favori_projet f ON f.id_projet = p.id_projet " +
                "WHERE f.id_investisseur = ?";
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

    public boolean hasFavoriForProjet(int idProjet) {
        String sql = "SELECT COUNT(*) FROM favori_projet WHERE id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idProjet);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ProjetArtistique mapRow(ResultSet rs) throws SQLException {
        ProjetArtistique p = new ProjetArtistique();
        p.setIdProjet(rs.getInt("id_projet"));
        p.setIdArtiste(rs.getInt("id_artiste"));
        p.setTitre(rs.getString("titre"));
        p.setDescription(rs.getString("description"));
        p.setObjectifFinancier(rs.getBigDecimal("objectif_financier"));
        p.setMontantCollecte(rs.getBigDecimal("montant_collecte"));
        Date dc = rs.getDate("date_creation");
        if (dc != null) {
            p.setDateCreation(dc.toLocalDate());
        }
        Date dl = rs.getDate("date_limite");
        if (dl != null) {
            p.setDateLimite(dl.toLocalDate());
        }
        p.setStatut(rs.getString("statut"));
        p.setVisibilite(rs.getBoolean("visibilite"));
        p.setCategorie(rs.getString("categorie"));
        return p;
    }
}

