package com.basedatos.aerouq.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private VBox menuBoxCollapsed;

    @FXML
    private VBox menuBoxExpanded;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private void toggleMenuLock() {
        boolean isCollapsedVisible = menuBoxCollapsed.isVisible();
        menuBoxCollapsed.setVisible(!isCollapsedVisible);
        menuBoxExpanded.setVisible(isCollapsedVisible);
    }

    @FXML
    private void loadAerolineaView() {
        loadView("com/basedatos/aerouq/aerolinea.fxml");
    }

    @FXML
    private void loadCargaView() {
        loadView("com/basedatos/aerouq/carga.fxml");
    }

    @FXML
    private void loadCargoView() {
        loadView("com/basedatos/aerouq/cargo.fxml");
    }

    @FXML
    private void loadEmbarqueView() {
        loadView("com/basedatos/aerouq/embarque.fxml");
    }

    @FXML
    private void loadEmpleadosView() {
        loadView("com/basedatos/aerouq/empleados.fxml");
    }

    @FXML
    private void loadEquipajeView() {
        loadView("com/basedatos/aerouq/equipaje.fxml");
    }

    @FXML
    private void loadMantenimientoView() {
        loadView("com/basedatos/aerouq/mantenimiento.fxml");
    }

    @FXML
    private void loadPasajerosView() {
        loadView("com/basedatos/aerouq/pasajeros.fxml");
    }

    @FXML
    private void loadUsuariosView() {
        loadView("com/basedatos/aerouq/usuarios.fxml");
    }

    @FXML
    private void loadVuelosView() {
        loadView("com/basedatos/aerouq/vuelos.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            URL location = getClass().getClassLoader().getResource(fxmlPath);
            if (location == null) {
                System.err.println("❌ No se encontró el archivo FXML: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Error al cargar la vista: " + fxmlPath);
        }
    }

}
