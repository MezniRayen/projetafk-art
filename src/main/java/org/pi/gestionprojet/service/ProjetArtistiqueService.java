package org.pi.gestionprojet.service;

import org.pi.gestionprojet.entities.ProjetArtistique;
import org.pi.gestionprojet.tools.DBconnection;
import org.pi.gestionprojet.tools.EmailService;


import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjetArtistiqueService implements ICrud<ProjetArtistique> {

    private final Connection cnx;

    public ProjetArtistiqueService() {
        this.cnx = DBconnection.getInstance().getCnx();
    }

    @Override
    public void ajouterEntite(ProjetArtistique p) {
        String sql = "INSERT INTO projet_artistique (id_artiste, titre, description, objectif_financier, montant_collecte, " +
                "date_creation, date_limite, statut, visibilite, categorie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdArtiste());
            ps.setString(2, p.getTitre());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getObjectifFinancier());
            ps.setBigDecimal(5, p.getMontantCollecte() != null ? p.getMontantCollecte() : BigDecimal.ZERO);
            ps.setDate(6, Date.valueOf(p.getDateCreation()));
            if (p.getDateLimite() != null) {
                ps.setDate(7, Date.valueOf(p.getDateLimite()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, p.getStatut());
            ps.setBoolean(9, p.isVisibilite());
            ps.setString(10, p.getCategorie());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdProjet(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ProjetArtistique> afficherEntite() {
        List<ProjetArtistique> list = new ArrayList<>();
        String sql = "SELECT * FROM projet_artistique";
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

    public List<ProjetArtistique> afficherParArtiste(int idArtiste) {
        List<ProjetArtistique> list = new ArrayList<>();
        String sql = "SELECT * FROM projet_artistique WHERE id_artiste = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idArtiste);
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
    public void modifierEntite(ProjetArtistique p) {
        String sql = "UPDATE projet_artistique SET titre = ?, description = ?, objectif_financier = ?, " +
                "montant_collecte = ?, date_creation = ?, date_limite = ?, statut = ?, visibilite = ?, categorie = ? " +
                "WHERE id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getObjectifFinancier());
            ps.setBigDecimal(4, p.getMontantCollecte() != null ? p.getMontantCollecte() : BigDecimal.ZERO);
            ps.setDate(5, Date.valueOf(p.getDateCreation()));
            if (p.getDateLimite() != null) {
                ps.setDate(6, Date.valueOf(p.getDateLimite()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, p.getStatut());
            ps.setBoolean(8, p.isVisibilite());
            ps.setString(9, p.getCategorie());
            ps.setInt(10, p.getIdProjet());
            ps.executeUpdate();

            // notifier les investisseurs qui ont ce projet en favori
            FavoriService favoriService = new FavoriService();
            if (favoriService.hasFavoriForProjet(p.getIdProjet())) {
                EmailService.sendProjetUpdated(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerEntite(ProjetArtistique p) {
        boolean hasFavori = new org.pi.gestionprojet.service.FavoriService()
                .hasFavoriForProjet(p.getIdProjet());
        String sql = "DELETE FROM projet_artistique WHERE id_projet = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getIdProjet());
            ps.executeUpdate();
            if (hasFavori) {
                EmailService.sendProjetDeleted(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

