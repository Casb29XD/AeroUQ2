package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class EmbargueController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtNoPuerta;
    @FXML private TextField txtEstado;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<?> tableEmbargue;
    @FXML private TableColumn<?, ?> colIdPuerta;
    @FXML private TableColumn<?, ?> colNoPuerta;
    @FXML private TableColumn<?, ?> colEstado;

    @FXML
    private void handleBuscarPuerta() {
        // Lógica para buscar puerta de embarque
        System.out.println("Buscar puerta: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddPuerta() {
        // Lógica para agregar puerta
        System.out.println("Agregar puerta: " + txtNoPuerta.getText() + " Estado: " + txtEstado.getText());
    }

    @FXML
    private void handleEditPuerta() {
        // Lógica para editar puerta seleccionada
        System.out.println("Editar puerta");
    }

    @FXML
    private void handleDeletePuerta() {
        // Lógica para eliminar puerta seleccionada
        System.out.println("Eliminar puerta");
    }

    @FXML
    private void initialize() {
        // Inicialización si necesitas cargar datos, configurar columnas, etc.
        System.out.println("EmbargueController inicializado");
    }
}