package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Cargo;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class CargoController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiarBusqueda;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private TableView<Cargo> tableCargas;
    @FXML
    private TableColumn<Cargo, String> colIDCargo;
    @FXML
    private TableColumn<Cargo, String> colNombre;
    @FXML
    private TableColumn<Cargo, String> colDescripcion;
    @FXML
    private TableColumn<Cargo, String> colFechaCreacion;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Cargo> data = FXCollections.observableArrayList();

    private Cargo cargoSeleccionado = null;

    @FXML
    private void initialize() {
        // Configura columnas de la tabla usando los nombres correctos
        colIDCargo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdCargo())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCargo()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colFechaCreacion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaCreacion()));

        cargarTabla();

        // Evento para cargar datos al seleccionar una fila
        tableCargas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        cargoSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Cargos");
            for (Map<String, Object> fila : resultados) {
                Cargo cargo = new Cargo(
                        (int) fila.get("idCargo"),
                        (String) fila.get("nombreCargo"),
                        (String) fila.get("descripcion"),
                        fila.get("fechaCreacion") != null ? fila.get("fechaCreacion").toString() : ""
                );
                data.add(cargo);
            }
            tableCargas.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los cargos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarCarga() {
        String buscarId = txtBuscar.getText().trim();
        if (buscarId.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID del cargo a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados = repository.buscar("Cargos", "idCargo = ?", Collections.singletonList(Integer.valueOf(buscarId)));
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ningún cargo con ese ID.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            Cargo cargo = new Cargo(
                    (int) fila.get("idCargo"),
                    (String) fila.get("nombreCargo"),
                    (String) fila.get("descripcion"),
                    fila.get("fechaCreacion") != null ? fila.get("fechaCreacion").toString() : ""
            );
            cargoSeleccionado = cargo;
            tableCargas.getSelectionModel().select(buscarCargoEnTabla(cargo.getIdCargo()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar cargo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableCargas.getSelectionModel().clearSelection();
        cargoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (cargoSeleccionado != null) {
            txtNombre.setText(cargoSeleccionado.getNombreCargo());
            txtDescripcion.setText(cargoSeleccionado.getDescripcion());
        }
    }

    @FXML
    private void handleAddCarga() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombreCargo", nombre);
        datos.put("descripcion", descripcion);
        datos.put("fechaCreacion", LocalDate.now().toString());

        try {
            int filas = repository.insertar("Cargos", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Cargo agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar cargo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditCarga() {
        if (cargoSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un cargo para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombreCargo", nombre);
        datos.put("descripcion", descripcion);

        try {
            int filas = repository.actualizar(
                    "Cargos",
                    datos,
                    "idCargo = ?",
                    Collections.singletonList(cargoSeleccionado.getIdCargo())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Cargo actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar cargo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteCarga() {
        if (cargoSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un cargo para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Cargos",
                    "idCargo = ?",
                    Collections.singletonList(cargoSeleccionado.getIdCargo())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Cargo eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar cargo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Cargo buscarCargoEnTabla(int idCargo) {
        for (Cargo c : data) {
            if (c.getIdCargo() == idCargo) return c;
        }
        return null;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}