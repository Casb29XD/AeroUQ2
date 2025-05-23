package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Vuelo;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;
import java.util.List;

public class VuelosController {

    @FXML private TextField txtNumeroVuelo;
    @FXML private ComboBox<String> comboAerolinea;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtDestino;
    @FXML private DatePicker datePickerSalida;
    @FXML private TextField txtHoraSalida;
    @FXML private DatePicker datePickerLlegada;
    @FXML private TextField txtHoraLlegada;
    @FXML private ComboBox<String> comboEstadoVuelo;
    @FXML private ComboBox<String> comboPuerta;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnReporte;

    @FXML private TableView<Vuelo> tableVuelos;
    @FXML private TableColumn<Vuelo, String> colIdVuelo;
    @FXML private TableColumn<Vuelo, String> colNumeroVuelo;
    @FXML private TableColumn<Vuelo, String> colAerolinea;
    @FXML private TableColumn<Vuelo, String> colOrigen;
    @FXML private TableColumn<Vuelo, String> colDestino;
    @FXML private TableColumn<Vuelo, String> colFechaHoraSalida;
    @FXML private TableColumn<Vuelo, String> colFechaHoraLlegada;
    @FXML private TableColumn<Vuelo, String> colEstadoVuelo;
    @FXML private TableColumn<Vuelo, String> colPuerta;

    @FXML private ComboBox<String> comboFiltroEstadoVuelo;
    @FXML private ComboBox<String> comboFiltroAerolinea;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Vuelo> data = FXCollections.observableArrayList();

    private Vuelo vueloSeleccionado = null;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Map<String, Integer> nombreAerolineaToId = new HashMap<>();
    private Map<Integer, String> idToNombreAerolinea = new HashMap<>();
    private Map<String, Integer> numeroPuertaToId = new HashMap<>();
    private Map<Integer, String> idToNumeroPuerta = new HashMap<>();

    private static final List<String> ESTADOS = Arrays.asList("En horario", "Retrasado", "Cancelado");

