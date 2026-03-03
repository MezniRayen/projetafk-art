package tn.hounayda.services.GestionInvestissment;

import tn.hounayda.entities.GestionInvestissment.ProjetArtistique;
import tn.hounayda.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriService {

    private final Connection cnx;

    public FavoriService() {
        this.cnx = DatabaseConnection.getConnection();
    }

    // Méthodes existantes
    public boolean ajouterFavori(int idInvestisseur, int idProjet) {
        String sql = "INSERT INTO favori_projet (id_investisseur, id_projet) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            ps.setInt(2, idProjet);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerFavori(int idInvestisseur, int idProjet) {
        String sql = "DELETE FROM favori_projet WHERE id_investisseur = ? AND id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            ps.setInt(2, idProjet);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NOUVELLE MÉTHODE : Vérifier si un projet est en favori
    public boolean estFavori(int idInvestisseur, int idProjet) {
        String sql = "SELECT COUNT(*) FROM favori_projet WHERE id_investisseur = ? AND id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            ps.setInt(2, idProjet);
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

    public List<ProjetArtistique> afficherFavorisParInvestisseur(int idInvestisseur) {
        List<ProjetArtistique> favoris = new ArrayList<>();
        String sql = "SELECT p.* FROM projet_artistique p " +
                "JOIN favori_projet f ON p.id_projet = f.id_projet " +
                "WHERE f.id_investisseur = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idInvestisseur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProjetArtistique p = new ProjetArtistique();
                    p.setIdProjet(rs.getInt("id_projet"));
                    p.setIdArtiste(rs.getInt("id_artiste"));
                    p.setTitre(rs.getString("titre"));
                    p.setDescription(rs.getString("description"));
                    p.setObjectifFinancier(rs.getBigDecimal("objectif_financier"));
                    p.setMontantCollecte(rs.getBigDecimal("montant_collecte"));
                    p.setDateCreation(rs.getDate("date_creation").toLocalDate());
                    Date dl = rs.getDate("date_limite");
                    if (dl != null) p.setDateLimite(dl.toLocalDate());
                    p.setStatut(rs.getString("statut"));
                    p.setVisibilite(rs.getBoolean("visibilite"));
                    p.setCategorie(rs.getString("categorie"));
                    favoris.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
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

    public List<ProjetArtistique> afficherEntite() {
        List<ProjetArtistique> favoris = new ArrayList<>();
        String sql = "SELECT p.* FROM projet_artistique p " +
                "JOIN favori_projet f ON p.id_projet = f.id_projet";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ProjetArtistique p = new ProjetArtistique();
                p.setIdProjet(rs.getInt("id_projet"));
                p.setIdArtiste(rs.getInt("id_artiste"));
                p.setTitre(rs.getString("titre"));
                p.setDescription(rs.getString("description"));
                p.setObjectifFinancier(rs.getBigDecimal("objectif_financier"));
                p.setMontantCollecte(rs.getBigDecimal("montant_collecte"));
                p.setDateCreation(rs.getDate("date_creation").toLocalDate());
                Date dl = rs.getDate("date_limite");
                if (dl != null) p.setDateLimite(dl.toLocalDate());
                p.setStatut(rs.getString("statut"));
                p.setVisibilite(rs.getBoolean("visibilite"));
                p.setCategorie(rs.getString("categorie"));
                favoris.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
    }
}