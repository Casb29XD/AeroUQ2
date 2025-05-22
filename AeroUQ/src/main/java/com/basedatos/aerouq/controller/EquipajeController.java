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
import java.util.List;

// PDF
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Desktop;

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
    @FXML private Button btnReporte;

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

    // Carga vuelos y actualiza el mapa y ComboBox
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
            comboVuelo.setValue(null);
            comboPasajero.getItems().clear();
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los vuelos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Carga pasajeros SOLO del vuelo seleccionado
    private void cargarPasajerosComboPorVuelo(Integer idVuelo) {
        comboPasajero.getItems().clear();
        pasajeroToId.clear();
        idToPasajero.clear();
        if (idVuelo == null) return;
        try {
            List<Map<String, Object>> pasajeros = repository.buscar("Pasajeros", "ID_Vuelo = ?", Collections.singletonList(idVuelo));
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

    // Se activa al seleccionar vuelo
    @FXML
    private void handleVueloSeleccionado() {
        String vueloSel = comboVuelo.getValue();
        Integer idVuelo = vueloToId.get(vueloSel);
        cargarPasajerosComboPorVuelo(idVuelo);
        comboPasajero.getSelectionModel().clearSelection();
        filtrarTabla();
    }

    // Se activa al seleccionar pasajero
    @FXML
    private void handlePasajeroSeleccionado() {
        filtrarTabla();
    }

    // Filtra tabla según vuelo y/o pasajero
    private void filtrarTabla() {
        String vueloSel = comboVuelo.getValue();
        String pasajeroSel = comboPasajero.getValue();
        Integer idVuelo = vueloToId.get(vueloSel);
        Integer idPasajero = pasajeroToId.get(pasajeroSel);
        data.clear();
        String sql = "SELECT e.ID_Maleta, e.CodigoDeBarras, e.Peso, e.ID_Vuelo, e.Estado, e.ID_Pasajero " +
                "FROM Equipajes e WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (idVuelo != null) {
            sql += " AND e.ID_Vuelo = ?";
            params.add(idVuelo);
        }
        if (idPasajero != null) {
            sql += " AND e.ID_Pasajero = ?";
            params.add(idPasajero);
        }
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i+1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
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
            mostrarAlerta("Error", "No se pudieron filtrar los equipajes:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Carga todos los equipajes (para "limpiar")
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
    public void handleAddEquipaje() {
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
    public void handleEditEquipaje() {
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
    public void handleDeleteEquipaje() {
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

    @FXML
    public void handleBuscarEquipaje() {
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
    public void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableEquipaje.getSelectionModel().clearSelection();
        cargarVuelosCombo();
        cargarTabla();
        equipajeSeleccionado = null;
    }

    private void limpiarCampos() {
        txtCodBarras.clear();
        txtPeso.clear();
        comboVuelo.getSelectionModel().clearSelection();
        comboPasajero.getSelectionModel().clearSelection();
        comboEstado.getSelectionModel().clearSelection();
        comboPasajero.getItems().clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (equipajeSeleccionado != null) {
            txtCodBarras.setText(equipajeSeleccionado.getCodigoDeBarras());
            txtPeso.setText(String.valueOf(equipajeSeleccionado.getPeso()));
            comboVuelo.setValue(idToVuelo.get(equipajeSeleccionado.getIdVuelo()));
            cargarPasajerosComboPorVuelo(equipajeSeleccionado.getIdVuelo());
            comboPasajero.setValue(idToPasajero.get(equipajeSeleccionado.getIdPasajero()));
            comboEstado.setValue(equipajeSeleccionado.getEstado());
        }
    }

    @FXML
    public void handleGenerarReporte() {
        List<Equipaje> equipajesParaReporte = new ArrayList<>(tableEquipaje.getItems());
        if (equipajesParaReporte.isEmpty()) {
            mostrarAlerta("Reporte", "No hay equipajes para generar el reporte.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            Document document = new Document();
            String filename = "ReporteEquipaje_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            String titulo = "Reporte de Equipaje";
            if (comboVuelo.getValue() != null) {
                titulo += " | Vuelo: " + comboVuelo.getValue();
            }
            if (comboPasajero.getValue() != null) {
                titulo += " | Pasajero: " + comboPasajero.getValue();
            }
            document.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.addCell("ID Maleta");
            table.addCell("Código de Barras");
            table.addCell("Peso");
            table.addCell("Vuelo");
            table.addCell("Pasajero");
            table.addCell("Estado");

            for (Equipaje eq : equipajesParaReporte) {
                table.addCell(String.valueOf(eq.getIdMaleta()));
                table.addCell(eq.getCodigoDeBarras());
                table.addCell(String.valueOf(eq.getPeso()));
                table.addCell(idToVuelo.getOrDefault(eq.getIdVuelo(), String.valueOf(eq.getIdVuelo())));
                table.addCell(idToPasajero.getOrDefault(eq.getIdPasajero(), String.valueOf(eq.getIdPasajero())));
                table.addCell(eq.getEstado());
            }
            document.add(table);
            document.close();

            // Abrir el PDF automáticamente
            File pdfFile = new File(filename);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            }

            mostrarAlerta("Reporte", "Reporte generado exitosamente: " + filename, Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo generar el PDF: " + ex.getMessage(), Alert.AlertType.ERROR);
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