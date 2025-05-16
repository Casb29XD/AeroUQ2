package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Empleado;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class EmpleadoController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtIdCargo;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<Empleado> tableEmpleado;
    @FXML private TableColumn<Empleado, String> colIdEmpleado;
    @FXML private TableColumn<Empleado, String> colDocumento;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellido;
    @FXML private TableColumn<Empleado, String> colCargo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Empleado> data = FXCollections.observableArrayList();

    private Empleado empleadoSeleccionado = null;

    @FXML
    private void initialize() {
        colIdEmpleado.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdEmpleado())));
        colDocumento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumento()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colCargo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdCargo())));

        cargarTabla();

        tableEmpleado.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        empleadoSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Empleados");
            for (Map<String, Object> fila : resultados) {
                // Usa los nombres exactos de tu BD:
                int idEmpleado = fila.get("ID_Empleado") != null ? ((Number) fila.get("ID_Empleado")).intValue() : 0;
                String documento = String.valueOf(fila.getOrDefault("DocumentoIdentidad", ""));
                String nombre = String.valueOf(fila.getOrDefault("Nombre", ""));
                String apellido = String.valueOf(fila.getOrDefault("Apellido", ""));
                int idCargo = fila.get("idCargo") != null ? ((Number) fila.get("idCargo")).intValue() : 0;

                Empleado e = new Empleado(
                        idEmpleado,
                        documento,
                        nombre,
                        apellido,
                        idCargo
                );
                data.add(e);
            }
            tableEmpleado.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los empleados:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarEmpleado() {
        String buscar = txtBuscar.getText().trim();
        if (buscar.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID o nombre del empleado a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados;
            try {
                int id = Integer.parseInt(buscar);
                resultados = repository.buscar("Empleados", "ID_Empleado = ?", Collections.singletonList(id));
            } catch (NumberFormatException e) {
                resultados = repository.buscar("Empleados", "Nombre LIKE ?", Collections.singletonList("%" + buscar + "%"));
            }
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ningún empleado.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            int idEmpleado = fila.get("ID_Empleado") != null ? ((Number) fila.get("ID_Empleado")).intValue() : 0;
            String documento = String.valueOf(fila.getOrDefault("DocumentoIdentidad", ""));
            String nombre = String.valueOf(fila.getOrDefault("Nombre", ""));
            String apellido = String.valueOf(fila.getOrDefault("Apellido", ""));
            int idCargo = fila.get("idCargo") != null ? ((Number) fila.get("idCargo")).intValue() : 0;

            Empleado emp = new Empleado(
                    idEmpleado,
                    documento,
                    nombre,
                    apellido,
                    idCargo
            );
            empleadoSeleccionado = emp;
            tableEmpleado.getSelectionModel().select(buscarEmpleadoEnTabla(emp.getIdEmpleado()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableEmpleado.getSelectionModel().clearSelection();
        empleadoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtDocumento.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtIdCargo.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (empleadoSeleccionado != null) {
            txtDocumento.setText(empleadoSeleccionado.getDocumento());
            txtNombre.setText(empleadoSeleccionado.getNombre());
            txtApellido.setText(empleadoSeleccionado.getApellido());
            txtIdCargo.setText(String.valueOf(empleadoSeleccionado.getIdCargo()));
        }
    }

    @FXML
    private void handleAddEmpleado() {
        try {
            String documento = txtDocumento.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            int idCargo = Integer.parseInt(txtIdCargo.getText().trim());

            Map<String, Object> datos = new HashMap<>();
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("idCargo", idCargo);

            int filas = repository.insertar("Empleados", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "ID Cargo debe ser un número válido.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un empleado para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String documento = txtDocumento.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            int idCargo = Integer.parseInt(txtIdCargo.getText().trim());

            Map<String, Object> datos = new HashMap<>();
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("idCargo", idCargo);

            int filas = repository.actualizar(
                    "Empleados",
                    datos,
                    "ID_Empleado = ?",
                    Collections.singletonList(empleadoSeleccionado.getIdEmpleado())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "ID Cargo debe ser un número válido.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un empleado para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Empleados",
                    "ID_Empleado = ?",
                    Collections.singletonList(empleadoSeleccionado.getIdEmpleado())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Empleado buscarEmpleadoEnTabla(int idEmpleado) {
        for (Empleado e : data) {
            if (e.getIdEmpleado() == idEmpleado) return e;
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