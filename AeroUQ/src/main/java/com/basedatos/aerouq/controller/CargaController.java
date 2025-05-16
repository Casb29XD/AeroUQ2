package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CargaController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtIdAerolinea;
    @FXML private TextField txtPeso;
    @FXML private TextField txtTipoCarga;
    @FXML private TextField txtEstado;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tableCargas;
    @FXML private TableColumn<?, ?> colIdCarga;
    @FXML private TableColumn<?, ?> colNIdAerolinea;
    @FXML private TableColumn<?, ?> colPeso;
    @FXML private TableColumn<?, ?> colTipoCarga;
    @FXML private TableColumn<?, ?> colEstado;

    @FXML
    private void handleBuscarCarga() {
        System.out.println("Buscar carga: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddCarga() {
        System.out.println("Agregar carga: " +
                "ID Aerolínea: " + txtIdAerolinea.getText() + ", " +
                "Peso: " + txtPeso.getText() + ", " +
                "Tipo: " + txtTipoCarga.getText() + ", " +
                "Estado: " + txtEstado.getText());
    }

    @FXML
    private void handleEditCarga() {
        System.out.println("Editar carga seleccionada");
    }

    @FXML
    private void handleDeleteCarga() {
        System.out.println("Eliminar carga seleccionada");
    }

    @FXML
    private void initialize() {
        // Inicialización adicional si lo necesitas
        System.out.println("CargaController inicializado");
    }
}