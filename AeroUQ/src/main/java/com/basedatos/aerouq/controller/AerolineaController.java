package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.Aerolinea;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Desktop;

public class AerolineaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtFlota;
    @FXML private TextField txtContacto;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnReporte;

    @FXML private TableView<Aerolinea> tableAerolineas;
    @FXML private TableColumn<Aerolinea, String> colIdAerolínea;
    @FXML private TableColumn<Aerolinea, String> colNombre;
    @FXML private TableColumn<Aerolinea, String> colFlota;
    @FXML private TableColumn<Aerolinea, String> colContacto;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<Aerolinea> data = FXCollections.observableArrayList();

    private Aerolinea aerolineaSeleccionada = null;

    @FXML
    private void initialize() {
        colIdAerolínea.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAerolinea())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colFlota.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getFlota())));
        colContacto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContacto()));

        cargarTabla();

        tableAerolineas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        aerolineaSeleccionada = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("Aerolineas");
            for (Map<String, Object> fila : resultados) {
                Aerolinea a = new Aerolinea(
                        (int) fila.get("ID_Aerolinea"),
                        (String) fila.get("Nombre"),
                        (int) fila.get("Flota"),
                        (String) fila.get("Contacto")
                );
                data.add(a);
            }
            tableAerolineas.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las aerolíneas:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarAerolínea() {
        String buscar = txtBuscar.getText().trim();
        if (buscar.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el nombre o parte del nombre de la aerolínea a buscar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados = repository.buscar("Aerolineas", "Nombre LIKE ?", Collections.singletonList("%" + buscar + "%"));
            if (resultados.isEmpty()) {
                mostrarAlerta("Buscar", "No se encontró ninguna aerolínea.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> fila = resultados.get(0);
            Aerolinea a = new Aerolinea(
                    (int) fila.get("ID_Aerolinea"),
                    (String) fila.get("Nombre"),
                    (int) fila.get("Flota"),
                    (String) fila.get("Contacto")
            );
            aerolineaSeleccionada = a;
            tableAerolineas.getSelectionModel().select(buscarAerolineaEnTabla(a.getIdAerolinea()));
            llenarCamposDesdeSeleccion();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar aerolínea:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableAerolineas.getSelectionModel().clearSelection();
        cargarTabla();
        aerolineaSeleccionada = null;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtFlota.clear();
        txtContacto.clear();
    }

    private void llenarCamposDesdeSeleccion() {
        if (aerolineaSeleccionada != null) {
            txtNombre.setText(aerolineaSeleccionada.getNombre());
            txtFlota.setText(String.valueOf(aerolineaSeleccionada.getFlota()));
            txtContacto.setText(aerolineaSeleccionada.getContacto());
        }
    }

    @FXML
    private void handleAddAerolinea() {
        try {
            String nombre = txtNombre.getText().trim();
            int flota = Integer.parseInt(txtFlota.getText().trim());
            String contacto = txtContacto.getText().trim();

            Map<String, Object> datos = new HashMap<>();
            datos.put("Nombre", nombre);
            datos.put("Flota", flota);
            datos.put("Contacto", contacto);

            int filas = repository.insertar("Aerolineas", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aerolínea agregada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aerolineaSeleccionada = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "El campo Flota debe ser un número válido.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar aerolínea:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditAerolinea() {
        if (aerolineaSeleccionada == null) {
            mostrarAlerta("Editar", "Seleccione una aerolínea para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            String nombre = txtNombre.getText().trim();
            int flota = Integer.parseInt(txtFlota.getText().trim());
            String contacto = txtContacto.getText().trim();

            Map<String, Object> datos = new HashMap<>();
            datos.put("Nombre", nombre);
            datos.put("Flota", flota);
            datos.put("Contacto", contacto);

            int filas = repository.actualizar(
                    "Aerolineas",
                    datos,
                    "ID_Aerolinea = ?",
                    Collections.singletonList(aerolineaSeleccionada.getIdAerolinea())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aerolínea actualizada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aerolineaSeleccionada = null;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "El campo Flota debe ser un número válido.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar aerolínea:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteAerolinea() {
        if (aerolineaSeleccionada == null) {
            mostrarAlerta("Eliminar", "Seleccione una aerolínea para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "Aerolineas",
                    "ID_Aerolinea = ?",
                    Collections.singletonList(aerolineaSeleccionada.getIdAerolinea())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Aerolínea eliminada correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                aerolineaSeleccionada = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar aerolínea:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Aerolinea buscarAerolineaEnTabla(int idAerolinea) {
        for (Aerolinea a : data) {
            if (a.getIdAerolinea() == idAerolinea) return a;
        }
        return null;
    }

    // REPORTE PDF (con estado de mantenimiento "Excelente" si no existe en MantenimientoAeronaves)
    @FXML
    private void handleGenerarReporte() {
        String nombreBuscado = txtNombre.getText().trim();
        if (nombreBuscado.isEmpty()) {
            mostrarAlerta("Reporte", "Debe ingresar el nombre de la aerolínea en el campo Nombre.", Alert.AlertType.WARNING);
            return;
        }
        try {
            // Buscar Aerolínea
            List<Map<String, Object>> resAero = repository.buscar("Aerolineas", "Nombre = ?", Collections.singletonList(nombreBuscado));
            if (resAero.isEmpty()) {
                mostrarAlerta("Reporte", "No se encontró ninguna aerolínea con ese nombre.", Alert.AlertType.INFORMATION);
                return;
            }
            Map<String, Object> aeroFila = resAero.get(0);
            int idAerolinea = (int) aeroFila.get("ID_Aerolinea");
            String nombre = (String) aeroFila.get("Nombre");
            int flota = (int) aeroFila.get("Flota");
            String contacto = (String) aeroFila.get("Contacto");

            // Buscar aeronaves de la aerolínea
            List<Map<String, Object>> aeronaves = repository.buscar("Aeronaves", "ID_Aerolinea = ?", Collections.singletonList(idAerolinea));

            // Guardar estados de mantenimiento por aeronave (ID_Aeronave -> Estado), tabla: MantenimientoAeronaves
            Map<Integer, String> estadoMantenimiento = new HashMap<>();
            List<Map<String, Object>> mantenimientos = repository.buscar("MantenimientoAeronaves");
            for (Map<String, Object> mant : mantenimientos) {
                Object idAeronaveObj = mant.get("ID_Aeronave");
                Object estadoObj = mant.get("Estado");
                if (idAeronaveObj != null && estadoObj != null) {
                    estadoMantenimiento.put(
                            ((Number) idAeronaveObj).intValue(),
                            estadoObj.toString()
                    );
                }
            }

            // Generar PDF
            Document document = new Document();
            String filename = "Reporte_Aerolinea_" + nombre.replaceAll(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            document.add(new Paragraph("Reporte de Aerolínea", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Nombre: " + nombre, FontFactory.getFont(FontFactory.HELVETICA, 14)));
            document.add(new Paragraph("Flota: " + flota, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph("Contacto: " + contacto, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Aeronaves:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("ID Aeronave");
            table.addCell("Modelo");
            table.addCell("Matrícula");
            table.addCell("Estado");

            for (Map<String, Object> a : aeronaves) {
                int idAeronave = ((Number)a.get("ID_Aeronave")).intValue();
                table.addCell(String.valueOf(idAeronave));
                table.addCell(a.get("Modelo") != null ? a.get("Modelo").toString() : "");
                table.addCell(a.get("Matricula") != null ? a.get("Matricula").toString() : "");
                // Estado: de MantenimientoAeronaves o "Excelente"
                String estado = estadoMantenimiento.getOrDefault(idAeronave, "Excelente");
                table.addCell(estado);
            }
            document.add(table);
            document.close();

            // Abrir PDF automáticamente
            File pdfFile = new File(filename);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
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