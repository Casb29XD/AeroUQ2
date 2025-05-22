package com.basedatos.aerouq.repository;

import com.basedatos.aerouq.config.CreateTable;
import com.basedatos.aerouq.config.DatabaseConfig;

import java.sql.*;
import java.util.*;

public class DatabaseRepository {
    private final CreateTable createTable;

    public DatabaseRepository() {
        this.createTable = new CreateTable();
    }

    private void validarNombreTabla(String tabla) {
        if (!tabla.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Nombre de tabla no válido: " + tabla);
        }
    }

    public int insertar(String tabla, Map<String, Object> datos) throws SQLException {
        validarNombreTabla(tabla);

        if (datos.isEmpty()) {
            throw new IllegalArgumentException("No se proporcionaron datos para insertar.");
        }

        if (!createTable.verificarTablaExiste(tabla)) {
            System.err.println("La tabla " + tabla + " no existe. Creándola...");
            createTable.crearTabla(tabla);
        }

        StringBuilder columnas = new StringBuilder();
        StringBuilder valores = new StringBuilder();

        for (String columna : datos.keySet()) {
            columnas.append(columna).append(", ");
            valores.append("?, ");
        }

        columnas.setLength(columnas.length() - 2);
        valores.setLength(valores.length() - 2);

        String sql = "INSERT INTO " + tabla + " (" + columnas + ") VALUES (" + valores + ")";

        try (Connection conn = Objects.requireNonNull(DatabaseConfig.getConnection());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Object valor : datos.values()) {
                stmt.setObject(index++, valor);
            }

            int filasInsertadas = stmt.executeUpdate();
            System.out.println("Datos insertados correctamente en la tabla " + tabla);
            return filasInsertadas;
        } catch (SQLException e) {
            System.err.println("Error al insertar en la tabla " + tabla + ": " + e.getMessage());
            throw e;
        }
    }

    public int actualizar(String tabla, Map<String, Object> datos, String condicion, List<Object> parametrosCondicion) throws SQLException {
        validarNombreTabla(tabla);

        if (datos.isEmpty()) {
            throw new IllegalArgumentException("No se proporcionaron datos para actualizar.");
        }

        StringBuilder setClause = new StringBuilder();
        for (String columna : datos.keySet()) {
            setClause.append(columna).append(" = ?, ");
        }
        setClause.setLength(setClause.length() - 2);

        String sql = "UPDATE " + tabla + " SET " + setClause + " WHERE " + condicion;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Object valor : datos.values()) {
                stmt.setObject(index++, valor);
            }

            for (Object param : parametrosCondicion) {
                stmt.setObject(index++, param);
            }

            int filas = stmt.executeUpdate();
            System.out.println("Filas actualizadas: " + filas);
            return filas;
        }
    }

    public int eliminar(String tabla, String condicion, List<Object> parametrosCondicion) throws SQLException {
        validarNombreTabla(tabla);

        String sql = "DELETE FROM " + tabla + " WHERE " + condicion;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametrosCondicion.size(); i++) {
                stmt.setObject(i + 1, parametrosCondicion.get(i));
            }

            int filas = stmt.executeUpdate();
            System.out.println("Filas eliminadas: " + filas);
            return filas;
        }
    }

    public List<Map<String, Object>> buscar(String tabla, String condicion, List<Object> parametrosCondicion) throws SQLException {
        validarNombreTabla(tabla);

        String sql = "SELECT * FROM " + tabla + " WHERE " + condicion;
        List<Map<String, Object>> resultados = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametrosCondicion.size(); i++) {
                stmt.setObject(i + 1, parametrosCondicion.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= columnas; i++) {
                        fila.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    resultados.add(fila);
                }
            }
        }

        return resultados;
    }

    // Agrega este método en tu clase DatabaseRepository:

    public List<Map<String, Object>> buscarPorConsulta(String sql, List<Object> parametros) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= columnas; i++) {
                        fila.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    resultados.add(fila);
                }
            }
        }
        return resultados;
    }

    public List<Map<String, Object>> buscar(String tabla) throws SQLException {
        return buscar(tabla, "1=1", Collections.emptyList());
    }
}
