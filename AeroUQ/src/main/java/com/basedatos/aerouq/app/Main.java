package com.basedatos.aerouq.app;

import com.basedatos.aerouq.config.DatabaseConfig;

public class Main {
    public static void main(String[] args) {
        try {
            // Inicializa la base de datos si no existe
            DatabaseConfig.inicializarBaseDatos();

            // Intenta obtener la conexión a la base de datos
            try (var connection = DatabaseConfig.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Conexión establecida correctamente con la base de datos: " + connection.getCatalog());
                } else {
                    System.out.println("❌ No se pudo establecer la conexión.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error durante la prueba de conexión: " + e.getMessage());
        }
    }
}

