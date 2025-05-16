package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AerolineaController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtIdAerolinea;
    @FXML private TextField txtNombre;
    @FXML private TextField txtFlota;
    @FXML private TextField txtContacto;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tableAerolineas;
    @FXML private TableColumn<?, ?> colIdAerolínea;
    @FXML private TableColumn<?, ?> colFlota;
    @FXML private TableColumn<?, ?> colIdVuelo;
    @FXML private TableColumn<?, ?> colContacto;

    @FXML
    private void handleBuscarAerolínea() {
        System.out.println("Buscar aerolínea: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddAerolinea() {
        System.out.println("Agregar aerolínea: " + txtNombre.getText() + ", Flota: " + txtFlota.getText() + ", Contacto: " + txtContacto.getText());
    }

    @FXML
    private void handleEditAerolinea() {
        System.out.println("Editar aerolínea seleccionada");
    }

    @FXML
    private void handleDeleteAerolinea() {
        System.out.println("Eliminar aerolínea seleccionada");
    }

    @FXML
    private void initialize() {
        // Inicialización adicional si lo necesitas
        System.out.println("AerolineaController inicializado");
    }
}