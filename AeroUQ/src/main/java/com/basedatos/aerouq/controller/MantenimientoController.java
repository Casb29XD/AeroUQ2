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

    @FXML private TextField txtBuscar;
    @FXML private TextField txtIdAeronave;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtFechaMantenimiento;
    @FXML private ComboBox<String> comboEstado;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<MantenimientoAeronave> tableMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colIdMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colIdAeronave;
    @FXML private TableColumn<MantenimientoAeronave, String> colDesc;
    @FXML private TableColumn<MantenimientoAeronave, String> colFechaMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<MantenimientoAeronave> data = FXCollections.observableArrayList();
    private MantenimientoAeronave mantenimientoSeleccionado = null;

    private static final List<String> ESTADOS = Arrays.asList("Completado", "Pendiente", "En progreso");

    @FXML
    private void initialize() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS));

        colIdMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdMantenimiento())));
        colIdAeronave.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAeronave())));
        colDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colFechaMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaMantenimiento()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        tableMantenimiento.getSelectionModel().selectedItemProperty().addListener(
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
                        ((Number) fila.get("ID_Mantenimiento")).intValue(),
                        ((Number) fila.get("ID_Aeronave")).intValue(),
                        (String) fila.get("Descripcion"),
                        String.valueOf(fila.get("FechaMantenimiento")),
                        (String) fila.get("Estado")
                );
                data.add(mant);
            }
            tableMantenimiento.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los mantenimientos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarAeronave() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID de mantenimiento o id de aeronave.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados;
            if (texto.matches("\\d+")) {
                resultados = repository.buscar(
                        "MantenimientoAeronave",
                        "ID_Mantenimiento = ? OR ID_Aeronave = ?",
                        Arrays.asList(Integer.valueOf(texto), Integer.valueOf(texto))
                );
            } else {
                resultados = repository.buscar(
                        "MantenimientoAeronave",
                        "Descripcion LIKE ?",
                        Collections.singletonList("%" + texto + "%")
                );
            }
            data.clear();
            for (Map<String, Object> fila : resultados) {
                MantenimientoAeronave mant = new MantenimientoAeronave(
                        ((Number) fila.get("ID_Mantenimiento")).intValue(),
                        ((Number) fila.get("ID_Aeronave")).intValue(),
                        (String) fila.get("Descripcion"),
                        String.valueOf(fila.get("FechaMantenimiento")),
                        (String) fila.get("Estado")
                );
                data.add(mant);
            }
            tableMantenimiento.setItems(data);

            if (!data.isEmpty()) {
                mantenimientoSeleccionado = data.get(0);
                tableMantenimiento.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron mantenimientos con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableMantenimiento.getSelectionModel().clearSelection();
        cargarTabla();
        mantenimientoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtIdAeronave.clear();
        txtDescripcion.clear();
        txtFechaMantenimiento.clear();
        comboEstado.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (mantenimientoSeleccionado != null) {
            txtIdAeronave.setText(String.valueOf(mantenimientoSeleccionado.getIdAeronave()));
            txtDescripcion.setText(mantenimientoSeleccionado.getDescripcion());
            txtFechaMantenimiento.setText(mantenimientoSeleccionado.getFechaMantenimiento());
            comboEstado.setValue(mantenimientoSeleccionado.getEstado());
        }
    }

    @FXML
    private void handleAddMantenimiento() {
        String idAeronaveStr = txtIdAeronave.getText().trim();
        String desc = txtDescripcion.getText().trim();
        String fecha = txtFechaMantenimiento.getText().trim();
        String estado = comboEstado.getValue();

        if (idAeronaveStr.isEmpty() || desc.isEmpty() || fecha.isEmpty() || estado == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idAeronave = Integer.parseInt(idAeronaveStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aeronave", idAeronave);
            datos.put("Descripcion", desc);
            datos.put("FechaMantenimiento", fecha);
            datos.put("Estado", estado);

            int filas = repository.insertar("MantenimientoAeronave", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Agregar", "ID Aeronave debe ser numérico.", Alert.AlertType.WARNING);
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
        String idAeronaveStr = txtIdAeronave.getText().trim();
        String desc = txtDescripcion.getText().trim();
        String fecha = txtFechaMantenimiento.getText().trim();
        String estado = comboEstado.getValue();

        if (idAeronaveStr.isEmpty() || desc.isEmpty() || fecha.isEmpty() || estado == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idAeronave = Integer.parseInt(idAeronaveStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aeronave", idAeronave);
            datos.put("Descripcion", desc);
            datos.put("FechaMantenimiento", fecha);
            datos.put("Estado", estado);

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
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Editar", "ID Aeronave debe ser numérico.", Alert.AlertType.WARNING);
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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}