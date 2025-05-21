package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.repository.DatabaseRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblError;

    private final DatabaseRepository repository = new DatabaseRepository();

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsuario.getText();
        String pass = txtContrasena.getText();

        try {
            String tabla = "UsuariosDelSistema";
            String condicion = "Usuario = ?";
            List<Object> parametros = List.of(user);

            List<Map<String, Object>> resultados = repository.buscar(tabla, condicion, parametros);

            if (!resultados.isEmpty()) {
                Map<String, Object> usuario = resultados.get(0);
                String contrasenaGuardada = String.valueOf(usuario.get("Contraseña"));
                String rol = String.valueOf(usuario.get("Rol"));

                // Si la contraseña es correcta y el rol es válido
                if (pass.equals(contrasenaGuardada)) {
                    if (rol != null && (
                            rol.equalsIgnoreCase("Administrador") ||
                                    rol.equalsIgnoreCase("Aerolínea") ||
                                    rol.equalsIgnoreCase("Cliente"))) {
                        abrirDashboard(event, user, rol); // Puedes personalizar según el rol
                    } else {
                        mostrarError("Rol de usuario no válido");
                    }
                } else {
                    mostrarError("Credenciales incorrectas");
                }
            } else {
                mostrarError("Usuario no encontrado");
            }
        } catch (SQLException e) {
            mostrarError("Error de conexión a la base de datos");
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void abrirDashboard(ActionEvent event, String usuario, String rol) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basedatos/aerouq/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load());

            // Si quieres pasar el usuario y rol al DashboardController, puedes hacerlo así:
            // DashboardController controller = loader.getController();
            // controller.setUsuarioYRol(usuario, rol);

            Stage dashboardStage = new Stage();
            dashboardStage.setScene(dashboardScene);
            dashboardStage.setTitle("Dashboard - " + rol);
            dashboardStage.show();

            // Cerrar la ventana de login
            Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el dashboard");
        }
    }
}