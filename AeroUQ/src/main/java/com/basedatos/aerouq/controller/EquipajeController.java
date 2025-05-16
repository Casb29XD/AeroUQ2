package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class EquipajeController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPeso;
    @FXML private TextField txtIdVuelo;
    @FXML private TextField txtIdPasajero;
    @FXML private TextField txtEstado;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tableEquipaje;
    @FXML private TableColumn<?, ?> colIdMaleta;
    @FXML private TableColumn<?, ?> colCodBarras;
    @FXML private TableColumn<?, ?> colPeso;
    @FXML private TableColumn<?, ?> colIdVuelo;
    @FXML private TableColumn<?, ?> colIdPasajero;
    @FXML private TableColumn<?, ?> colEstado;

    @FXML
    private void handleBuscarEquipaje() {
        System.out.println("Buscar equipaje: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddEquipaje() {
        System.out.println("Agregar equipaje: " +
                "Código de Barras: " + txtCodBarras.getText() + ", " +
                "Peso: " + txtPeso.getText() + ", " +
                "ID Vuelo: " + txtIdVuelo.getText() + ", " +
                "ID Pasajero: " + txtIdPasajero.getText() + ", " +
                "Estado: " + txtEstado.getText());
    }

    @FXML
    private void handleEditEquipaje() {
        System.out.println("Editar equipaje seleccionado");
    }

    @FXML
    private void handleDeleteEquipaje() {
        System.out.println("Eliminar equipaje seleccionado");
    }

    @FXML
    private void initialize() {
        // Inicialización adicional si lo necesitas
        System.out.println("EquipajeController inicializado");
    }
}