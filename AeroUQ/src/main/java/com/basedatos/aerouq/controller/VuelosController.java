package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VuelosController {
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNumeroVuelo;
    @FXML private TextField txtIdAerolinea;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtDestino;
    @FXML private TextField txtFechaSalida;
    @FXML private TextField txtFechaLlegada;
    @FXML private TextField txtEstadoVuelo;
    @FXML private TextField txtIdPuerta;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<?> tableVuelos;
    @FXML private TableColumn<?, ?> colIdVuelo;
    @FXML private TableColumn<?, ?> colNumeroVuelo;
    @FXML private TableColumn<?, ?> colIdAerolinea;
    @FXML private TableColumn<?, ?> colOrigen;
    @FXML private TableColumn<?, ?> colDestino;
    @FXML private TableColumn<?, ?> colFechaSalida;
    @FXML private TableColumn<?, ?> colFechaLlegada;
    @FXML private TableColumn<?, ?> colEstadoVuelo;
    @FXML private TableColumn<?, ?> colIdPuerta;

    @FXML
    private void handleBuscarVuelo() {
        // Lógica para buscar vuelos (por ahora muestra el texto buscado)
        System.out.println("Buscar vuelo: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddVuelo() {
        // Aquí puedes obtener los datos y agregarlos
        System.out.println("Agregar vuelo: " + txtNumeroVuelo.getText());
    }

    @FXML
    private void handleEditVuelo() {
        System.out.println("Editar vuelo seleccionado");
    }

    @FXML
    private void handleDeleteVuelo() {
        System.out.println("Eliminar vuelo seleccionado");
    }

    @FXML
    private void initialize() {
        // Inicialización si necesitas cargar datos, configurar columnas, etc.
        System.out.println("VuelosController inicializado");
    }
}