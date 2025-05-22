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
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> comboEstado;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private TableView<PuertaDeEmbarqueVuelo> tablePuertas;
    @FXML private TableColumn<PuertaDeEmbarqueVuelo, String> colIdPuerta;
    @FXML private TableColumn<PuertaDeEmbarqueVuelo, String> colNumeroPuerta;
    @FXML private TableColumn<PuertaDeEmbarqueVuelo, String> colEstado;
    @FXML private TableColumn<PuertaDeEmbarqueVuelo, String> colNumeroVuelo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<PuertaDeEmbarqueVuelo> data = FXCollections.observableArrayList();

    private PuertaDeEmbarqueVuelo puertaSeleccionada = null;

    // Modelo auxiliar para mostrar puerta y número de vuelo asociado (si existe)
    public static class PuertaDeEmbarqueVuelo {
        private final int idPuerta;
        private final String numeroPuerta;
        private final String estado;
        private final String numeroVuelo; // Puede ser null

        public PuertaDeEmbarqueVuelo(int idPuerta, String numeroPuerta, String estado, String numeroVuelo) {
            this.idPuerta = idPuerta;
            this.numeroPuerta = numeroPuerta;
            this.estado = estado;
            this.numeroVuelo = numeroVuelo;
        }

        public int getIdPuerta() { return idPuerta; }
        public String getNumeroPuerta() { return numeroPuerta; }
        public String getEstado() { return estado; }
        public String getNumeroVuelo() { return numeroVuelo; }
    }

    @FXML
    private void initialize() {
        // ComboBox de estados
        comboEstado.setItems(FXCollections.observableArrayList("Disponible", "Ocupada", "Mantenimiento"));

        // Columnas tabla
        colIdPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdPuerta())));
        colNumeroPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroPuerta()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));
        colNumeroVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getNumeroVuelo() != null ? cellData.getValue().getNumeroVuelo() : "—"
        ));

        cargarTablaPuertas(null); // Cargar todas al inicio

        tablePuertas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        puertaSeleccionada = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTablaPuertas(String estadoFiltro) {
        data.clear();
        try {
            String sql = """
                SELECT p.ID_Puerta, p.NumeroPuerta, p.Estado, v.NumeroVuelo
                FROM PuertasDeEmbarque p
                LEFT JOIN Vuelos v ON v.ID_Puerta = p.ID_Puerta
            """;
            List<Object> params = new ArrayList<>();
            if (estadoFiltro != null) {
                sql += " WHERE p.Estado = ?";
                params.add(estadoFiltro);
            }
            List<Map<String, Object>> resultados = repository.buscarPorConsulta(sql, params);

            for (Map<String, Object> fila : resultados) {
                PuertaDeEmbarqueVuelo puerta = new PuertaDeEmbarqueVuelo(
                        (int) fila.get("ID_Puerta"),
                        (String) fila.get("NumeroPuerta"),
                        (String) fila.get("Estado"),
                        (String) fila.get("NumeroVuelo") // Puede ser null
                );
                data.add(puerta);
            }
            tablePuertas.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las puertas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleFiltrarPorEstado() {
        String estado = comboEstado.getSelectionModel().getSelectedItem();
        if (estado != null && !estado.isEmpty()) {
            cargarTablaPuertas(estado);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        comboEstado.getSelectionModel().clearSelection();
        limpiarCampos();
        tablePuertas.getSelectionModel().clearSelection();
        puertaSeleccionada = null;
        cargarTablaPuertas(null);
    }

    @FXML
    private void handleBuscarPuerta() {
        String buscarId = txtBuscar.getText().trim();
        if (buscarId.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID de la puerta a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String sql = """
                SELECT p.ID_Puerta, p.NumeroPuerta, p.Estado, v.NumeroVuelo
                FROM PuertasDeEmbarque p
                LEFT JOIN Vuelos v ON v.ID_Puerta = p.ID_Puerta
                WHERE p.ID_Puerta = ?
            """;
            List<Object> params = Collections.singletonList(Integer.valueOf(buscarId));
            List<Map<String, Object>> resultados = repository.buscarPorConsulta(sql, params);
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ninguna puerta con ese ID.", Alert.AlertType.INFORMATION);
                return;
            }
            data.clear();
            for (Map<String, Object> fila : resultados) {
                PuertaDeEmbarqueVuelo puerta = new PuertaDeEmbarqueVuelo(
                        (int) fila.get("ID_Puerta"),
                        (String) fila.get("NumeroPuerta"),
                        (String) fila.get("Estado"),
                        (String) fila.get("NumeroVuelo")
                );
                data.add(puerta);
            }
            tablePuertas.setItems(data);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtNumeroPuerta.clear();
        // No limpiar comboEstado aquí, ya se limpia en handleLimpiarBusqueda
    }

    private void llenarCamposDesdeSeleccion() {
        if (puertaSeleccionada != null) {
            txtNumeroPuerta.setText(puertaSeleccionada.getNumeroPuerta());
            comboEstado.getSelectionModel().select(puertaSeleccionada.getEstado());
        }
    }

    @FXML
    private void handleAddPuerta() {
        String numeroPuerta = txtNumeroPuerta.getText().trim();
        String estado = comboEstado.getSelectionModel().getSelectedItem();

        if (numeroPuerta.isEmpty() || estado == null || estado.isEmpty()) {
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
                cargarTablaPuertas(null);
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
        String estado = comboEstado.getSelectionModel().getSelectedItem();
        if (numeroPuerta.isEmpty() || estado == null || estado.isEmpty()) {
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
                cargarTablaPuertas(null);
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
                cargarTablaPuertas(null);
                limpiarCampos();
                puertaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar puerta:\n" + e.getMessage(), Alert.AlertType.ERROR);
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