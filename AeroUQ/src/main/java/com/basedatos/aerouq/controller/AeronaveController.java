package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Aeronave;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;

public class AeronaveController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> comboAerolinea;
    @FXML private TextField txtModelo;
    @FXML private TextField txtMatricula;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<Aeronave> tableAeronave;
    @FXML private TableColumn<Aeronave, String> colIdAeronave;
    @FXML private TableColumn<Aeronave, String> colAerolinea;
    @FXML private TableColumn<Aeronave, String> colModelo;
    @FXML private TableColumn<Aeronave, String> colMatricula;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Aeronave> data = FXCollections.observableArrayList();
    private Aeronave aeronaveSeleccionada = null;

    // Mapas para convertir nombre <-> ID
    private Map<String, Integer> nombreAerolineaToId = new HashMap<>();
    private Map<Integer, String> idToNombreAerolinea = new HashMap<>();

    @FXML
    private void initialize() {
        colIdAeronave.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAeronave())));
        colAerolinea.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreAerolinea()));
        colModelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModelo()));
        colMatricula.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMatricula()));

        cargarAerolineasCombo();
        cargarTabla();

        tableAeronave.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        aeronaveSeleccionada = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );

        // Listener SOLO para filtrar la tabla al seleccionar una aerolínea
        comboAerolinea.setOnAction(event -> {
            String aerolinea = comboAerolinea.getValue();
            if (aerolinea == null || aerolinea.isEmpty()) {
                cargarTabla();
            } else {
                filtrarPorAerolinea(aerolinea);
            }
        });
    }

    private void cargarAerolineasCombo() {
        comboAerolinea.getItems().clear();
        nombreAerolineaToId.clear();
        idToNombreAerolinea.clear();
        try {
            List<Map<String, Object>> aerolineas = repository.buscar("Aerolineas");
            for (Map<String, Object> fila : aerolineas) {
                int id = ((Number) fila.get("ID_Aerolinea")).intValue();
                String nombre = (String) fila.get("Nombre");
                nombreAerolineaToId.put(nombre, id);
                idToNombreAerolinea.put(id, nombre);
                comboAerolinea.getItems().add(nombre);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aerolíneas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT a.ID_Aeronave, a.ID_Aerolinea, a.Modelo, a.Matricula, al.Nombre " +
                "FROM Aeronaves a JOIN Aerolineas al ON a.ID_Aerolinea = al.ID_Aerolinea";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Aeronave aero = new Aeronave(
                        rs.getInt("ID_Aeronave"),
                        rs.getInt("ID_Aerolinea"),
                        rs.getString("Modelo"),
                        rs.getString("Matricula"),
                        rs.getString("Nombre")
                );
                data.add(aero);
            }
            tableAeronave.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aeronaves:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtrarPorAerolinea(String nombreAerolinea) {
        Integer idAerolinea = nombreAerolineaToId.get(nombreAerolinea);
        if (idAerolinea == null) return;

        data.clear();
        String sql = "SELECT a.ID_Aeronave, a.ID_Aerolinea, a.Modelo, a.Matricula, al.Nombre " +
                "FROM Aeronaves a JOIN Aerolineas al ON a.ID_Aerolinea = al.ID_Aerolinea " +
                "WHERE a.ID_Aerolinea = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAerolinea);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Aeronave aero = new Aeronave(
                            rs.getInt("ID_Aeronave"),
                            rs.getInt("ID_Aerolinea"),
                            rs.getString("Modelo"),
                            rs.getString("Matricula"),
                            rs.getString("Nombre")
                    );
                    data.add(aero);
                }
            }
            tableAeronave.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aeronaves:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarAeronave() {
        String texto = txtBuscar.getText().trim();
        String sql = "SELECT a.ID_Aeronave, a.ID_Aerolinea, a.Modelo, a.Matricula, al.Nombre " +
                "FROM Aeronaves a JOIN Aerolineas al ON a.ID_Aerolinea = al.ID_Aerolinea " +
                "WHERE a.Matricula LIKE ? OR a.Modelo LIKE ? OR al.Nombre LIKE ? OR CAST(a.ID_Aeronave AS NVARCHAR) LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String like = "%" + texto + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);
            stmt.setString(4, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Aeronave aero = new Aeronave(
                            rs.getInt("ID_Aeronave"),
                            rs.getInt("ID_Aerolinea"),
                            rs.getString("Modelo"),
                            rs.getString("Matricula"),
                            rs.getString("Nombre")
                    );
                    data.add(aero);
                }
            }
            tableAeronave.setItems(data);
            if (!data.isEmpty()) {
                aeronaveSeleccionada = data.get(0);
                tableAeronave.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron aeronaves con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar aeronaves:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableAeronave.getSelectionModel().clearSelection();
        comboAerolinea.getSelectionModel().clearSelection(); // Limpia selección ComboBox
        cargarTabla();
        aeronaveSeleccionada = null;
    }

    private void limpiarCampos() {
        comboAerolinea.getSelectionModel().clearSelection();
        txtModelo.clear();
        txtMatricula.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (aeronaveSeleccionada != null) {
            String nombreAerolinea = idToNombreAerolinea.get(aeronaveSeleccionada.getIdAerolinea());
            comboAerolinea.setValue(nombreAerolinea);
            txtModelo.setText(aeronaveSeleccionada.getModelo());
            txtMatricula.setText(aeronaveSeleccionada.getMatricula());
        }
    }

    @FXML
    private void handleAddAeronave() {
        String nombreAerolinea = comboAerolinea.getValue();
        String modelo = txtModelo.getText().trim();
        String matricula = txtMatricula.getText().trim();

        Integer idAerolinea = nombreAerolineaToId.get(nombreAerolinea);

        if (nombreAerolinea == null || idAerolinea == null || modelo.isEmpty() || matricula.isEmpty()) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Modelo", modelo);
            datos.put("Matricula", matricula);

            int filas = repository.insertar("Aeronaves", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aeronave agregada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aeronaveSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar aeronave:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditAeronave() {
        if (aeronaveSeleccionada == null) {
            mostrarAlerta("Editar", "Seleccione una aeronave para editar.", Alert.AlertType.INFORMATION);
            return;
        }

        String nombreAerolinea = comboAerolinea.getValue();
        String modelo = txtModelo.getText().trim();
        String matricula = txtMatricula.getText().trim();

        Integer idAerolinea = nombreAerolineaToId.get(nombreAerolinea);

        if (nombreAerolinea == null || idAerolinea == null || modelo.isEmpty() || matricula.isEmpty()) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Modelo", modelo);
            datos.put("Matricula", matricula);

            int filas = repository.actualizar(
                    "Aeronaves",
                    datos,
                    "ID_Aeronave = ?",
                    Collections.singletonList(aeronaveSeleccionada.getIdAeronave())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aeronave actualizada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aeronaveSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar aeronave:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteAeronave() {
        if (aeronaveSeleccionada == null) {
            mostrarAlerta("Eliminar", "Seleccione una aeronave para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Aeronaves",
                    "ID_Aeronave = ?",
                    Collections.singletonList(aeronaveSeleccionada.getIdAeronave())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aeronave eliminada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aeronaveSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar aeronave:\n" + e.getMessage(), Alert.AlertType.ERROR);
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