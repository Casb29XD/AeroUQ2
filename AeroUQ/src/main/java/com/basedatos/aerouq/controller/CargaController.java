package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.ControlDeCargaYLogistica;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class CargaController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtIdAerolinea;
    @FXML private TextField txtPeso;
    @FXML private ComboBox<String> comboTipoCarga;
    @FXML private ComboBox<String> comboEstado;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private TableView<ControlDeCargaYLogistica> tableCargas;
    @FXML private TableColumn<ControlDeCargaYLogistica, String> colIdCarga;
    @FXML private TableColumn<ControlDeCargaYLogistica, String> colNIdAerolinea;
    @FXML private TableColumn<ControlDeCargaYLogistica, String> colPeso;
    @FXML private TableColumn<ControlDeCargaYLogistica, String> colTipoCarga;
    @FXML private TableColumn<ControlDeCargaYLogistica, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<ControlDeCargaYLogistica> data = FXCollections.observableArrayList();
    private ControlDeCargaYLogistica cargaSeleccionada = null;

    @FXML
    private void initialize() {
        // Inicializa los ComboBox
        comboTipoCarga.setItems(FXCollections.observableArrayList(
                "Mercancia Peligrosa", "Animales", "Cerveza y Buñuelo"
        ));
        comboEstado.setItems(FXCollections.observableArrayList(
                "En espera", "En tránsito", "Entregado"
        ));

        // Configura las columnas de la tabla
        colIdCarga.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdCarga())));
        colNIdAerolinea.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAerolinea())));
        colPeso.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPeso())));
        colTipoCarga.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoDeCarga()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        // Evento para cargar datos al seleccionar una fila
        tableCargas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        cargaSeleccionada = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("ControlDeCargaYLogistica");
            for (Map<String, Object> fila : resultados) {
                ControlDeCargaYLogistica carga = new ControlDeCargaYLogistica(
                        ((Number) fila.get("ID_Carga")).intValue(),
                        ((Number) fila.get("ID_Aerolinea")).intValue(),
                        fila.get("Peso") instanceof Number
                                ? ((Number) fila.get("Peso")).doubleValue()
                                : Double.parseDouble(fila.get("Peso").toString()),
                        (String) fila.get("TipoDeCarga"),
                        (String) fila.get("Estado")
                );
                data.add(carga);
            }
            tableCargas.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los datos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarCarga() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID de carga o tipo de carga.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados;
            // Buscar por ID_Carga numérico o por TipoDeCarga
            if (texto.matches("\\d+")) {
                resultados = repository.buscar(
                        "ControlDeCargaYLogistica",
                        "ID_Carga = ?",
                        Collections.singletonList(Integer.valueOf(texto))
                );
            } else {
                resultados = repository.buscar(
                        "ControlDeCargaYLogistica",
                        "TipoDeCarga LIKE ?",
                        Collections.singletonList("%" + texto + "%")
                );
            }
            data.clear();
            for (Map<String, Object> fila : resultados) {
                ControlDeCargaYLogistica carga = new ControlDeCargaYLogistica(
                        ((Number) fila.get("ID_Carga")).intValue(),
                        ((Number) fila.get("ID_Aerolinea")).intValue(),
                        fila.get("Peso") instanceof Number
                                ? ((Number) fila.get("Peso")).doubleValue()
                                : Double.parseDouble(fila.get("Peso").toString()),
                        (String) fila.get("TipoDeCarga"),
                        (String) fila.get("Estado")
                );
                data.add(carga);
            }
            tableCargas.setItems(data);

            if (!data.isEmpty()) {
                // Selecciona la primera coincidencia y llena los campos
                cargaSeleccionada = data.get(0);
                tableCargas.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron datos con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar carga:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableCargas.getSelectionModel().clearSelection();
        cargarTabla();
        cargaSeleccionada = null;
    }

    private void limpiarCampos() {
        txtIdAerolinea.clear();
        txtPeso.clear();
        comboTipoCarga.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (cargaSeleccionada != null) {
            txtIdAerolinea.setText(String.valueOf(cargaSeleccionada.getIdAerolinea()));
            txtPeso.setText(String.valueOf(cargaSeleccionada.getPeso()));
            comboTipoCarga.setValue(cargaSeleccionada.getTipoDeCarga());
            comboEstado.setValue(cargaSeleccionada.getEstado());
        }
    }

    @FXML
    private void handleAddCarga() {
        String idAerolineaStr = txtIdAerolinea.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String tipoCarga = comboTipoCarga.getValue();
        String estado = comboEstado.getValue();

        if (idAerolineaStr.isEmpty() || pesoStr.isEmpty() || tipoCarga == null || estado == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idAerolinea = Integer.parseInt(idAerolineaStr);
            double peso = Double.parseDouble(pesoStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Peso", peso);
            datos.put("TipoDeCarga", tipoCarga);
            datos.put("Estado", estado);

            int filas = repository.insertar("ControlDeCargaYLogistica", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Carga agregada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargaSeleccionada = null;
            }
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Agregar", "ID Aerolínea y Peso deben ser numéricos.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar carga:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditCarga() {
        if (cargaSeleccionada == null) {
            mostrarAlerta("Editar", "Seleccione una carga para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String idAerolineaStr = txtIdAerolinea.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String tipoCarga = comboTipoCarga.getValue();
        String estado = comboEstado.getValue();

        if (idAerolineaStr.isEmpty() || pesoStr.isEmpty() || tipoCarga == null || estado == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idAerolinea = Integer.parseInt(idAerolineaStr);
            double peso = Double.parseDouble(pesoStr);

            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Peso", peso);
            datos.put("TipoDeCarga", tipoCarga);
            datos.put("Estado", estado);

            int filas = repository.actualizar(
                    "ControlDeCargaYLogistica",
                    datos,
                    "ID_Carga = ?",
                    Collections.singletonList(cargaSeleccionada.getIdCarga())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Carga actualizada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargaSeleccionada = null;
            }
        } catch (NumberFormatException nfe) {
            mostrarAlerta("Editar", "ID Aerolínea y Peso deben ser numéricos.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar carga:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteCarga() {
        if (cargaSeleccionada == null) {
            mostrarAlerta("Eliminar", "Seleccione una carga para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "ControlDeCargaYLogistica",
                    "ID_Carga = ?",
                    Collections.singletonList(cargaSeleccionada.getIdCarga())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Carga eliminada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                cargaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar carga:\n" + e.getMessage(), Alert.AlertType.ERROR);
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