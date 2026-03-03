package tn.hounayda.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/afkart_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String OPTIONS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            // Vérifie si la connexion est encore valide
            if (connection != null && !connection.isClosed() && connection.isValid(1)) {
                return connection;
            }
        } catch (SQLException ignored) {
            // Si exception → on recrée la connexion
        }

        // (Re)création de la connexion
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL + OPTIONS, USER, PASSWORD);
            System.out.println("Connexion à afkart_db (re)établie !");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Échec connexion DB : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection = null;
        }
    }
}