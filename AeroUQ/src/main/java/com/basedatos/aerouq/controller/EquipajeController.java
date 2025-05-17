package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Equipaje;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;

public class EquipajeController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPeso;
    @FXML private ComboBox<String> comboVuelo;
    @FXML private ComboBox<String> comboPasajero;
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
    @FXML private TableColumn<Equipaje, String> colVuelo;
    @FXML private TableColumn<Equipaje, String> colPasajero;
    @FXML private TableColumn<Equipaje, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Equipaje> data = FXCollections.observableArrayList();
    private Equipaje equipajeSeleccionado = null;

    private Map<String, Integer> vueloToId = new HashMap<>();
    private Map<Integer, String> idToVuelo = new HashMap<>();
    private Map<String, Integer> pasajeroToId = new HashMap<>();
    private Map<Integer, String> idToPasajero = new HashMap<>();

    private static final List<String> ESTADOS = Arrays.asList("En tránsito", "Cargada", "Extraviada");

    @FXML
    private void initialize() {
        comboEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cargarVuelosCombo();
        cargarPasajerosCombo();

        colIdMaleta.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdMaleta())));
        colCodBarras.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigoDeBarras()));
        colPeso.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPeso())));
        colVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(
                idToVuelo.getOrDefault(cellData.getValue().getIdVuelo(), String.valueOf(cellData.getValue().getIdVuelo()))
        ));
        colPasajero.setCellValueFactory(cellData -> new SimpleStringProperty(
                idToPasajero.getOrDefault(cellData.getValue().getIdPasajero(), String.valueOf(cellData.getValue().getIdPasajero()))
        ));
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

    private void cargarVuelosCombo() {
        comboVuelo.getItems().clear();
        vueloToId.clear();
        idToVuelo.clear();
        try {
            List<Map<String, Object>> vuelos = repository.buscar("Vuelos");
            for (Map<String, Object> fila : vuelos) {
                int idVuelo = ((Number) fila.get("ID_Vuelo")).intValue();
                String numeroVuelo = (String) fila.get("NumeroVuelo");
                vueloToId.put(numeroVuelo, idVuelo);
                idToVuelo.put(idVuelo, numeroVuelo);
                comboVuelo.getItems().add(numeroVuelo);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarPasajerosCombo() {
        comboPasajero.getItems().clear();
        pasajeroToId.clear();
        idToPasajero.clear();
        try {
            List<Map<String, Object>> pasajeros = repository.buscar("Pasajeros");
            for (Map<String, Object> fila : pasajeros) {
                int idPasajero = ((Number) fila.get("ID_Pasajero")).intValue();
                String nombreCompleto = fila.get("Nombre") + " " + fila.get("Apellido");
                pasajeroToId.put(nombreCompleto, idPasajero);
                idToPasajero.put(idPasajero, nombreCompleto);
                comboPasajero.getItems().add(nombreCompleto);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los pasajeros:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT e.ID_Maleta, e.CodigoDeBarras, e.Peso, e.ID_Vuelo, e.Estado, e.ID_Pasajero " +
                "FROM Equipajes e";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Equipaje equipaje = new Equipaje(
                        rs.getInt("ID_Maleta"),
                        rs.getString("CodigoDeBarras"),
                        rs.getDouble("Peso"),
                        rs.getInt("ID_Vuelo"),
                        rs.getString("Estado"),
                        rs.getInt("ID_Pasajero")
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
        String sql = "SELECT e.ID_Maleta, e.CodigoDeBarras, e.Peso, e.ID_Vuelo, e.Estado, e.ID_Pasajero " +
                "FROM Equipajes e " +
                "LEFT JOIN Vuelos v ON e.ID_Vuelo = v.ID_Vuelo " +
                "LEFT JOIN Pasajeros p ON e.ID_Pasajero = p.ID_Pasajero " +
                "WHERE e.CodigoDeBarras LIKE ? OR v.NumeroVuelo LIKE ? OR CONCAT(p.Nombre, ' ', p.Apellido) LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String like = "%" + texto + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);
            stmt.setString(3, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipaje equipaje = new Equipaje(
                            rs.getInt("ID_Maleta"),
                            rs.getString("CodigoDeBarras"),
                            rs.getDouble("Peso"),
                            rs.getInt("ID_Vuelo"),
                            rs.getString("Estado"),
                            rs.getInt("ID_Pasajero")
                    );
                    data.add(equipaje);
                }
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
        comboVuelo.getSelectionModel().clearSelection();
        comboPasajero.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (equipajeSeleccionado != null) {
            txtCodBarras.setText(equipajeSeleccionado.getCodigoDeBarras());
            txtPeso.setText(String.valueOf(equipajeSeleccionado.getPeso()));
            comboVuelo.setValue(idToVuelo.get(equipajeSeleccionado.getIdVuelo()));
            comboPasajero.setValue(idToPasajero.get(equipajeSeleccionado.getIdPasajero()));
            comboEstado.setValue(equipajeSeleccionado.getEstado());
        }
    }

    @FXML
    private void handleAddEquipaje() {
        String codBarras = txtCodBarras.getText().trim();
        String pesoStr = txtPeso.getText().trim();
        String vueloNombre = comboVuelo.getValue();
        String pasajeroNombre = comboPasajero.getValue();
        String estado = comboEstado.getValue();

        Integer idVuelo = vueloToId.get(vueloNombre);
        Integer idPasajero = pasajeroToId.get(pasajeroNombre);

        if (codBarras.isEmpty() || pesoStr.isEmpty() || idVuelo == null || idPasajero == null || estado == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);

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
            mostrarAlerta("Agregar", "El peso debe ser numérico.", Alert.AlertType.WARNING);
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
        String vueloNombre = comboVuelo.getValue();
        String pasajeroNombre = comboPasajero.getValue();
        String estado = comboEstado.getValue();

        Integer idVuelo = vueloToId.get(vueloNombre);
        Integer idPasajero = pasajeroToId.get(pasajeroNombre);

        if (codBarras.isEmpty() || pesoStr.isEmpty() || idVuelo == null || idPasajero == null || estado == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double peso = Double.parseDouble(pesoStr);

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
            mostrarAlerta("Editar", "El peso debe ser numérico.", Alert.AlertType.WARNING);
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