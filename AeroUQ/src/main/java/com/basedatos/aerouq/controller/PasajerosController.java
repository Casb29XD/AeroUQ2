package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Pasajero;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.util.List;

// PDF
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.TextField;

public class PasajerosController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumentoIdentidad;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboVuelo;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnReporte;

    @FXML private TableView<Pasajero> tablePasajeros;
    @FXML private TableColumn<Pasajero, String> colIdPasajero;
    @FXML private TableColumn<Pasajero, String> colNombre;
    @FXML private TableColumn<Pasajero, String> colApellido;
    @FXML private TableColumn<Pasajero, String> colDocumentoIdentidad;
    @FXML private TableColumn<Pasajero, String> colNacionalidad;
    @FXML private TableColumn<Pasajero, String> colIdVuelo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Pasajero> data = FXCollections.observableArrayList();

    private Pasajero pasajeroSeleccionado = null;

    // Mapas para asociar número de vuelo <-> ID
    private Map<String, Integer> numeroVueloToId = new HashMap<>();
    private Map<Integer, String> idToNumeroVuelo = new HashMap<>();

    @FXML
    private void initialize() {
        colIdPasajero.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPasajero())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colDocumentoIdentidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colNacionalidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNacionalidad()));
        colIdVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(
                idToNumeroVuelo.getOrDefault(cellData.getValue().getIdVuelo(), String.valueOf(cellData.getValue().getIdVuelo()))
        ));

        cargarVuelosCombo();
        cargarTabla();

        tablePasajeros.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        pasajeroSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarVuelosCombo() {
        comboVuelo.getItems().clear();
        numeroVueloToId.clear();
        idToNumeroVuelo.clear();
        try {
            List<Map<String, Object>> vuelos = repository.buscar("Vuelos");
            for (Map<String, Object> fila : vuelos) {
                int idVuelo = ((Number) fila.get("ID_Vuelo")).intValue();
                String numeroVuelo = (String) fila.get("NumeroVuelo");
                numeroVueloToId.put(numeroVuelo, idVuelo);
                idToNumeroVuelo.put(idVuelo, numeroVuelo);
                comboVuelo.getItems().add(numeroVuelo);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT p.ID_Pasajero, p.Nombre, p.Apellido, p.DocumentoIdentidad, p.Nacionalidad, p.ID_Vuelo, v.NumeroVuelo " +
                "FROM Pasajeros p LEFT JOIN Vuelos v ON p.ID_Vuelo = v.ID_Vuelo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idVuelo = rs.getInt("ID_Vuelo");
                String numeroVuelo = rs.getString("NumeroVuelo");
                if (numeroVuelo != null) {
                    idToNumeroVuelo.put(idVuelo, numeroVuelo);
                }
                Pasajero p = new Pasajero(
                        rs.getInt("ID_Pasajero"),
                        rs.getString("Nombre"),
                        rs.getString("Apellido"),
                        rs.getString("DocumentoIdentidad"),
                        rs.getString("Nacionalidad"),
                        idVuelo
                );
                data.add(p);
            }
            tablePasajeros.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los pasajeros:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleFiltrarPorVuelo() {
        String numeroVuelo = comboVuelo.getValue();
        if (numeroVuelo == null || numeroVuelo.isEmpty()) {
            cargarTabla();
            return;
        }
        Integer idVuelo = numeroVueloToId.get(numeroVuelo);
        if (idVuelo == null) return;

        data.clear();
        String sql = "SELECT p.ID_Pasajero, p.Nombre, p.Apellido, p.DocumentoIdentidad, p.Nacionalidad, p.ID_Vuelo " +
                "FROM Pasajeros p WHERE p.ID_Vuelo = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVuelo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pasajero p = new Pasajero(
                            rs.getInt("ID_Pasajero"),
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nacionalidad"),
                            rs.getInt("ID_Vuelo")
                    );
                    data.add(p);
                }
            }
            tablePasajeros.setItems(data);
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo filtrar por vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleReportePdf() {
        String numeroVuelo = comboVuelo.getValue();
        if (numeroVuelo == null || numeroVuelo.isEmpty()) {
            mostrarAlerta("Reporte", "Seleccione un vuelo para generar el reporte.", Alert.AlertType.INFORMATION);
            return;
        }
        Integer idVuelo = numeroVueloToId.get(numeroVuelo);
        if (idVuelo == null) return;

        // Consulta los pasajeros del vuelo seleccionado
        String sqlPasajeros = """
            SELECT p.ID_Pasajero, p.Nombre, p.Apellido, p.DocumentoIdentidad, p.Nacionalidad
            FROM Pasajeros p
            WHERE p.ID_Vuelo = ?
        """;
        // Consulta los equipajes de un pasajero
        String sqlEquipajes = """
            SELECT ID_Maleta, CodigoDeBarras, Peso, Estado
            FROM Equipajes
            WHERE ID_Pasajero = ?
        """;

        Map<Integer, Pasajero> pasajerosMap = new LinkedHashMap<>();
        Map<Integer, List<Map<String, Object>>> equipajesMap = new HashMap<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmtPas = conn.prepareStatement(sqlPasajeros);
             PreparedStatement stmtEq = conn.prepareStatement(sqlEquipajes)
        ) {
            stmtPas.setInt(1, idVuelo);
            try (ResultSet rs = stmtPas.executeQuery()) {
                while (rs.next()) {
                    int idPas = rs.getInt("ID_Pasajero");
                    Pasajero p = new Pasajero(
                            idPas,
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nacionalidad"),
                            idVuelo
                    );
                    pasajerosMap.put(idPas, p);

                    // Equipajes de este pasajero
                    stmtEq.setInt(1, idPas);
                    try (ResultSet rsEq = stmtEq.executeQuery()) {
                        List<Map<String, Object>> equipajes = new ArrayList<>();
                        while (rsEq.next()) {
                            Map<String, Object> eq = new HashMap<>();
                            eq.put("ID_Maleta", rsEq.getInt("ID_Maleta"));
                            eq.put("CodigoDeBarras", rsEq.getString("CodigoDeBarras"));
                            eq.put("Peso", rsEq.getDouble("Peso"));
                            eq.put("Estado", rsEq.getString("Estado"));
                            equipajes.add(eq);
                        }
                        equipajesMap.put(idPas, equipajes);
                    }
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo consultar la información:\n" + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Generar el PDF
        try {
            Document document = new Document();
            String fileName = "Pasajeros_Vuelo_" + numeroVuelo + ".pdf";
            File file = new File(fileName);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Título
            document.add(new Paragraph("Reporte de Pasajeros y Equipajes - Vuelo " + numeroVuelo,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));

            // Tabla de pasajeros
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("ID Pasajero");
            table.addCell("Nombre");
            table.addCell("Apellido");
            table.addCell("Documento");
            table.addCell("Nacionalidad");

            for (Pasajero p : pasajerosMap.values()) {
                table.addCell(String.valueOf(p.getIdPasajero()));
                table.addCell(p.getNombre());
                table.addCell(p.getApellido());
                table.addCell(p.getDocumentoIdentidad());
                table.addCell(p.getNacionalidad());
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Equipajes detallados por pasajero:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            // Equipajes por pasajero (tabla)
            for (Pasajero p : pasajerosMap.values()) {
                document.add(new Paragraph(p.getNombre() + " " + p.getApellido() + " (ID: " + p.getIdPasajero() + "):"));
                List<Map<String, Object>> equipajes = equipajesMap.get(p.getIdPasajero());
                if (equipajes == null || equipajes.isEmpty()) {
                    document.add(new Paragraph("   - Sin equipaje registrado"));
                } else {
                    PdfPTable eqTable = new PdfPTable(4);
                    eqTable.setWidthPercentage(95);
                    eqTable.addCell("ID Maleta");
                    eqTable.addCell("Código de Barras");
                    eqTable.addCell("Peso (kg)");
                    eqTable.addCell("Estado");
                    for (Map<String, Object> eq : equipajes) {
                        eqTable.addCell(eq.get("ID_Maleta").toString());
                        eqTable.addCell(eq.get("CodigoDeBarras").toString());
                        eqTable.addCell(eq.get("Peso").toString());
                        eqTable.addCell(eq.get("Estado").toString());
                    }
                    document.add(eqTable);
                }
                document.add(new Paragraph(" "));
            }

            document.close();

            // Abrir el PDF automáticamente
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el PDF:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void handleLimpiar() {
        limpiarCampos();
        comboVuelo.getSelectionModel().clearSelection();
        tablePasajeros.getSelectionModel().clearSelection();
        cargarTabla();
        pasajeroSeleccionado = null;
    }

    @FXML
    private void handleAddPasajero() {
        try {
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String documento = txtDocumentoIdentidad.getText().trim();
            String nacionalidad = txtNacionalidad.getText().trim();
            String numeroVuelo = comboVuelo.getValue();
            Integer idVuelo = numeroVueloToId.get(numeroVuelo);

            if (nombre.isEmpty() || apellido.isEmpty() || documento.isEmpty() || nacionalidad.isEmpty() || numeroVuelo == null || idVuelo == null) {
                mostrarAlerta("Advertencia", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nacionalidad", nacionalidad);
            datos.put("ID_Vuelo", idVuelo);

            int filas = repository.insertar("Pasajeros", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Pasajero agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                pasajeroSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar pasajero:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditPasajero() {
        if (pasajeroSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un pasajero para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String documento = txtDocumentoIdentidad.getText().trim();
            String nacionalidad = txtNacionalidad.getText().trim();
            String numeroVuelo = comboVuelo.getValue();
            Integer idVuelo = numeroVueloToId.get(numeroVuelo);

            if (nombre.isEmpty() || apellido.isEmpty() || documento.isEmpty() || nacionalidad.isEmpty() || numeroVuelo == null || idVuelo == null) {
                mostrarAlerta("Advertencia", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nacionalidad", nacionalidad);
            datos.put("ID_Vuelo", idVuelo);

            int filas = repository.actualizar(
                    "Pasajeros",
                    datos,
                    "ID_Pasajero = ?",
                    Collections.singletonList(pasajeroSeleccionado.getIdPasajero())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Pasajero actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                pasajeroSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar pasajero:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeletePasajero() {
        if (pasajeroSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un pasajero para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Pasajeros",
                    "ID_Pasajero = ?",
                    Collections.singletonList(pasajeroSeleccionado.getIdPasajero())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Pasajero eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                pasajeroSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar pasajero:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtDocumentoIdentidad.clear();
        txtNacionalidad.clear();
        comboVuelo.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (pasajeroSeleccionado != null) {
            txtNombre.setText(pasajeroSeleccionado.getNombre());
            txtApellido.setText(pasajeroSeleccionado.getApellido());
            txtDocumentoIdentidad.setText(pasajeroSeleccionado.getDocumentoIdentidad());
            txtNacionalidad.setText(pasajeroSeleccionado.getNacionalidad());
            comboVuelo.setValue(idToNumeroVuelo.get(pasajeroSeleccionado.getIdVuelo()));
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}