package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.UsuarioDelSistema;
import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;

public class UsuarioController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private ComboBox<String> comboRol;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtContraseña;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarBusqueda;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    @FXML private TableView<UsuarioDelSistema> tableUsuarios;
    @FXML private TableColumn<UsuarioDelSistema, String> colIdUsuario;
    @FXML private TableColumn<UsuarioDelSistema, String> colNombre;
    @FXML private TableColumn<UsuarioDelSistema, String> colApellido;
    @FXML private TableColumn<UsuarioDelSistema, String> colUsuario;
    @FXML private TableColumn<UsuarioDelSistema, String> colRol;
    @FXML private TableColumn<UsuarioDelSistema, String> colContraseña;

    private final DatabaseRepository repository = new DatabaseRepository();
    private ObservableList<UsuarioDelSistema> data = FXCollections.observableArrayList();
    private UsuarioDelSistema usuarioSeleccionado = null;

    private static final List<String> ROLES = Arrays.asList("Administrador", "Aerolínea", "Cliente");

    @FXML
    private void initialize() {
        comboRol.setItems(FXCollections.observableArrayList(ROLES));

        colIdUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdUsuario())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario()));
        colRol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRol()));
        colContraseña.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContraseña()));

        cargarTabla();

        tableUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        usuarioSeleccionado = newSelection;
                        llenarCamposDesdeSeleccion();
                    }
                }
        );
    }

    private void cargarTabla() {
        data.clear();
        try {
            List<Map<String, Object>> resultados = repository.buscar("UsuariosDelSistema");
            for (Map<String, Object> fila : resultados) {
                UsuarioDelSistema usuario = new UsuarioDelSistema(
                        ((Number) fila.get("ID_Usuario")).intValue(),
                        (String) fila.get("Nombre"),
                        (String) fila.get("Apellido"),
                        (String) fila.get("Usuario"),
                        (String) fila.get("Contraseña"),
                        (String) fila.get("Rol")
                );
                data.add(usuario);
            }
            tableUsuarios.setItems(data);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los usuarios:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarUsuario() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            mostrarAlerta("Buscar", "Ingrese el ID o nombre de usuario.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            List<Map<String, Object>> resultados;
            if (texto.matches("\\d+")) {
                resultados = repository.buscar(
                        "UsuariosDelSistema",
                        "ID_Usuario = ?",
                        Collections.singletonList(Integer.valueOf(texto))
                );
            } else {
                resultados = repository.buscar(
                        "UsuariosDelSistema",
                        "Nombre LIKE ?",
                        Collections.singletonList("%" + texto + "%")
                );
            }
            data.clear();
            for (Map<String, Object> fila : resultados) {
                UsuarioDelSistema usuario = new UsuarioDelSistema(
                        ((Number) fila.get("ID_Usuario")).intValue(),
                        (String) fila.get("Nombre"),
                        (String) fila.get("Apellido"),
                        (String) fila.get("Usuario"),
                        (String) fila.get("Contraseña"),
                        (String) fila.get("Rol")
                );
                data.add(usuario);
            }
            tableUsuarios.setItems(data);

            if (!data.isEmpty()) {
                usuarioSeleccionado = data.get(0);
                tableUsuarios.getSelectionModel().select(0);
                llenarCamposDesdeSeleccion();
            } else {
                limpiarCampos();
                mostrarAlerta("Buscar", "No se encontraron usuarios con ese criterio.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar usuario:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        limpiarCampos();
        tableUsuarios.getSelectionModel().clearSelection();
        cargarTabla();
        usuarioSeleccionado = null;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtUsuario.clear();
        txtContraseña.clear();
        comboRol.getSelectionModel().clearSelection();
    }

    private void llenarCamposDesdeSeleccion() {
        if (usuarioSeleccionado != null) {
            txtNombre.setText(usuarioSeleccionado.getNombre());
            txtApellido.setText(usuarioSeleccionado.getApellido());
            txtUsuario.setText(usuarioSeleccionado.getUsuario());
            txtContraseña.setText(usuarioSeleccionado.getContraseña());
            comboRol.setValue(usuarioSeleccionado.getRol());
        }
    }

    @FXML
    private void handleAddUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contraseña = txtContraseña.getText().trim();
        String rol = comboRol.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || contraseña.isEmpty() || rol == null) {
            mostrarAlerta("Agregar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("Nombre", nombre);
        datos.put("Apellido", apellido);
        datos.put("Usuario", usuario);
        datos.put("Contraseña", contraseña); // Recuerda, deberías encriptar antes de guardar.
        datos.put("Rol", rol);

        try {
            int filas = repository.insertar("UsuariosDelSistema", datos);
            if (filas > 0) {
                mostrarAlerta("Éxito", "Usuario agregado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                usuarioSeleccionado = null;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                mostrarAlerta("Error", "El nombre de usuario ya existe.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al agregar usuario:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleEditUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Editar", "Seleccione un usuario para editar.", Alert.AlertType.INFORMATION);
            return;
        }
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contraseña = txtContraseña.getText().trim();
        String rol = comboRol.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || contraseña.isEmpty() || rol == null) {
            mostrarAlerta("Editar", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("Nombre", nombre);
        datos.put("Apellido", apellido);
        datos.put("Usuario", usuario);
        datos.put("Contraseña", contraseña); // Recuerda, deberías encriptar antes de guardar.
        datos.put("Rol", rol);

        try {
            int filas = repository.actualizar(
                    "UsuariosDelSistema",
                    datos,
                    "ID_Usuario = ?",
                    Collections.singletonList(usuarioSeleccionado.getIdUsuario())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                usuarioSeleccionado = null;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                mostrarAlerta("Error", "El nombre de usuario ya existe.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al actualizar usuario:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDeleteUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Eliminar", "Seleccione un usuario para eliminar.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            int filas = repository.eliminar(
                    "UsuariosDelSistema",
                    "ID_Usuario = ?",
                    Collections.singletonList(usuarioSeleccionado.getIdUsuario())
            );
            if (filas > 0) {
                mostrarAlerta("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);
                cargarTabla();
                limpiarCampos();
                usuarioSeleccionado = null;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar usuario:\n" + e.getMessage(), Alert.AlertType.ERROR);
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