package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AeronaveController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnLimpiarBusqueda;

    @FXML
    private TextField txtIdAerolinea;

    @FXML
    private TextField txtModelo;

    @FXML
    private TextField txtMatricula;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TableView<?> tableAeronave;

    @FXML
    private TableColumn<?, ?> colIdAeronave;

    @FXML
    private TableColumn<?, ?> colIdAerolinea;

    @FXML
    private TableColumn<?, ?> colModelo;

    @FXML
    private TableColumn<?, ?> colMatricula;

    // Constructor vacío es suficiente para JavaFX
    public AeronaveController() {
        // Aquí puedes inicializar variables si lo necesitas
    }

    // Métodos de acción para los botones
    @FXML
    private void handleBuscarAeronave() {
        // Lógica para buscar aeronave
    }

    @FXML
    private void handleLimpiarBusqueda() {
        // Lógica para limpiar búsqueda
    }

    @FXML
    private void handleAddAeronave() {
        // Lógica para agregar aeronave
    }

    @FXML
    private void handleEditAeronave() {
        // Lógica para editar aeronave
    }

    @FXML
    private void handleDeleteAeronave() {
        // Lógica para eliminar aeronave
    }
}