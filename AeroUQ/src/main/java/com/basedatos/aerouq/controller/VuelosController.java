package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Vuelo;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VuelosController {

    @FXML private TextField txtNumeroVuelo;
    @FXML private TextField txtIdAerolinea;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtDestino;
    @FXML private TextField txtFechaHoraSalida;
    @FXML private TextField txtFechaHoraLlegada;
    @FXML private TextField txtEstadoVuelo;
    @FXML private TextField txtIdPuerta;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<Vuelo> tableVuelos;
    @FXML private TableColumn<Vuelo, String> colIdVuelo;
    @FXML private TableColumn<Vuelo, String> colNumeroVuelo;
    @FXML private TableColumn<Vuelo, String> colIdAerolinea;
    @FXML private TableColumn<Vuelo, String> colOrigen;
    @FXML private TableColumn<Vuelo, String> colDestino;
    @FXML private TableColumn<Vuelo, String> colFechaHoraSalida;
    @FXML private TableColumn<Vuelo, String> colFechaHoraLlegada;
    @FXML private TableColumn<Vuelo, String> colEstadoVuelo;
    @FXML private TableColumn<Vuelo, String> colIdPuerta;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Vuelo> data = FXCollections.observableArrayList();

    private Vuelo vueloSeleccionado = null;

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        colIdVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdVuelo())));
        colNumeroVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroVuelo()));
        colIdAerolinea.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAerolinea())));
        colOrigen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen()));
        colDestino.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestino()));
        colFechaHoraSalida.setCellValueFactory(cellData -> new SimpleStringProperty(dateTimeFormat.format(cellData.getValue().getFechaHoraSalida())));
        colFechaHoraLlegada.setCellValueFactory(cellData -> new SimpleStringProperty(dateTimeFormat.format(cellData.getValue().getFechaHoraLlegada())));
        colEstadoVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstadoVuelo()));
        colIdPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPuerta())));

        cargarTabla();

        tableVuelos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        vueloSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Vuelos");
            for (Map<String, Object> fila : resultados) {
                Vuelo vuelo = new Vuelo(
                        (int) fila.get("ID_Vuelo"),
                        (String) fila.get("NumeroVuelo"),
                        (int) fila.get("ID_Aerolinea"),
                        (String) fila.get("Origen"),
                        (String) fila.get("Destino"),
                        fila.get("FechaHoraSalida") != null ? (Date) fila.get("FechaHoraSalida") : null,
                        fila.get("FechaHoraLlegada") != null ? (Date) fila.get("FechaHoraLlegada") : null,
                        (String) fila.get("EstadoVuelo"),
                        fila.get("ID_Puerta") != null ? (int) fila.get("ID_Puerta") : 0
                );
                data.add(vuelo);
            }
            tableVuelos.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarVuelo() {
        String buscarId = txtBuscar.getText().trim();
        if (buscarId.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID del vuelo a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados = repository.buscar("Vuelos", "ID_Vuelo = ?", Collections.singletonList(Integer.valueOf(buscarId)));
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ningún vuelo con ese ID.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            Vuelo vuelo = new Vuelo(
                    (int) fila.get("ID_Vuelo"),
                    (String) fila.get("NumeroVuelo"),
                    (int) fila.get("ID_Aerolinea"),
                    (String) fila.get("Origen"),
                    (String) fila.get("Destino"),
                    fila.get("FechaHoraSalida") != null ? (Date) fila.get("FechaHoraSalida") : null,
                    fila.get("FechaHoraLlegada") != null ? (Date) fila.get("FechaHoraLlegada") : null,
                    (String) fila.get("EstadoVuelo"),
                    fila.get("ID_Puerta") != null ? (int) fila.get("ID_Puerta") : 0
            );
            vueloSeleccionado = vuelo;
            tableVuelos.getSelectionModel().select(buscarVueloEnTabla(vuelo.getIdVuelo()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableVuelos.getSelectionModel().clearSelection();
        vueloSeleccionado = null;
    }

    private void limpiarCampos() {
        txtNumeroVuelo.clear();
        txtIdAerolinea.clear();
        txtOrigen.clear();
        txtDestino.clear();
        txtFechaHoraSalida.clear();
        txtFechaHoraLlegada.clear();
        txtEstadoVuelo.clear();
        txtIdPuerta.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (vueloSeleccionado != null) {
            txtNumeroVuelo.setText(vueloSeleccionado.getNumeroVuelo());
            txtIdAerolinea.setText(String.valueOf(vueloSeleccionado.getIdAerolinea()));
            txtOrigen.setText(vueloSeleccionado.getOrigen());
            txtDestino.setText(vueloSeleccionado.getDestino());
            txtFechaHoraSalida.setText(dateTimeFormat.format(vueloSeleccionado.getFechaHoraSalida()));
            txtFechaHoraLlegada.setText(dateTimeFormat.format(vueloSeleccionado.getFechaHoraLlegada()));
            txtEstadoVuelo.setText(vueloSeleccionado.getEstadoVuelo());
            txtIdPuerta.setText(String.valueOf(vueloSeleccionado.getIdPuerta()));
        }
    }

    @FXML
    private void handleAddVuelo() {
        try {
            String numeroVuelo = txtNumeroVuelo.getText().trim();
            int idAerolinea = Integer.parseInt(txtIdAerolinea.getText().trim());
            String origen = txtOrigen.getText().trim();
            String destino = txtDestino.getText().trim();
            Date fechaHoraSalida = dateTimeFormat.parse(txtFechaHoraSalida.getText().trim());
            Date fechaHoraLlegada = dateTimeFormat.parse(txtFechaHoraLlegada.getText().trim());
            String estadoVuelo = txtEstadoVuelo.getText().trim();
            int idPuerta = Integer.parseInt(txtIdPuerta.getText().trim());

            Map<String, Object> datos = new HashMap<>();
            datos.put("NumeroVuelo", numeroVuelo);
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Origen", origen);
            datos.put("Destino", destino);
            datos.put("FechaHoraSalida", new java.sql.Timestamp(fechaHoraSalida.getTime()));
            datos.put("FechaHoraLlegada", new java.sql.Timestamp(fechaHoraLlegada.getTime()));
            datos.put("EstadoVuelo", estadoVuelo);
            datos.put("ID_Puerta", idPuerta);

            int filas = repository.insertar("Vuelos", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Vuelo agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                vueloSeleccionado = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "Los campos numéricos deben tener valores válidos.", Alert.AlertType.WARNING);
        } catch (ParseException e) {
            mostrarAlerta("Advertencia", "El formato de fecha y hora debe ser yyyy-MM-dd HH:mm:ss.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditVuelo() {
        if (vueloSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un vuelo para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String numeroVuelo = txtNumeroVuelo.getText().trim();
            int idAerolinea = Integer.parseInt(txtIdAerolinea.getText().trim());
            String origen = txtOrigen.getText().trim();
            String destino = txtDestino.getText().trim();
            Date fechaHoraSalida = dateTimeFormat.parse(txtFechaHoraSalida.getText().trim());
            Date fechaHoraLlegada = dateTimeFormat.parse(txtFechaHoraLlegada.getText().trim());
            String estadoVuelo = txtEstadoVuelo.getText().trim();
            int idPuerta = Integer.parseInt(txtIdPuerta.getText().trim());

            Map<String, Object> datos = new HashMap<>();
            datos.put("NumeroVuelo", numeroVuelo);
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Origen", origen);
            datos.put("Destino", destino);
            datos.put("FechaHoraSalida", new java.sql.Timestamp(fechaHoraSalida.getTime()));
            datos.put("FechaHoraLlegada", new java.sql.Timestamp(fechaHoraLlegada.getTime()));
            datos.put("EstadoVuelo", estadoVuelo);
            datos.put("ID_Puerta", idPuerta);

            int filas = repository.actualizar(
                    "Vuelos",
                    datos,
                    "ID_Vuelo = ?",
                    Collections.singletonList(vueloSeleccionado.getIdVuelo())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Vuelo actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                vueloSeleccionado = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "Los campos numéricos deben tener valores válidos.", Alert.AlertType.WARNING);
        } catch (ParseException e) {
            mostrarAlerta("Advertencia", "El formato de fecha y hora debe ser yyyy-MM-dd HH:mm:ss.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteVuelo() {
        if (vueloSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un vuelo para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Vuelos",
                    "ID_Vuelo = ?",
                    Collections.singletonList(vueloSeleccionado.getIdVuelo())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Vuelo eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                vueloSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Vuelo buscarVueloEnTabla(int idVuelo) {
        for (Vuelo v : data) {
            if (v.getIdVuelo() == idVuelo) return v;
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