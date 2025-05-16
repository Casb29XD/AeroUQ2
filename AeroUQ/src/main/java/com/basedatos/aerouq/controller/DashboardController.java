package com.basedatos.aerouq.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox menuBoxCollapsed;

    @FXML
    private VBox menuBoxExpanded;

    @FXML
    private Button btnLockMenu;
    @FXML
    private Button btnLockMenuExpanded;

    private boolean menuLocked = false;

    /**
     * Carga una vista FXML dentro del contentArea.
     */
    private void loadView(String fxmlPath) {
        try {
            // Usar getResource con slash inicial para seguridad en classpath
            URL location = getClass().getResource("/" + fxmlPath);
            if (location == null) {
                System.err.println("No se encontró el archivo FXML: /" + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Node view = loader.load();

            contentArea.getChildren().setAll(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos para cargar las vistas. Usa siempre "/" como separador de carpetas.
    @FXML
    private void loadVuelosView() {
        loadView("com/basedatos/aerouq/vuelos.fxml");
    }

    @FXML
    private void loadPasajerosView() {
        loadView("com/basedatos/aerouq/pasajeros.fxml");
    }

    @FXML
    private void loadEmbarqueView() {
        loadView("com/basedatos/aerouq/embarque.fxml");
    }

    @FXML
    private void loadEquipajeView() {
        loadView("com/basedatos/aerouq/equipaje.fxml");
    }

    @FXML
    private void loadUsuariosView() {
        loadView("com/basedatos/aerouq/usuarios.fxml");
    }

    @FXML
    private void loadCargaView() {
        loadView("com/basedatos/aerouq/carga.fxml");
    }

    @FXML
    private void loadEmpleadosView() {
        loadView("com/basedatos/aerouq/empleados.fxml");
    }

    @FXML
    private void loadAerolineaView() {
        loadView("com/basedatos/aerouq/aerolinea.fxml");
    }

    @FXML
    private void loadMantenimientoView() {
        loadView("com/basedatos/aerouq/mantenimiento.fxml");
    }

    @FXML
    private void loadCargoView() {
        loadView("com/basedatos/aerouq/cargo.fxml");
    }

    /**
     * Alterna entre menú colapsado y expandido, si no está bloqueado.
     */
    @FXML
    private void toggleMenuLock() {
        menuLocked = !menuLocked;

        if (menuLocked) {
            menuBoxCollapsed.setVisible(false);
            menuBoxExpanded.setVisible(true);
        } else {
            menuBoxCollapsed.setVisible(true);
            menuBoxExpanded.setVisible(false);
        }

        btnLockMenu.setText(menuLocked ? "🔒" : "🔓");
        btnLockMenuExpanded.setText(menuLocked ? "🔒" : "🔓");
    }

    /**
     * Inicializa el controlador.
     */
    @FXML
    private void initialize() {
        menuBoxCollapsed.setVisible(true);
        menuBoxExpanded.setVisible(false);
        menuLocked = false;
        btnLockMenu.setText("🔓");
        btnLockMenuExpanded.setText("🔓");

        // Carga vista inicial
        loadVuelosView();

        // (Opcional) Prueba de carga FXML "cargo.fxml"
        // testFXMLLoad();
    }

    // Método opcional para depuración, verifica si cargo.fxml existe en el classpath
    private void testFXMLLoad() {
        URL url = getClass().getResource("/com/basedatos/aerouq/cargo.fxml");
        if (url == null) {
            System.out.println("NO ENCONTRADO");
        } else {
            System.out.println("ENCONTRADO: " + url);
        }
    }
}