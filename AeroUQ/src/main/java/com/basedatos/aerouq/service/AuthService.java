package com.basedatos.aerouq.service;


import com.basedatos.aerouq.config.CreateTable;
import com.basedatos.aerouq.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    CreateTable createTable = new CreateTable();
    public boolean validarLogin(String username, String password) throws SQLException {
        if (!createTable.verificarTablaExiste("Usuarios")) {
            System.err.println("La tabla Usuarios no existe. Creándola...");
            createTable.crearTabla("Usuarios");
        }

        String sql = "SELECT COUNT(*) FROM Usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al validar el login: " + e.getMessage());
            throw new SQLException("No se pudo validar el login", e);
        }
        return false;
    }
}