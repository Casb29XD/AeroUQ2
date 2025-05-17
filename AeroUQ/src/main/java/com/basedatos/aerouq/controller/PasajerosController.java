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

public class PasajerosController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumentoIdentidad;
    @FXML private TextField txtNacionalidad;
    @FXML private ComboBox<String> comboVuelo;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

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