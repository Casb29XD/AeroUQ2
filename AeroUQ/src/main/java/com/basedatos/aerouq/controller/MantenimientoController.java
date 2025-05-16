package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.MantenimientoAeronave;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class MantenimientoController {

    @FXML private TextField txtIdAeronave;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtFechaMantenimiento;
    @FXML private TextField txtEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private TableView<MantenimientoAeronave> tableMantenimientos;
    @FXML private TableColumn<MantenimientoAeronave, String> colIdMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colIdAeronave;
    @FXML private TableColumn<MantenimientoAeronave, String> colDescripcion;
    @FXML private TableColumn<MantenimientoAeronave, String> colFechaMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<MantenimientoAeronave> data = FXCollections.observableArrayList();

    private MantenimientoAeronave mantenimientoSeleccionado = null;

    @FXML
    private void initialize() {
        colIdMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdMantenimiento())));
        colIdAeronave.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAeronave())));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colFechaMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaMantenimiento()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        tableMantenimientos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        mantenimientoSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("MantenimientoAeronave");
            for (Map<String, Object> fila : resultados) {
                MantenimientoAeronave mant = new MantenimientoAeronave(
                        (int) fila.get("ID_Mantenimiento"),
                        (int) fila.get("ID_Aeronave"),
                        (String) fila.get("Descripcion"),
                        fila.get("FechaMantenimiento") != null ? fila.get("FechaMantenimiento").toString() : "",
                        (String) fila.get("Estado")
                );
                data.add(mant);
            }
            tableMantenimientos.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los mantenimientos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarMantenimiento() {
        String buscarId = txtBuscar.getText().trim();
        if (buscarId.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID del mantenimiento a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados = repository.buscar("MantenimientoAeronave", "ID_Mantenimiento = ?", Collections.singletonList(Integer.valueOf(buscarId)));
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ningún mantenimiento con ese ID.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            MantenimientoAeronave mant = new MantenimientoAeronave(
                    (int) fila.get("ID_Mantenimiento"),
                    (int) fila.get("ID_Aeronave"),
                    (String) fila.get("Descripcion"),
                    fila.get("FechaMantenimiento") != null ? fila.get("FechaMantenimiento").toString() : "",
                    (String) fila.get("Estado")
            );
            mantenimientoSeleccionado = mant;
            tableMantenimientos.getSelectionModel().select(buscarMantenimientoEnTabla(mant.getIdMantenimiento()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableMantenimientos.getSelectionModel().clearSelection();
        mantenimientoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtIdAeronave.clear();
        txtDescripcion.clear();
        txtFechaMantenimiento.clear();
        txtEstado.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (mantenimientoSeleccionado != null) {
            txtIdAeronave.setText(String.valueOf(mantenimientoSeleccionado.getIdAeronave()));
            txtDescripcion.setText(mantenimientoSeleccionado.getDescripcion());
            txtFechaMantenimiento.setText(mantenimientoSeleccionado.getFechaMantenimiento());
            txtEstado.setText(mantenimientoSeleccionado.getEstado());
        }
    }

    @FXML
    private void handleAddMantenimiento() {
        String idAeronaveTxt = txtIdAeronave.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String fechaMantenimiento = txtFechaMantenimiento.getText().trim();
        String estado = txtEstado.getText().trim();

        if (idAeronaveTxt.isEmpty() || descripcion.isEmpty() || fechaMantenimiento.isEmpty() || estado.isEmpty()) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("ID_Aeronave", Integer.valueOf(idAeronaveTxt));
        datos.put("Descripcion", descripcion);
        datos.put("FechaMantenimiento", fechaMantenimiento);
        datos.put("Estado", estado);

        try {
            int filas = repository.insertar("MantenimientoAeronave", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditMantenimiento() {
        if (mantenimientoSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un mantenimiento para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String idAeronaveTxt = txtIdAeronave.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String fechaMantenimiento = txtFechaMantenimiento.getText().trim();
        String estado = txtEstado.getText().trim();

        if (idAeronaveTxt.isEmpty() || descripcion.isEmpty() || fechaMantenimiento.isEmpty() || estado.isEmpty()) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("ID_Aeronave", Integer.valueOf(idAeronaveTxt));
        datos.put("Descripcion", descripcion);
        datos.put("FechaMantenimiento", fechaMantenimiento);
        datos.put("Estado", estado);

        try {
            int filas = repository.actualizar(
                    "MantenimientoAeronave",
                    datos,
                    "ID_Mantenimiento = ?",
                    Collections.singletonList(mantenimientoSeleccionado.getIdMantenimiento())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteMantenimiento() {
        if (mantenimientoSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un mantenimiento para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "MantenimientoAeronave",
                    "ID_Mantenimiento = ?",
                    Collections.singletonList(mantenimientoSeleccionado.getIdMantenimiento())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private MantenimientoAeronave buscarMantenimientoEnTabla(int idMantenimiento) {
        for (MantenimientoAeronave m : data) {
            if (m.getIdMantenimiento() == idMantenimiento) return m;
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