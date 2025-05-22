package com.basedatos.aerouq.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class DashboardController {

    @FXML
    private AnchorPane contentArea;

    // Método general para cargar vistas
    private void loadView(String fxmlPath) {
        try {
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

    @FXML
    private void loadEmpleadosView() {
        loadView("com/basedatos/aerouq/empleados.fxml");
    }

    @FXML
    private void loadAerolineaView() {
        loadView("com/basedatos/aerouq/aerolinea.fxml");
    }

    @FXML
    private void loadAeronaveView() {
        loadView("com/basedatos/aerouq/aeronave.fxml");
    }

    @FXML
    private void loadUsuariosView() {
        loadView("com/basedatos/aerouq/usuarios.fxml");
    }

    @FXML
    private void loadPuertasView() {
        loadView("com/basedatos/aerouq/embarque.fxml");
    }

    @FXML
    private void loadVuelosView() {
        loadView("com/basedatos/aerouq/vuelos.fxml");
    }

    @FXML
    private void loadPasajerosView() {
        loadView("com/basedatos/aerouq/pasajeros.fxml");
    }

    @FXML
    private void loadEquipajesView() {
        loadView("com/basedatos/aerouq/equipaje.fxml");
    }

    @FXML
    private void loadCargaLogisticaView() {
        loadView("com/basedatos/aerouq/carga.fxml");
    }

    @FXML
    private void loadCargosView() {
        loadView("com/basedatos/aerouq/cargo.fxml");
    }

    @FXML
    private void loadMatenimientosView() {
        loadView("com/basedatos/aerouq/mantenimiento.fxml");
    }

    @FXML
    private void cerrarAplicacion() {
        System.exit(0);
    }

    // Puedes cargar una vista por defecto al iniciar
    @FXML
    private void initialize() {
        loadVuelosView();  // o cualquier otra vista inicial
    }
}
