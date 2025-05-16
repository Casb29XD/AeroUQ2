package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Equipaje;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class EquipajeController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPeso;
    @FXML private TextField txtIdVuelo;
    @FXML private TextField txtIdPasajero;

    @FXML private ComboBox<String> comboEstado;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<Equipaje> tableEquipaje;
    @FXML private TableColumn<Equipaje, String> colIdMaleta;
    @FXML private TableColumn<Equipaje, String> colCodBarras;
    @FXML private TableColumn<Equipaje, String> colPeso;
    @FXML private TableColumn<Equipaje, String> colIdVuelo;
    @FXML private TableColumn<Equipaje, String> colIdPasajero;
    @FXML private TableColumn<Equipaje, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Equipaje> data = FXCollections.observableArrayList();
    private Equipaje equipajeSeleccionado = null;

    private static final List<String> ESTADOS = Arrays.asList("En tránsito", "Cargada", "Extraviada");

    @FXML
    private void initialize() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS));

        colIdMaleta.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdMaleta())));
        colCodBarras.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigoDeBarras()));
        colPeso.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPeso())));
        colIdVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdVuelo())));
        colIdPasajero.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPasajero())));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        tableEquipaje.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        equipajeSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Equipajes");
            for (Map<String, Object> fila : resultados) {
                Equipaje equipaje = new Equipaje(
                        ((Number) fila.get("ID_Maleta")).intValue(),
                        (String) fila.get("CodigoDeBarras"),
                        fila.get("Peso") instanceof Number
                                ? ((Number) fila.get("Peso")).doubleValue()
                                : Double.parseDouble(fila.get("Peso").toString()),
                        ((Number) fila.get("ID_Vuelo")).intValue(),
                        (String) fila.get("Estado"),
                        ((Number) fila.get("ID_Pasajero")).intValue()
                );
                data.add(equipaje);
            }
            tableEquipaje.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los equipajes:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarEquipaje() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID de la maleta o el código de barras.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados;
            if (texto.matches("\\d+")) {
                resultados = repository.buscar(
                        "Equipajes",
                        "ID_Maleta = ?",
                        Collections.singletonList(Integer.valueOf(texto))
                );
            } else {
                resultados = repository.buscar(
                        "Equipajes",
                        "CodigoDeBarras LIKE ?",
                        Collections.singletonList("%" + texto + "%")
                );
            }
            data.clear();
            for (Map<String, Object> fila : resultados) {
                Equipaje equipaje = new Equipaje(
                        ((Number) fila.get("ID_Maleta")).intValue(),
                        (String) fila.get("CodigoDeBarras"),
                        fila.get("Peso") instanceof Number
                                ? ((Number) fila.get("Peso")).doubleValue()
                                : Double.parseDouble(fila.get("Peso").toString()),
                        ((Number) fila.get("ID_Vuelo")).intValue(),
                        (String) fila.get("Estado"),
                        ((Number) fila.get("ID_Pasajero")).intValue()
                );
                data.add(equipaje);
            }
            tableEquipaje.setItems(data);

            if (!data.isEmpty()) {
                equipajeSeleccionado = data.get(0);
                tableEquipaje.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron equipajes con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar equipaje:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableEquipaje.getSelectionModel().clearSelection();
        cargarTabla();
        equipajeSeleccionado = null;
    }

    private void limpiarCampos() {
        txtCodBarras.clear();
        txtPeso.clear();
        txtIdVuelo.clear();
        txtIdPasajero.clear();
        comboEstado.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (equipajeSeleccionado != null) {
            txtCodBarras.setText(equipajeSeleccionado.getCodigoDeBarras());
            txtPeso.setText(String.valueOf(equipajeSeleccionado.getPeso()));
            txtIdVuelo.setText(String.valueOf(equipajeSeleccionado.getIdVuelo()));
            txtIdPasajero.setText(String.valueOf(equipajeSeleccionado.getIdPasajero()));
            comboEstado.setValue(equipajeSeleccionado.getEstado());
        }
    }

    @FXML
    private void handleAddEquipaje() {
        String codBarras = txtCodBarras.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String idVueloStr = txtIdVuelo.getText().trim();
        String idPasajeroStr = txtIdPasajero.getText().trim();
        String estado = comboEstado.getValue();

        if (codBarras.isEmpty() || pesoStr.isEmpty() || idVueloStr.isEmpty() || idPasajeroStr.isEmpty() || estado == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);
            int idVuelo = Integer.parseInt(idVueloStr);
            int idPasajero = Integer.parseInt(idPasajeroStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("CodigoDeBarras", codBarras);
            datos.put("Peso", peso);
            datos.put("ID_Vuelo", idVuelo);
            datos.put("Estado", estado);
            datos.put("ID_Pasajero", idPasajero);

            int filas = repository.insertar("Equipajes", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Equipaje agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                equipajeSeleccionado = null;
            }
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Agregar", "Peso, ID Vuelo y ID Pasajero deben ser numéricos.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                mostrarAlerta("Error", "El código de barras ya existe.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al agregar equipaje:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleEditEquipaje() {
        if (equipajeSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un equipaje para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String codBarras = txtCodBarras.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String idVueloStr = txtIdVuelo.getText().trim();
        String idPasajeroStr = txtIdPasajero.getText().trim();
        String estado = comboEstado.getValue();

        if (codBarras.isEmpty() || pesoStr.isEmpty() || idVueloStr.isEmpty() || idPasajeroStr.isEmpty() || estado == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);
            int idVuelo = Integer.parseInt(idVueloStr);
            int idPasajero = Integer.parseInt(idPasajeroStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("CodigoDeBarras", codBarras);
            datos.put("Peso", peso);
            datos.put("ID_Vuelo", idVuelo);
            datos.put("Estado", estado);
            datos.put("ID_Pasajero", idPasajero);

            int filas = repository.actualizar(
                    "Equipajes",
                    datos,
                    "ID_Maleta = ?",
                    Collections.singletonList(equipajeSeleccionado.getIdMaleta())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Equipaje actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                equipajeSeleccionado = null;
            }
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Editar", "Peso, ID Vuelo y ID Pasajero deben ser numéricos.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                mostrarAlerta("Error", "El código de barras ya existe.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al actualizar equipaje:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDeleteEquipaje() {
        if (equipajeSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un equipaje para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Equipajes",
                    "ID_Maleta = ?",
                    Collections.singletonList(equipajeSeleccionado.getIdMaleta())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Equipaje eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                equipajeSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar equipaje:\n" + e.getMessage(), Alert.AlertType.ERROR);
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