package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Cargo;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class CargoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TableView<Cargo> tableCargos;
    @FXML private TableColumn<Cargo, Integer> colId;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    private final DatabaseRepository repository = new DatabaseRepository();
    private final String NOMBRE_TABLA = "cargo";

    private ObservableList<Cargo> listaCargos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getIdCargo()));
        colNombre.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNombre()));
        cargarCargos();

        tableCargos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtId.setText(String.valueOf(newSelection.getIdCargo()));
                txtNombre.setText(newSelection.getNombre());
            }
        });
    }

    private void cargarCargos() {
        listaCargos.clear();
        try {
            List<Map<String, Object>> registros = repository.buscar(NOMBRE_TABLA);
            for (Map<String, Object> fila : registros) {
                int id = (int) fila.get("id");
                String nombre = (String) fila.get("nombre");
                String descripcion = (String) fila.get("descripcion");
                String fecha = (String) fila.get("fecha");
                listaCargos.add(new Cargo(id, nombre, descripcion, fecha));
            }
            tableCargos.setItems(listaCargos);
        } catch (SQLException e) {
            mostrarError("Error al cargar cargos", e.getMessage());
        }
    }

    @FXML
    private void agregarCargo() {
        String nombre = txtNombre.getText();
        if (nombre.isEmpty()) {
            mostrarError("Validación", "El nombre no puede estar vacío.");
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);

        try {
            repository.insertar(NOMBRE_TABLA, datos);
            cargarCargos();
            limpiarCampos();
        } catch (SQLException e) {
            mostrarError("Error al insertar cargo", e.getMessage());
        }
    }

    @FXML
    private void actualizarCargo() {
        String idText = txtId.getText();
        String nombre = txtNombre.getText();

        if (idText.isEmpty() || nombre.isEmpty()) {
            mostrarError("Validación", "Debe seleccionar un cargo y escribir un nombre.");
            return;
        }

        int id = Integer.parseInt(idText);
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);

        try {
            repository.actualizar(NOMBRE_TABLA, datos, "id = ?", List.of(id));
            cargarCargos();
            limpiarCampos();
        } catch (SQLException e) {
            mostrarError("Error al actualizar cargo", e.getMessage());
        }
    }

    @FXML
    private void eliminarCargo() {
        String idText = txtId.getText();
        if (idText.isEmpty()) {
            mostrarError("Validación", "Debe seleccionar un cargo para eliminar.");
            return;
        }

        int id = Integer.parseInt(idText);

        try {
            repository.eliminar(NOMBRE_TABLA, "id = ?", List.of(id));
            cargarCargos();
            limpiarCampos();
        } catch (SQLException e) {
            mostrarError("Error al eliminar cargo", e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtId.clear();
        txtNombre.clear();
        tableCargos.getSelectionModel().clearSelection();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
