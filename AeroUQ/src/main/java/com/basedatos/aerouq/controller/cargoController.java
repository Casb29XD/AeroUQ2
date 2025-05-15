package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.basedatos.aerouq.model.Cargo;

public class cargoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtBuscar;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<Cargo> tableCargas;
    @FXML private TableColumn<Cargo, Integer> colIDCargo;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;
    @FXML private TableColumn<Cargo, String> colFechaCreacion;

    private ObservableList<Cargo> cargaList;
    DatabaseRepository repo = new DatabaseRepository();
    private int idCounter = 1;

    @FXML
    public void initialize() {
        // Inicializar lista
        cargaList = FXCollections.observableArrayList();

        // Configurar columnas
        colIDCargo.setCellValueFactory(new PropertyValueFactory<>("idCargo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colFechaCreacion.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        tableCargas.setItems(cargaList);

        // Detectar selección en la tabla
        tableCargas.setOnMouseClicked(this::handleTableClick);
    }

    @FXML
    private void handleAddCarga() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            showAlert("Por favor, complete todos los campos.");
            return;
        }
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("descripcion", descripcion);
            datos.put("fechaCreacion", new Timestamp(System.currentTimeMillis())); // Fecha actual

            repo.insertar("Cargo", datos);

            showAlert("Carga agregada correctamente.");
            limpiarCampos();
            cargarDatosDesdeBD();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error al agregar la carga.");
        }
    }


    @FXML
    private void handleEditCarga() {
        Cargo cargaSeleccionada = tableCargas.getSelectionModel().getSelectedItem();

        if (cargaSeleccionada == null) {
            showAlert("Debe seleccionar una carga para editar.");
            return;
        }

        cargaSeleccionada.setNombre(txtNombre.getText());
        cargaSeleccionada.setDescripcion(txtDescripcion.getText());
        tableCargas.refresh();
        limpiarCampos();
    }

    @FXML
    private void handleDeleteCarga() {
        Cargo cargaSeleccionada = tableCargas.getSelectionModel().getSelectedItem();
        if (cargaSeleccionada == null) {
            showAlert("Por favor, seleccione una carga para eliminar.");
            return;
        }
        try {
            String condicion = "idCargo = ?";
            List<Object> parametros = List.of(cargaSeleccionada.getIdCargo());

            int filasEliminadas = repo.eliminar("Cargo", condicion, parametros);

            if (filasEliminadas > 0) {
                showAlert("Carga eliminada correctamente.");
                limpiarCampos();
                cargarDatosDesdeBD();
            } else {
                showAlert("No se pudo eliminar la carga.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error al eliminar la carga.");
        }
    }


    @FXML
    private void handleBuscarCarga() {
        String textoBuscar = txtBuscar.getText();

        if (textoBuscar.isEmpty()) {
            showAlert("Ingrese un ID para buscar.");
            return;
        }

        try {
            int idBuscar = Integer.parseInt(textoBuscar);
            for (Cargo carga : cargaList) {
                if (carga.getIdCargo() == idBuscar) {
                    tableCargas.getSelectionModel().select(carga);
                    txtNombre.setText(carga.getNombre());
                    txtDescripcion.setText(carga.getDescripcion());
                    return;
                }
            }
            showAlert("Carga con ID " + idBuscar + " no encontrada.");
        } catch (NumberFormatException e) {
            showAlert("El ID debe ser un número entero.");
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        tableCargas.getSelectionModel().clearSelection();
    }

    private void handleTableClick(MouseEvent event) {
        Cargo cargaSeleccionada = tableCargas.getSelectionModel().getSelectedItem();

        if (cargaSeleccionada != null) {
            txtNombre.setText(cargaSeleccionada.getNombre());
            txtDescripcion.setText(cargaSeleccionada.getDescripcion());
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarDatosDesdeBD() {
        try {
            List<Map<String, Object>> cargosBD = repo.buscar("Cargo");

            cargaList.clear(); // Limpiar lista antes de cargar nueva

            for (Map<String, Object> c : cargosBD) {
                Cargo cargo = new Cargo(
                        (int) c.get("idCargo"),
                        (String) c.get("nombre"),
                        (String) c.get("descripcion"),
                        c.get("fechaCreacion").toString()
                );

                cargaList.add(cargo);
            }

            tableCargas.setItems(cargaList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al cargar datos desde la base de datos.");
        }
    }

}
