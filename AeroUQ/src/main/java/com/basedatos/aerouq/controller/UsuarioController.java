package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class UsuarioController {

    // Campos de búsqueda y formulario
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtRol;
    @FXML private TextField txtContraseña;

    // Botones de acción
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // Tabla y columnas
    @FXML private TableView<?> tableUsuarios;
    @FXML private TableColumn<?, ?> colIdUsuario;
    @FXML private TableColumn<?, ?> colNombre;
    @FXML private TableColumn<?, ?> colApellido;
    @FXML private TableColumn<?, ?> colRol;
    @FXML private TableColumn<?, ?> colContraseña;

    @FXML
    private void handleBuscarUsuario() {
        System.out.println("Buscar usuario: " + txtBuscar.getText());
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        System.out.println("Limpieza de búsqueda");
    }

    @FXML
    private void handleAddUsuario() {
        System.out.println("Agregar usuario: " +
                "Nombre: " + txtNombre.getText() + ", " +
                "Apellido: " + txtApellido.getText() + ", " +
                "Rol: " + txtRol.getText() + ", " +
                "Contraseña: " + txtContraseña.getText());
    }

    @FXML
    private void handleEditUsuario() {
        System.out.println("Editar usuario seleccionado");
    }

    @FXML
    private void handleDeleteUsuario() {
        System.out.println("Eliminar usuario seleccionado");
    }

    @FXML
    private void initialize() {
        // Inicialización adicional si lo necesitas
        System.out.println("UsuarioController inicializado");
    }
}