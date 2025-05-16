package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PasajerosController {

    // Campos del formulario
    @FXML private TextField txtIdPasajero;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumentoIdentidad;
    @FXML private TextField txtNacionalidad;
    @FXML private TextField txtIdVuelo;

    // Botones de acción
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tablePasajeros;
    @FXML private TableColumn<?, ?> colIdPasajero;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colApellido;
    @FXML private TableColumn<?, ?> colDocumentoIdentidad;
    @FXML private TableColumn<?, ?> colNacionalidad;
    @FXML private TableColumn<?, ?> colIdVuelo;

    @FXML
    private void handleAddPasajero() {
        System.out.println("Agregar pasajero: " +
                "ID: " + txtIdPasajero.getText() + ", " +
                "Nombre: " + txtNombre.getText() + ", " +
                "Apellido: " + txtApellido.getText() + ", " +
                "Documento: " + txtDocumentoIdentidad.getText() + ", " +
                "Nacionalidad: " + txtNacionalidad.getText() + ", " +
                "ID Vuelo: " + txtIdVuelo.getText()
        );
    }

    @FXML
    private void handleEditPasajero() {
        System.out.println("Editar pasajero seleccionado");
    }

    @FXML
    private void handleDeletePasajero() {
        System.out.println("Eliminar pasajero seleccionado");
    }

    @FXML
    private void initialize() {
        // Aquí puedes inicializar la tabla o cargar datos si lo necesitas
        System.out.println("PasajerosController inicializado");
    }
}