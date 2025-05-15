package com.basedatos.aerouq.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String SERVER_URL = "jdbc:sqlserver://localhost:1433";
    private static final String DATABASE_NAME = "AeroUQ";
    private static final String USER = "prueba2";
    private static final String PASSWORD = "12345";

    private static final String CONNECTION_OPTIONS = ";encrypt=true;trustServerCertificate=true";

    // Método para inicializar la base de datos si no existe
    public static void inicializarBaseDatos() throws SQLException {
        String initUrl = SERVER_URL + CONNECTION_OPTIONS;
        try (Connection conn = DriverManager.getConnection(initUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = '" + DATABASE_NAME + "') " +
                    "BEGIN CREATE DATABASE " + DATABASE_NAME + " END";
            stmt.executeUpdate(sql);
            System.out.println("Base de datos verificada/creada correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            throw new SQLException("No se pudo crear/verificar la base de datos", e);
        }
    }

    // Método para obtener la conexión directamente a la base de datos
    public static Connection getConnection() throws SQLException {
        String fullUrl = SERVER_URL + ";databaseName=" + DATABASE_NAME + CONNECTION_OPTIONS;
        try {
            return DriverManager.getConnection(fullUrl, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            throw new SQLException("No se pudo conectar a la base de datos", e);
        }
    }


}
