package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.PuertaDeEmbarque;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleIntegerProperty;
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

    // Para la tabla de vuelos con puertas ocupadas
    @FXML private TableView<VueloOcupado> tableVuelosOcupados;
    @FXML private TableColumn<VueloOcupado, Number> colIdVuelo;
    @FXML private TableColumn<VueloOcupado, String> colNumeroVuelo;
    @FXML private TableColumn<VueloOcupado, String> colOrigen;
    @FXML private TableColumn<VueloOcupado, String> colDestino;
    @FXML private TableColumn<VueloOcupado, String> colFechaSalida;
    @FXML private TableColumn<VueloOcupado, String> colFechaLlegada;
    @FXML private TableColumn<VueloOcupado, String> colEstadoVuelo;
    @FXML private TableColumn<VueloOcupado, String> colNumeroPuertaVuelo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<PuertaDeEmbarque> data = FXCollections.observableArrayList();
    private ObservableList<VueloOcupado> vuelosOcupadosData = FXCollections.observableArrayList();

    private PuertaDeEmbarque puertaSeleccionada = null;

    // Modelo auxiliar para la tabla de vuelos
    public static class VueloOcupado {
        private final SimpleIntegerProperty idVuelo;
        private final SimpleStringProperty numeroVuelo;
        private final SimpleStringProperty origen;
        private final SimpleStringProperty destino;
        private final SimpleStringProperty fechaSalida;
        private final SimpleStringProperty fechaLlegada;
        private final SimpleStringProperty estadoVuelo;
        private final SimpleStringProperty numeroPuerta;

        public VueloOcupado(int idVuelo, String numeroVuelo, String origen, String destino, String fechaSalida, String fechaLlegada, String estadoVuelo, String numeroPuerta) {
            this.idVuelo = new SimpleIntegerProperty(idVuelo);
            this.numeroVuelo = new SimpleStringProperty(numeroVuelo);
            this.origen = new SimpleStringProperty(origen);
            this.destino = new SimpleStringProperty(destino);
            this.fechaSalida = new SimpleStringProperty(fechaSalida);
            this.fechaLlegada = new SimpleStringProperty(fechaLlegada);
            this.estadoVuelo = new SimpleStringProperty(estadoVuelo);
            this.numeroPuerta = new SimpleStringProperty(numeroPuerta);
        }

        public int getIdVuelo() { return idVuelo.get(); }
        public String getNumeroVuelo() { return numeroVuelo.get(); }
        public String getOrigen() { return origen.get(); }
        public String getDestino() { return destino.get(); }
        public String getFechaSalida() { return fechaSalida.get(); }
        public String getFechaLlegada() { return fechaLlegada.get(); }
        public String getEstadoVuelo() { return estadoVuelo.get(); }
        public String getNumeroPuerta() { return numeroPuerta.get(); }
    }

    @FXML
    private void initialize() {
        // Puertas
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

        // Vuelos ocupados
        colIdVuelo.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getIdVuelo()));
        colNumeroVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroVuelo()));
        colOrigen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen()));
        colDestino.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestino()));
        colFechaSalida.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaSalida()));
        colFechaLlegada.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaLlegada()));
        colEstadoVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstadoVuelo()));
        colNumeroPuertaVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroPuerta()));
        tableVuelosOcupados.setItems(vuelosOcupadosData);

        cargarVuelosConPuertaOcupada();
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

    private void cargarVuelosConPuertaOcupada() {
        vuelosOcupadosData.clear();
        try {
            String sql = """
                SELECT v.ID_Vuelo, v.NumeroVuelo, v.Origen, v.Destino, v.FechaHoraSalida, v.FechaHoraLlegada, v.EstadoVuelo, p.NumeroPuerta
                FROM Vuelos v
                JOIN PuertasDeEmbarque p ON v.ID_Puerta = p.ID_Puerta
                WHERE p.Estado = ?
            """;
            List<Object> parametros = Collections.singletonList("Ocupada");
            List<Map<String, Object>> resultados = repository.buscarPorConsulta(sql, parametros);

            for (Map<String, Object> fila : resultados) {
                VueloOcupado vuelo = new VueloOcupado(
                        (int) fila.get("ID_Vuelo"),
                        (String) fila.get("NumeroVuelo"),
                        (String) fila.get("Origen"),
                        (String) fila.get("Destino"),
                        (fila.get("FechaHoraSalida") != null ? fila.get("FechaHoraSalida").toString() : ""),
                        (fila.get("FechaHoraLlegada") != null ? fila.get("FechaHoraLlegada").toString() : ""),
                        (String) fila.get("EstadoVuelo"),
                        (String) fila.get("NumeroPuerta")
                );
                vuelosOcupadosData.add(vuelo);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos con puertas ocupadas:\n" + e.getMessage(), Alert.AlertType.ERROR);
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
                cargarVuelosConPuertaOcupada();
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
                cargarVuelosConPuertaOcupada();
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
                cargarVuelosConPuertaOcupada();
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