    @FXML
    private void initialize() {
        comboEstadoVuelo.setItems(FXCollections.observableArrayList(ESTADOS));
        cargarAerolineasCombo();
        cargarPuertasCombo();

        inicializarFiltros();

        colIdVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdVuelo())));
        colNumeroVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroVuelo()));
        colAerolinea.setCellValueFactory(cellData -> new SimpleStringProperty(
                idToNombreAerolinea.getOrDefault(cellData.getValue().getIdAerolinea(), String.valueOf(cellData.getValue().getIdAerolinea()))
        ));
        colOrigen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen()));
        colDestino.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestino()));
        colFechaHoraSalida.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaHoraSalida();
            return new SimpleStringProperty(fecha != null ? dateTimeFormat.format(fecha) : "");
        });
        colFechaHoraLlegada.setCellValueFactory(cellData -> {
            Date fecha = cellData.getValue().getFechaHoraLlegada();
            return new SimpleStringProperty(fecha != null ? dateTimeFormat.format(fecha) : "");
        });
        colEstadoVuelo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstadoVuelo()));
        colPuerta.setCellValueFactory(cellData -> new SimpleStringProperty(
                idToNumeroPuerta.getOrDefault(cellData.getValue().getIdPuerta(), String.valueOf(cellData.getValue().getIdPuerta()))
        ));

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

    private void inicializarFiltros() {
        if (comboFiltroEstadoVuelo != null) {
            comboFiltroEstadoVuelo.setItems(FXCollections.observableArrayList(ESTADOS));
            comboFiltroEstadoVuelo.setOnAction(e -> filtrarTabla());
        }
        if (comboFiltroAerolinea != null) {
            List<String> aerolineas = new ArrayList<>(nombreAerolineaToId.keySet());
            comboFiltroAerolinea.setItems(FXCollections.observableArrayList(aerolineas));
            comboFiltroAerolinea.setOnAction(e -> filtrarTabla());
        }
    }

    private void cargarAerolineasCombo() {
        comboAerolinea.getItems().clear();
        nombreAerolineaToId.clear();
        idToNombreAerolinea.clear();
        try {
            List<Map<String, Object>> aerolineas = repository.buscar("Aerolineas");
            for (Map<String, Object> fila : aerolineas) {
                int id = ((Number) fila.get("ID_Aerolinea")).intValue();
                String nombre = (String) fila.get("Nombre");
                nombreAerolineaToId.put(nombre, id);
                idToNombreAerolinea.put(id, nombre);
                comboAerolinea.getItems().add(nombre);
            }
            if (comboFiltroAerolinea != null) {
                comboFiltroAerolinea.setItems(FXCollections.observableArrayList(nombreAerolineaToId.keySet()));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aerolíneas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarPuertasCombo() {
        comboPuerta.getItems().clear();
        numeroPuertaToId.clear();
        idToNumeroPuerta.clear();
        try {
            List<Map<String, Object>> puertas = repository.buscar("PuertasDeEmbarque");
            for (Map<String, Object> fila : puertas) {
                int id = ((Number) fila.get("ID_Puerta")).intValue();
                String numeroPuerta = (String) fila.get("NumeroPuerta");
                numeroPuertaToId.put(numeroPuerta, id);
                idToNumeroPuerta.put(id, numeroPuerta);
                comboPuerta.getItems().add(numeroPuerta);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las puertas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT v.ID_Vuelo, v.NumeroVuelo, v.ID_Aerolinea, a.Nombre as NombreAerolinea, v.Origen, v.Destino, v.FechaHoraSalida, v.FechaHoraLlegada, v.EstadoVuelo, v.ID_Puerta, p.NumeroPuerta " +
                "FROM Vuelos v " +
                "JOIN Aerolineas a ON v.ID_Aerolinea = a.ID_Aerolinea " +
                "LEFT JOIN PuertasDeEmbarque p ON v.ID_Puerta = p.ID_Puerta";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idAerolinea = rs.getInt("ID_Aerolinea");
                String nombreAerolinea = rs.getString("NombreAerolinea");
                idToNombreAerolinea.put(idAerolinea, nombreAerolinea);

                int idPuerta = rs.getObject("ID_Puerta") != null ? rs.getInt("ID_Puerta") : 0;
                String numeroPuerta = rs.getString("NumeroPuerta");
                idToNumeroPuerta.put(idPuerta, numeroPuerta);

                Vuelo vuelo = new Vuelo(
                        rs.getInt("ID_Vuelo"),
                        rs.getString("NumeroVuelo"),
                        idAerolinea,
                        rs.getString("Origen"),
                        rs.getString("Destino"),
                        rs.getTimestamp("FechaHoraSalida"),
                        rs.getTimestamp("FechaHoraLlegada"),
                        rs.getString("EstadoVuelo"),
                        idPuerta
                );
                data.add(vuelo);
            }
            tableVuelos.setItems(data);

            filtrarTabla();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtrarTabla() {
        String estadoFiltro = comboFiltroEstadoVuelo != null ? comboFiltroEstadoVuelo.getValue() : null;
        String aerolineaFiltro = comboFiltroAerolinea != null ? comboFiltroAerolinea.getValue() : null;
        ObservableList<Vuelo> filtrados = FXCollections.observableArrayList();

        for (Vuelo vuelo : data) {
            boolean coincide = true;
            if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
                coincide = coincide && estadoFiltro.equals(vuelo.getEstadoVuelo());
            }
            if (aerolineaFiltro != null && !aerolineaFiltro.isEmpty()) {
                coincide = coincide && aerolineaFiltro.equals(idToNombreAerolinea.get(vuelo.getIdAerolinea()));
            }
            if (coincide) filtrados.add(vuelo);
        }

        if ((estadoFiltro == null || estadoFiltro.isEmpty()) && (aerolineaFiltro == null || aerolineaFiltro.isEmpty())) {
            tableVuelos.setItems(data);
        } else {
            tableVuelos.setItems(filtrados);
        }
    }

    @FXML
    private void handleBuscarVuelo() {
        String buscar = txtBuscar.getText().trim();
        String sql = "SELECT v.ID_Vuelo, v.NumeroVuelo, v.ID_Aerolinea, a.Nombre as NombreAerolinea, v.Origen, v.Destino, v.FechaHoraSalida, v.FechaHoraLlegada, v.EstadoVuelo, v.ID_Puerta, p.NumeroPuerta " +
                "FROM Vuelos v " +
                "JOIN Aerolineas a ON v.ID_Aerolinea = a.ID_Aerolinea " +
                "LEFT JOIN PuertasDeEmbarque p ON v.ID_Puerta = p.ID_Puerta " +
                "WHERE v.ID_Vuelo = ? OR v.NumeroVuelo LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try {
                int id = Integer.parseInt(buscar);
                stmt.setInt(1, id);
            } catch (NumberFormatException e) {
                stmt.setInt(1, -1);
            }
            stmt.setString(2, "%" + buscar + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idAerolinea = rs.getInt("ID_Aerolinea");
                    String nombreAerolinea = rs.getString("NombreAerolinea");
                    idToNombreAerolinea.put(idAerolinea, nombreAerolinea);

                    int idPuerta = rs.getObject("ID_Puerta") != null ? rs.getInt("ID_Puerta") : 0;
                    String numeroPuerta = rs.getString("NumeroPuerta");
                    idToNumeroPuerta.put(idPuerta, numeroPuerta);

                    Vuelo vuelo = new Vuelo(
                            rs.getInt("ID_Vuelo"),
                            rs.getString("NumeroVuelo"),
                            idAerolinea,
                            rs.getString("Origen"),
                            rs.getString("Destino"),
                            rs.getTimestamp("FechaHoraSalida"),
                            rs.getTimestamp("FechaHoraLlegada"),
                            rs.getString("EstadoVuelo"),
                            idPuerta
                    );
                    data.add(vuelo);
                }
            }
            tableVuelos.setItems(data);
            if (!data.isEmpty()) {
                vueloSeleccionado = data.get(0);
                tableVuelos.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron vuelos con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar vuelo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableVuelos.getSelectionModel().clearSelection();
        if (comboFiltroEstadoVuelo != null) comboFiltroEstadoVuelo.getSelectionModel().clearSelection();
        if (comboFiltroAerolinea != null) comboFiltroAerolinea.getSelectionModel().clearSelection();
        cargarTabla();
        vueloSeleccionado = null;
    }

    private void limpiarCampos() {
        txtNumeroVuelo.clear();
        comboAerolinea.getSelectionModel().clearSelection();
        txtOrigen.clear();
        txtDestino.clear();
        datePickerSalida.setValue(null);
        txtHoraSalida.clear();
        datePickerLlegada.setValue(null);
        txtHoraLlegada.clear();
        comboEstadoVuelo.getSelectionModel().clearSelection();
        comboPuerta.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (vueloSeleccionado != null) {
            txtNumeroVuelo.setText(vueloSeleccionado.getNumeroVuelo());
            comboAerolinea.setValue(idToNombreAerolinea.get(vueloSeleccionado.getIdAerolinea()));
            txtOrigen.setText(vueloSeleccionado.getOrigen());
            txtDestino.setText(vueloSeleccionado.getDestino());

            if (vueloSeleccionado.getFechaHoraSalida() != null) {
                Date fecha = vueloSeleccionado.getFechaHoraSalida();
                LocalDate localDate = new java.sql.Date(fecha.getTime()).toLocalDate();
                datePickerSalida.setValue(localDate);
                txtHoraSalida.setText(new SimpleDateFormat("HH:mm").format(fecha));
            } else {
                datePickerSalida.setValue(null);
                txtHoraSalida.clear();
            }
            if (vueloSeleccionado.getFechaHoraLlegada() != null) {
                Date fecha = vueloSeleccionado.getFechaHoraLlegada();
                LocalDate localDate = new java.sql.Date(fecha.getTime()).toLocalDate();
                datePickerLlegada.setValue(localDate);
                txtHoraLlegada.setText(new SimpleDateFormat("HH:mm").format(fecha));
            } else {
                datePickerLlegada.setValue(null);
                txtHoraLlegada.clear();
            }

            comboEstadoVuelo.setValue(vueloSeleccionado.getEstadoVuelo());
            comboPuerta.setValue(idToNumeroPuerta.get(vueloSeleccionado.getIdPuerta()));
        }
    }

    @FXML
    private void handleAddVuelo() {
        try {
            String numeroVuelo = txtNumeroVuelo.getText().trim();
            String nombreAerolinea = comboAerolinea.getValue();
            Integer idAerolinea = nombreAerolineaToId.get(nombreAerolinea);
            String origen = txtOrigen.getText().trim();
            String destino = txtDestino.getText().trim();

            Date fechaHoraSalida = getFechaHora(datePickerSalida, txtHoraSalida);
            Date fechaHoraLlegada = getFechaHora(datePickerLlegada, txtHoraLlegada);

            String estadoVuelo = comboEstadoVuelo.getValue();
            String numeroPuerta = comboPuerta.getValue();
            Integer idPuerta = numeroPuertaToId.get(numeroPuerta);

            if (numeroVuelo.isEmpty() || nombreAerolinea == null || idAerolinea == null || origen.isEmpty() || destino.isEmpty() || estadoVuelo == null || numeroPuerta == null || idPuerta == null) {
                mostrarAlerta("Advertencia", "Todos los campos obligatorios deben estar completos.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("NumeroVuelo", numeroVuelo);
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Origen", origen);
            datos.put("Destino", destino);
            datos.put("FechaHoraSalida", fechaHoraSalida != null ? new java.sql.Timestamp(fechaHoraSalida.getTime()) : null);
            datos.put("FechaHoraLlegada", fechaHoraLlegada != null ? new java.sql.Timestamp(fechaHoraLlegada.getTime()) : null);
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
            mostrarAlerta("Advertencia", "El formato de hora debe ser HH:mm y se debe seleccionar una fecha.", Alert.AlertType.WARNING);
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
            String nombreAerolinea = comboAerolinea.getValue();
            Integer idAerolinea = nombreAerolineaToId.get(nombreAerolinea);
            String origen = txtOrigen.getText().trim();
            String destino = txtDestino.getText().trim();

            Date fechaHoraSalida = getFechaHora(datePickerSalida, txtHoraSalida);
            Date fechaHoraLlegada = getFechaHora(datePickerLlegada, txtHoraLlegada);

            String estadoVuelo = comboEstadoVuelo.getValue();
            String numeroPuerta = comboPuerta.getValue();
            Integer idPuerta = numeroPuertaToId.get(numeroPuerta);

            if (numeroVuelo.isEmpty() || nombreAerolinea == null || idAerolinea == null || origen.isEmpty() || destino.isEmpty() || estadoVuelo == null || numeroPuerta == null || idPuerta == null) {
                mostrarAlerta("Advertencia", "Todos los campos obligatorios deben estar completos.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("NumeroVuelo", numeroVuelo);
            datos.put("ID_Aerolinea", idAerolinea);
            datos.put("Origen", origen);
            datos.put("Destino", destino);
            datos.put("FechaHoraSalida", fechaHoraSalida != null ? new java.sql.Timestamp(fechaHoraSalida.getTime()) : null);
            datos.put("FechaHoraLlegada", fechaHoraLlegada != null ? new java.sql.Timestamp(fechaHoraLlegada.getTime()) : null);
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
            mostrarAlerta("Advertencia", "El formato de hora debe ser HH:mm y se debe seleccionar una fecha.", Alert.AlertType.WARNING);
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

    private Date getFechaHora(DatePicker datePicker, TextField txtHora) throws ParseException {
        LocalDate fecha = datePicker.getValue();
        String hora = txtHora.getText().trim();
        if (fecha == null || hora.isEmpty()) return null;
        String fechaHoraStr = fecha + " " + (hora.length() == 5 ? hora : "00:00") + ":00";
        return dateTimeFormat.parse(fechaHoraStr);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // === MÉTODO DE REPORTE PDF ===
    @FXML
    private void handleReporteVueloReporte() {
        Vuelo vuelo = tableVuelos.getSelectionModel().getSelectedItem();
        if (vuelo == null) {
            mostrarAlerta("Reporte PDF", "Seleccione un vuelo para generar el reporte.", Alert.AlertType.WARNING);
            return;
        }

        int idVuelo = vuelo.getIdVuelo();
        String nombreAerolinea = idToNombreAerolinea.get(vuelo.getIdAerolinea());
        String numeroVuelo = vuelo.getNumeroVuelo();
        String origen = vuelo.getOrigen();
        String destino = vuelo.getDestino();
        java.util.Date fechaSalida = vuelo.getFechaHoraSalida();
        java.util.Date fechaLlegada = vuelo.getFechaHoraLlegada();
        String estadoVuelo = vuelo.getEstadoVuelo();
        String puerta = idToNumeroPuerta.get(vuelo.getIdPuerta());

        List<String[]> pasajerosConEquipaje = new ArrayList<>();
        String sql = """
                SELECT p.Nombre, p.Apellido, p.DocumentoIdentidad, COUNT(e.ID_Maleta) AS NumeroEquipaje
                FROM Pasajeros p
                LEFT JOIN Equipajes e ON p.ID_Pasajero = e.ID_Pasajero AND e.ID_Vuelo = ?
                WHERE p.ID_Vuelo = ?
                GROUP BY p.ID_Pasajero, p.Nombre, p.Apellido, p.DocumentoIdentidad
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVuelo);
            stmt.setInt(2, idVuelo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");
                    String documento = rs.getString("DocumentoIdentidad");
                    String numEquipaje = String.valueOf(rs.getInt("NumeroEquipaje"));
                    pasajerosConEquipaje.add(new String[]{nombre, apellido, documento, numEquipaje});
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al consultar pasajeros y equipaje: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        String fileName = "ReporteVuelo_" + numeroVuelo.replaceAll("\\s+","_") + ".pdf";
        File pdfFile = new File(fileName);

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Título
            Paragraph title = new Paragraph("Reporte de Vuelo", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Info Aerolínea y Vuelo
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell("Aerolínea:");
            infoTable.addCell(nombreAerolinea);
            infoTable.addCell("Número de Vuelo:");
            infoTable.addCell(numeroVuelo);
            infoTable.addCell("Origen:");
            infoTable.addCell(origen);
            infoTable.addCell("Destino:");
            infoTable.addCell(destino);
            infoTable.addCell("Fecha/Hora Salida:");
            infoTable.addCell(fechaSalida != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(fechaSalida) : "");
            infoTable.addCell("Fecha/Hora Llegada:");
            infoTable.addCell(fechaLlegada != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(fechaLlegada) : "");
            infoTable.addCell("Estado del Vuelo:");
            infoTable.addCell(estadoVuelo);
            infoTable.addCell("Puerta de Embarque:");
            infoTable.addCell(puerta);
            document.add(infoTable);

            document.add(new Paragraph(" "));

            // Lista de pasajeros y equipaje
            Paragraph p = new Paragraph("Lista de Pasajeros y Equipaje", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            p.setSpacingAfter(10f);
            document.add(p);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 3f, 3f, 2f});
            table.addCell("Nombre");
            table.addCell("Apellido");
            table.addCell("Documento");
            table.addCell("Nº Equipaje");

            for (String[] pasajero : pasajerosConEquipaje) {
                table.addCell(pasajero[0]);
                table.addCell(pasajero[1]);
                table.addCell(pasajero[2]);
                table.addCell(pasajero[3]);
            }
            document.add(table);

            document.close();
            mostrarAlerta("Éxito", "Reporte PDF generado correctamente en la raíz del proyecto.", Alert.AlertType.INFORMATION);

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo generar o abrir el PDF: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
}