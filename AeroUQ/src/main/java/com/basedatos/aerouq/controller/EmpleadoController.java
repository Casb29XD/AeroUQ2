package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Empleado;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;

public class EmpleadoController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private ComboBox<String> comboCargo;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<Empleado> tableEmpleado;
    @FXML private TableColumn<Empleado, String> colIdEmpleado;
    @FXML private TableColumn<Empleado, String> colDocumento;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellido;
    @FXML private TableColumn<Empleado, String> colCargo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Empleado> data = FXCollections.observableArrayList();

    private Empleado empleadoSeleccionado = null;
    private Map<String, Integer> nombreCargoToId = new HashMap<>(); // <nombreCargo, idCargo>
    private Map<Integer, String> idToNombreCargo = new HashMap<>(); // <idCargo, nombreCargo>

    @FXML
    private void initialize() {
        colIdEmpleado.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdEmpleado())));
        colDocumento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumento()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colCargo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCargo()));

        cargarCargosCombo();
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

    private void cargarCargosCombo() {
        comboCargo.getItems().clear();
        nombreCargoToId.clear();
        idToNombreCargo.clear();
        try {
            List<Map<String, Object>> cargos = repository.buscar("Cargos");
            for (Map<String, Object> fila : cargos) {
                int idCargo = ((Number) fila.get("idCargo")).intValue();
                String nombreCargo = (String) fila.get("nombreCargo");
                nombreCargoToId.put(nombreCargo, idCargo);
                idToNombreCargo.put(idCargo, nombreCargo);
                comboCargo.getItems().add(nombreCargo);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los cargos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Empleado emp = new Empleado(
                        rs.getInt("ID_Empleado"),
                        rs.getString("DocumentoIdentidad"),
                        rs.getString("Nombre"),
                        rs.getString("Apellido"),
                        rs.getInt("idCargo"),
                        rs.getString("nombreCargo")
                );
                data.add(emp);
            }
            tableEmpleado.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los empleados:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarEmpleado() {
        String buscar = txtBuscar.getText().trim();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo " +
                "WHERE e.ID_Empleado = ? OR e.Nombre LIKE ? OR e.Apellido LIKE ? OR c.nombreCargo LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try {
                int id = Integer.parseInt(buscar);
                stmt.setInt(1, id);
            } catch (NumberFormatException e) {
                stmt.setInt(1, -1);
            }
            String like = "%" + buscar + "%";
            stmt.setString(2, like);
            stmt.setString(3, like);
            stmt.setString(4, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Empleado emp = new Empleado(
                            rs.getInt("ID_Empleado"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getInt("idCargo"),
                            rs.getString("nombreCargo")
                    );
                    data.add(emp);
                }
            }
            tableEmpleado.setItems(data);
            if (!data.isEmpty()) {
                empleadoSeleccionado = data.get(0);
                tableEmpleado.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontró ningún empleado.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableEmpleado.getSelectionModel().clearSelection();
        cargarTabla();
        empleadoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtDocumento.clear();
        txtNombre.clear();
        txtApellido.clear();
        comboCargo.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (empleadoSeleccionado != null) {
            txtDocumento.setText(empleadoSeleccionado.getDocumento());
            txtNombre.setText(empleadoSeleccionado.getNombre());
            txtApellido.setText(empleadoSeleccionado.getApellido());
            // Selecciona el nombre del cargo correspondiente en el combo
            comboCargo.setValue(empleadoSeleccionado.getNombreCargo());
        }
    }

    @FXML
    private void handleAddEmpleado() {
        try {
            String documento = txtDocumento.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String nombreCargo = comboCargo.getValue();
            Integer idCargo = nombreCargoToId.get(nombreCargo);

            if (documento.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || nombreCargo == null || idCargo == null) {
                mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

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
            String nombreCargo = comboCargo.getValue();
            Integer idCargo = nombreCargoToId.get(nombreCargo);

            if (documento.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || nombreCargo == null || idCargo == null) {
                mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}