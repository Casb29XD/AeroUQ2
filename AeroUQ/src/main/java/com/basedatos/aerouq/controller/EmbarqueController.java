package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.PuertaDeEmbarque;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class EmbarqueController {

    @FXML private TextField txtNumeroPuerta;
    @FXML private TextField txtEstado;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private TableView<PuertaDeEmbarque> tablePuertas;
    @FXML private TableColumn<PuertaDeEmbarque, String> colIdPuerta;
    @FXML private TableColumn<PuertaDeEmbarque, String> colNumeroPuerta;
    @FXML private TableColumn<PuertaDeEmbarque, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<PuertaDeEmbarque> data = FXCollections.observableArrayList();

    private PuertaDeEmbarque puertaSeleccionada = null;

    @FXML
    private void initialize() {
        colIdPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPuerta())));
        colNumeroPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroPuerta()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        tablePuertas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        puertaSeleccionada = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("PuertasDeEmbarque");
            for (Map<String, Object> fila : resultados) {
                PuertaDeEmbarque puerta = new PuertaDeEmbarque(
                        (int) fila.get("ID_Puerta"),
                        (String) fila.get("NumeroPuerta"),
                        (String) fila.get("Estado")
                );
                data.add(puerta);
            }
            tablePuertas.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las puertas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarPuerta() {
        String buscarId = txtBuscar.getText().trim();
        if (buscarId.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID de la puerta a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados = repository.buscar("PuertasDeEmbarque", "ID_Puerta = ?", Collections.singletonList(Integer.valueOf(buscarId)));
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ninguna puerta con ese ID.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            PuertaDeEmbarque puerta = new PuertaDeEmbarque(
                    (int) fila.get("ID_Puerta"),
                    (String) fila.get("NumeroPuerta"),
                    (String) fila.get("Estado")
            );
            puertaSeleccionada = puerta;
            tablePuertas.getSelectionModel().select(buscarPuertaEnTabla(puerta.getIdPuerta()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tablePuertas.getSelectionModel().clearSelection();
        puertaSeleccionada = null;
    }

    private void limpiarCampos() {
        txtNumeroPuerta.clear();
        txtEstado.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (puertaSeleccionada != null) {
            txtNumeroPuerta.setText(puertaSeleccionada.getNumeroPuerta());
            txtEstado.setText(puertaSeleccionada.getEstado());
        }
    }

    @FXML
    private void handleAddPuerta() {
        String numeroPuerta = txtNumeroPuerta.getText().trim();
        String estado = txtEstado.getText().trim();

        if (numeroPuerta.isEmpty() || estado.isEmpty()) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("NumeroPuerta", numeroPuerta);
        datos.put("Estado", estado);

        try {
            int filas = repository.insertar("PuertasDeEmbarque", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Puerta agregada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                puertaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditPuerta() {
        if (puertaSeleccionada == null) {
            mostrarAlerta("Editar", "Seleccione una puerta para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String numeroPuerta = txtNumeroPuerta.getText().trim();
        String estado = txtEstado.getText().trim();
        if (numeroPuerta.isEmpty() || estado.isEmpty()) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("NumeroPuerta", numeroPuerta);
        datos.put("Estado", estado);

        try {
            int filas = repository.actualizar(
                    "PuertasDeEmbarque",
                    datos,
                    "ID_Puerta = ?",
                    Collections.singletonList(puertaSeleccionada.getIdPuerta())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Puerta actualizada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                puertaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeletePuerta() {
        if (puertaSeleccionada == null) {
            mostrarAlerta("Eliminar", "Seleccione una puerta para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "PuertasDeEmbarque",
                    "ID_Puerta = ?",
                    Collections.singletonList(puertaSeleccionada.getIdPuerta())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Puerta eliminada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                puertaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private PuertaDeEmbarque buscarPuertaEnTabla(int idPuerta) {
        for (PuertaDeEmbarque p : data) {
            if (p.getIdPuerta() == idPuerta) return p;
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