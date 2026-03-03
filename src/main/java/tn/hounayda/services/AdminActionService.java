package tn.hounayda.services;

import tn.hounayda.utils.DatabaseConnection;
import tn.hounayda.entities.AdminAction;
import tn.hounayda.entities.ActionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminActionService {

    public void createAction(AdminAction action) {
        String sql = "INSERT INTO admin_actions (idAdmin, action, description, expirationDate) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, action.getIdAdmin());
            pstmt.setString(2, action.getAction());
            pstmt.setString(3, action.getDescription());
            pstmt.setTimestamp(4, new Timestamp(action.getExpirationDate().getTime()));

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                action.setIdAction(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur création action", e);
        }
    }

    public List<AdminAction> getAllActions() {
        List<AdminAction> actions = new ArrayList<>();
        String sql = "SELECT * FROM admin_actions ORDER BY dateAction DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AdminAction action = new AdminAction();
                action.setIdAction(rs.getLong("idAction"));
                action.setIdAdmin(rs.getLong("idAdmin"));
                action.setAction(rs.getString("action"));
                action.setDateAction(rs.getTimestamp("dateAction"));
                action.setDescription(rs.getString("description"));
                action.setExpirationDate(rs.getTimestamp("expirationDate"));
                // Pas de setStatus ici → getStatus() le calcule dynamiquement
                actions.add(action);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    public AdminAction getActionById(long id) {
        AdminAction action = null;
        String sql = "SELECT * FROM admin_actions WHERE idAction = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                action = new AdminAction();
                action.setIdAction(rs.getLong("idAction"));
                action.setIdAdmin(rs.getLong("idAdmin"));
                action.setAction(rs.getString("action"));
                action.setDateAction(rs.getTimestamp("dateAction"));
                action.setDescription(rs.getString("description"));
                action.setExpirationDate(rs.getTimestamp("expirationDate"));
                // Statut dynamique → pas de setStatus()
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return action;
    }

    public void updateAction(AdminAction action) {
        String sql = "UPDATE admin_actions SET action = ?, description = ?, expirationDate = ? WHERE idAction = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, action.getAction());
            pstmt.setString(2, action.getDescription());
            pstmt.setTimestamp(3, new Timestamp(action.getExpirationDate().getTime()));
            pstmt.setLong(4, action.getIdAction());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAction(long id) {
        String sql = "DELETE FROM admin_actions WHERE idAction = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}