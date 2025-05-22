package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.MantenimientoAeronave;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MantenimientoController {

    public static class AeronaveComboItem {
        private final int idAeronave;
        private final String matricula;
        public AeronaveComboItem(int id, String matricula) {
            this.idAeronave = id;
            this.matricula = matricula;
        }
        public int getIdAeronave() { return idAeronave; }
        public String getMatricula() { return matricula; }
        @Override public String toString() { return matricula; }
    }

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<AeronaveComboItem> comboAeronave;
    @FXML private TextField txtDescripcion;
    @FXML private DatePicker dateFechaMantenimiento;
    @FXML private ComboBox<String> comboEstado;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<MantenimientoAeronave> tableMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colIdMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colMatricula;
    @FXML private TableColumn<MantenimientoAeronave, String> colDesc;
    @FXML private TableColumn<MantenimientoAeronave, String> colFechaMantenimiento;
    @FXML private TableColumn<MantenimientoAeronave, String> colEstado;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<MantenimientoAeronave> data = FXCollections.observableArrayList();
    private MantenimientoAeronave mantenimientoSeleccionado = null;

    @FXML
    private void initialize() {
        cargarAeronavesCombo();

        colIdMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdMantenimiento())));
        colMatricula.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMatricula()));
        colDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colFechaMantenimiento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaMantenimiento()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        cargarTabla();

        tableMantenimiento.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        mantenimientoSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarAeronavesCombo() {
        comboAeronave.getItems().clear();
        try {
            List<Map<String, Object>> aeronaves = repository.buscar("Aeronaves");
            for (Map<String, Object> fila : aeronaves) {
                comboAeronave.getItems().add(
                        new AeronaveComboItem(
                                ((Number) fila.get("ID_Aeronave")).intValue(),
                                (String) fila.get("Matricula")
                        )
                );
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aeronaves:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT m.ID_Mantenimiento, m.ID_Aeronave, a.Matricula, m.Descripcion, m.FechaMantenimiento, m.Estado " +
                "FROM MantenimientoAeronaves m JOIN Aeronaves a ON m.ID_Aeronave = a.ID_Aeronave";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MantenimientoAeronave mant = new MantenimientoAeronave(
                        rs.getInt("ID_Mantenimiento"),
                        rs.getInt("ID_Aeronave"),
                        rs.getString("Matricula"),
                        rs.getString("Descripcion"),
                        rs.getString("FechaMantenimiento"),
                        rs.getString("Estado")
                );
                data.add(mant);
            }
            tableMantenimiento.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los mantenimientos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarAeronave() {
        String texto = txtBuscar.getText().trim();
        String sql = "SELECT m.ID_Mantenimiento, m.ID_Aeronave, a.Matricula, m.Descripcion, m.FechaMantenimiento, m.Estado " +
                "FROM MantenimientoAeronaves m JOIN Aeronaves a ON m.ID_Aeronave = a.ID_Aeronave " +
                "WHERE a.Matricula LIKE ? OR m.Descripcion LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + texto + "%");
            stmt.setString(2, "%" + texto + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MantenimientoAeronave mant = new MantenimientoAeronave(
                            rs.getInt("ID_Mantenimiento"),
                            rs.getInt("ID_Aeronave"),
                            rs.getString("Matricula"),
                            rs.getString("Descripcion"),
                            rs.getString("FechaMantenimiento"),
                            rs.getString("Estado")
                    );
                    data.add(mant);
                }
            }
            tableMantenimiento.setItems(data);
            if (!data.isEmpty()) {
                mantenimientoSeleccionado = data.get(0);
                tableMantenimiento.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron mantenimientos con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        comboAeronave.getSelectionModel().clearSelection();
        txtDescripcion.clear();
        dateFechaMantenimiento.setValue(null);
        comboEstado.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (mantenimientoSeleccionado != null) {
            for (AeronaveComboItem item : comboAeronave.getItems()) {
                if (item.getIdAeronave() == mantenimientoSeleccionado.getIdAeronave()) {
                    comboAeronave.setValue(item);
                    break;
                }
            }
            txtDescripcion.setText(mantenimientoSeleccionado.getDescripcion());
            try {
                dateFechaMantenimiento.setValue(LocalDate.parse(mantenimientoSeleccionado.getFechaMantenimiento()));
            } catch (Exception e) {
                dateFechaMantenimiento.setValue(null);
            }
            comboEstado.setValue(mantenimientoSeleccionado.getEstado());
        }
    }

    @FXML
    private void handleAddMantenimiento() {
        AeronaveComboItem aeronaveSel = comboAeronave.getValue();
        String desc = txtDescripcion.getText().trim();
        LocalDate fecha = dateFechaMantenimiento.getValue();
        String estado = comboEstado.getValue();

        if (aeronaveSel == null || desc.isEmpty() || fecha == null || estado == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aeronave", aeronaveSel.getIdAeronave());
            datos.put("Descripcion", desc);
            datos.put("FechaMantenimiento", fecha.toString());
            datos.put("Estado", estado);

            int filas = repository.insertar("MantenimientoAeronaves", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditMantenimiento() {
        if (mantenimientoSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un mantenimiento para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        AeronaveComboItem aeronaveSel = comboAeronave.getValue();
        String desc = txtDescripcion.getText().trim();
        LocalDate fecha = dateFechaMantenimiento.getValue();
        String estado = comboEstado.getValue();

        if (aeronaveSel == null || desc.isEmpty() || fecha == null || estado == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("ID_Aeronave", aeronaveSel.getIdAeronave());
            datos.put("Descripcion", desc);
            datos.put("FechaMantenimiento", fecha.toString());
            datos.put("Estado", estado);

            int filas = repository.actualizar(
                    "MantenimientoAeronaves",
                    datos,
                    "ID_Mantenimiento = ?",
                    Collections.singletonList(mantenimientoSeleccionado.getIdMantenimiento())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteMantenimiento() {
        if (mantenimientoSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un mantenimiento para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "MantenimientoAeronaves",
                    "ID_Mantenimiento = ?",
                    Collections.singletonList(mantenimientoSeleccionado.getIdMantenimiento())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Mantenimiento eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                mantenimientoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar mantenimiento:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // ...tu código previo...
    @FXML
    public void handleFiltrarPorEstado() {
        String estado = comboEstado.getValue();
        if (estado != null && !estado.isEmpty()) {
            data.clear();
            String sql = "SELECT m.ID_Mantenimiento, m.ID_Aeronave, a.Matricula, m.Descripcion, m.FechaMantenimiento, m.Estado " +
                    "FROM MantenimientoAeronaves m JOIN Aeronaves a ON m.ID_Aeronave = a.ID_Aeronave " +
                    "WHERE m.Estado = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, estado);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        MantenimientoAeronave mant = new MantenimientoAeronave(
                                rs.getInt("ID_Mantenimiento"),
                                rs.getInt("ID_Aeronave"),
                                rs.getString("Matricula"),
                                rs.getString("Descripcion"),
                                rs.getString("FechaMantenimiento"),
                                rs.getString("Estado")
                        );
                        data.add(mant);
                    }
                }
                tableMantenimiento.setItems(data);
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudieron filtrar los mantenimientos:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        comboEstado.getSelectionModel().clearSelection();
        limpiarCampos();
        tableMantenimiento.getSelectionModel().clearSelection();
        cargarTabla();
        mantenimientoSeleccionado = null;
    }
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}