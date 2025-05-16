package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class EmpleadoController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCargo;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tableEmpleado;
    @FXML private TableColumn<?, ?> colIdEmpleado;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colApellido;
    @FXML private TableColumn<?, ?> colCargo;

    @FXML
    private void handleBuscarEmpleado() {
        System.out.println("Buscar empleado: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddEmpleado() {
        System.out.println("Agregar empleado: " +
                "Documento: " + txtDocumento.getText() + ", " +
                "Nombre: " + txtNombre.getText() + ", " +
                "Apellido: " + txtApellido.getText() + ", " +
                "Cargo: " + txtCargo.getText());
    }

    @FXML
    private void handleEditEmpleado() {
        System.out.println("Editar empleado seleccionado");
    }

    @FXML
    private void handleDeleteEmpleado() {
        System.out.println("Eliminar empleado seleccionado");
    }

    @FXML
    private void initialize() {
        // Inicialización adicional si lo necesitas
        System.out.println("EmpleadoController inicializado");
    }
}