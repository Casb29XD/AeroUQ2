package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Pasajero;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class PasajerosController {

    // Campos del formulario (sin ID, porque lo genera la BD)
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumentoIdentidad;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtIdVuelo;

    // Botones de acción
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
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

    @FXML
    private void initialize() {
        colIdPasajero.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPasajero())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colDocumentoIdentidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentoIdentidad()));
        colNacionalidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNacionalidad()));
        colIdVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdVuelo())));

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

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Pasajeros");
            for (Map<String, Object> fila : resultados) {
                Pasajero p = new Pasajero(
                        (int) fila.get("ID_Pasajero"),
                        (String) fila.get("Nombre"),
                        (String) fila.get("Apellido"),
                        (String) fila.get("DocumentoIdentidad"),
                        (String) fila.get("Nacionalidad"),
                        (int) fila.get("ID_Vuelo")
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
            int idVuelo = Integer.parseInt(txtIdVuelo.getText().trim());

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
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "ID Vuelo debe ser un número válido.", Alert.AlertType.WARNING);
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
            int idVuelo = Integer.parseInt(txtIdVuelo.getText().trim());

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
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "ID Vuelo debe ser un número válido.", Alert.AlertType.WARNING);
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
        txtIdVuelo.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (pasajeroSeleccionado != null) {
            txtNombre.setText(pasajeroSeleccionado.getNombre());
            txtApellido.setText(pasajeroSeleccionado.getApellido());
            txtDocumentoIdentidad.setText(pasajeroSeleccionado.getDocumentoIdentidad());
            txtNacionalidad.setText(pasajeroSeleccionado.getNacionalidad());
            txtIdVuelo.setText(String.valueOf(pasajeroSeleccionado.getIdVuelo()));
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