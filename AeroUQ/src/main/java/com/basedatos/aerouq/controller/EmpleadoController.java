package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Empleado;
import com.basedatos.aerouq.repository.DatabaseRepository;
import com.basedatos.aerouq.config.DatabaseConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.*;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;


public class EmpleadoController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private ComboBox<String> comboCargo;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnReporte;

    @FXML private TableView<Empleado> tableEmpleado;
    @FXML private TableColumn<Empleado, String> colIdEmpleado;
    @FXML private TableColumn<Empleado, String> colDocumento;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellido;
    @FXML private TableColumn<Empleado, String> colCargo;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Empleado> data = FXCollections.observableArrayList();

    private Empleado empleadoSeleccionado = null;
    private Map<String, Integer> nombreCargoToId = new HashMap<>();
    private Map<Integer, String> idToNombreCargo = new HashMap<>();

    @FXML
    private void initialize() {
        colIdEmpleado.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdEmpleado())));
        colDocumento.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumento()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colCargo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCargo()));

        cargarCargosCombo();
        cargarTabla();

        comboCargo.setOnAction(event -> handleCargoSeleccionado());

        tableEmpleado.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        empleadoSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarCargosCombo() {
        comboCargo.getItems().clear();
        nombreCargoToId.clear();
        idToNombreCargo.clear();
        try {
            List<Map<String, Object>> cargos = repository.buscar("Cargos");
            for (Map<String, Object> fila : cargos) {
                int idCargo = ((Number) fila.get("idCargo")).intValue();
                String nombreCargo = (String) fila.get("nombreCargo");
                nombreCargoToId.put(nombreCargo, idCargo);
                idToNombreCargo.put(idCargo, nombreCargo);
                comboCargo.getItems().add(nombreCargo);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los cargos:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        data.clear();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Empleado emp = new Empleado(
                        rs.getInt("ID_Empleado"),
                        rs.getString("DocumentoIdentidad"),
                        rs.getString("Nombre"),
                        rs.getString("Apellido"),
                        rs.getInt("idCargo"),
                        rs.getString("nombreCargo")
                );
                data.add(emp);
            }
            tableEmpleado.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los empleados:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCargoSeleccionado() {
        String cargoSeleccionado = comboCargo.getValue();
        if (cargoSeleccionado == null || cargoSeleccionado.isEmpty()) {
            cargarTabla();
            return;
        }
        Integer idCargo = nombreCargoToId.get(cargoSeleccionado);
        if (idCargo == null) {
            cargarTabla();
            return;
        }
        data.clear();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo " +
                "WHERE e.idCargo = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCargo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Empleado emp = new Empleado(
                            rs.getInt("ID_Empleado"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getInt("idCargo"),
                            rs.getString("nombreCargo")
                    );
                    data.add(emp);
                }
            }
            tableEmpleado.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron filtrar por cargo:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarEmpleado() {
        String buscar = txtBuscar.getText().trim();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo " +
                "WHERE e.ID_Empleado = ? OR e.Nombre LIKE ? OR e.Apellido LIKE ? OR c.nombreCargo LIKE ?";
        data.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try {
                int id = Integer.parseInt(buscar);
                stmt.setInt(1, id);
            } catch (NumberFormatException e) {
                stmt.setInt(1, -1);
            }
            String like = "%" + buscar + "%";
            stmt.setString(2, like);
            stmt.setString(3, like);
            stmt.setString(4, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Empleado emp = new Empleado(
                            rs.getInt("ID_Empleado"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getInt("idCargo"),
                            rs.getString("nombreCargo")
                    );
                    data.add(emp);
                }
            }
            tableEmpleado.setItems(data);
            if (!data.isEmpty()) {
                empleadoSeleccionado = data.get(0);
                tableEmpleado.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontró ningún empleado.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        comboCargo.getSelectionModel().clearSelection();
        tableEmpleado.getSelectionModel().clearSelection();
        cargarTabla();
        empleadoSeleccionado = null;
    }

    private void limpiarCampos() {
        txtDocumento.clear();
        txtNombre.clear();
        txtApellido.clear();
        comboCargo.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (empleadoSeleccionado != null) {
            txtDocumento.setText(empleadoSeleccionado.getDocumento());
            txtNombre.setText(empleadoSeleccionado.getNombre());
            txtApellido.setText(empleadoSeleccionado.getApellido());
            comboCargo.setValue(empleadoSeleccionado.getNombreCargo());
        }
    }

    @FXML
    private void handleAddEmpleado() {
        try {
            String documento = txtDocumento.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String nombreCargo = comboCargo.getValue();
            Integer idCargo = nombreCargoToId.get(nombreCargo);

            if (documento.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || nombreCargo == null || idCargo == null) {
                mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("idCargo", idCargo);

            int filas = repository.insertar("Empleados", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un empleado para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String documento = txtDocumento.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String nombreCargo = comboCargo.getValue();
            Integer idCargo = nombreCargoToId.get(nombreCargo);

            if (documento.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || nombreCargo == null || idCargo == null) {
                mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("DocumentoIdentidad", documento);
            datos.put("Nombre", nombre);
            datos.put("Apellido", apellido);
            datos.put("idCargo", idCargo);

            int filas = repository.actualizar(
                    "Empleados",
                    datos,
                    "ID_Empleado = ?",
                    Collections.singletonList(empleadoSeleccionado.getIdEmpleado())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un empleado para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Empleados",
                    "ID_Empleado = ?",
                    Collections.singletonList(empleadoSeleccionado.getIdEmpleado())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Empleado eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                empleadoSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReporteCargo() {
        String cargoSeleccionado = comboCargo.getValue();
        if (cargoSeleccionado == null || cargoSeleccionado.isEmpty()) {
            mostrarAlerta("Reporte PDF", "Seleccione un cargo para generar el reporte.", Alert.AlertType.WARNING);
            return;
        }
        Integer idCargo = nombreCargoToId.get(cargoSeleccionado);
        if (idCargo == null) {
            mostrarAlerta("Reporte PDF", "Cargo no válido.", Alert.AlertType.WARNING);
            return;
        }

        // Consultar empleados del cargo seleccionado
        ObservableList<Empleado> empleadosCargo = FXCollections.observableArrayList();
        String sql = "SELECT e.ID_Empleado, e.DocumentoIdentidad, e.Nombre, e.Apellido, e.idCargo, c.nombreCargo " +
                "FROM Empleados e JOIN Cargos c ON e.idCargo = c.idCargo WHERE e.idCargo = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCargo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Empleado emp = new Empleado(
                            rs.getInt("ID_Empleado"),
                            rs.getString("DocumentoIdentidad"),
                            rs.getString("Nombre"),
                            rs.getString("Apellido"),
                            rs.getInt("idCargo"),
                            rs.getString("nombreCargo")
                    );
                    empleadosCargo.add(emp);
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo consultar empleados: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        if (empleadosCargo.isEmpty()) {
            mostrarAlerta("Reporte PDF", "No hay empleados para el cargo seleccionado.", Alert.AlertType.INFORMATION);
            return;
        }

        // Guardar en la raíz del proyecto
        String fileName = "ReporteEmpleados_" + cargoSeleccionado.replaceAll("\\s+", "_") + ".pdf";
        File pdfFile = new File(fileName);

        // Generar el PDF
        try (OutputStream out = new FileOutputStream(pdfFile)) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Paragraph title = new Paragraph("Reporte de Empleados por Cargo", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Cargo: " + cargoSeleccionado, FontFactory.getFont(FontFactory.HELVETICA, 14)));
            document.add(new Paragraph("Fecha: " + java.time.LocalDate.now()));
            document.add(new Paragraph(" ")); // espacio

            // Tabla
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 3f, 3f, 3f});
            table.addCell("Documento");
            table.addCell("Nombre");
            table.addCell("Apellido");
            table.addCell("ID Empleado");

            for (Empleado emp : empleadosCargo) {
                table.addCell(emp.getDocumento());
                table.addCell(emp.getNombre());
                table.addCell(emp.getApellido());
                table.addCell(String.valueOf(emp.getIdEmpleado()));
            }
            document.add(table);

            document.close();
            mostrarAlerta("Éxito", "Reporte PDF generado correctamente en la raíz del proyecto.", Alert.AlertType.INFORMATION);

            // Abrir el PDF generado
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo generar o abrir el PDF: " + ex.getMessage(), Alert.AlertType.ERROR);
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