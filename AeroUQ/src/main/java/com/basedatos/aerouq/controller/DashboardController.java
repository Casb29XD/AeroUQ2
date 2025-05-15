package com.basedatos.aerouq.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblCurrentDateTime;
    @FXML private Label lblUsername;

    @FXML private VBox menuBoxCollapsed;
    @FXML private VBox menuBoxExpanded;
    @FXML private AnchorPane contentArea;

    @FXML private Button btnLockMenu;
    @FXML private Button btnLockMenuExpanded;

    // Botones de menú colapsado
    @FXML private Button btnVuelosCollapsed;
    @FXML private Button btnPasajerosCollapsed;
    @FXML private Button btnPuertasEmbarqueCollapsed;
    @FXML private Button btnEquipajeCollapsed;
    @FXML private Button btnUsuariosCollapsed;
    @FXML private Button btnCargaLogisticaCollapsed;
    @FXML private Button btnEmpleadosCollapsed;
    @FXML private Button btnAerolineasCollapsed;
    @FXML private Button btnMantenimientoCollapsed;
    @FXML private Button btnCargoCollapsed;

    // Botones de menú expandido
    @FXML private Button btnVuelos;
    @FXML private Button btnPasajeros;
    @FXML private Button btnPuertasEmbarque;
    @FXML private Button btnEquipaje;
    @FXML private Button btnUsuarios;
    @FXML private Button btnCargaLogistica;
    @FXML private Button btnEmpleados;
    @FXML private Button btnAerolineas;
    @FXML private Button btnMantenimiento;
    @FXML private Button btnCargo;

    private boolean menuLocked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initClock();
        lblUsername.setText("Administrador");
        setupMenuHover();
        setupMenuButtons();
        loadPage("vuelos"); // Página por defecto
    }

    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            lblCurrentDateTime.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void setupMenuHover() {
        menuBoxCollapsed.setOnMouseEntered(e -> {
            if (!menuLocked) {
                menuBoxCollapsed.setVisible(false);
                menuBoxExpanded.setVisible(true);
            }
        });

        menuBoxExpanded.setOnMouseExited(e -> {
            if (!menuLocked) {
                menuBoxExpanded.setVisible(false);
                menuBoxCollapsed.setVisible(true);
            }
        });
    }

    @FXML
    private void toggleMenuLock() {
        menuLocked = !menuLocked;
    }

    private void setupMenuButtons() {
        // Colapsado
        btnVuelosCollapsed.setOnAction(e -> loadPage("vuelos"));
        btnPasajerosCollapsed.setOnAction(e -> loadPage("pasajeros"));
        btnPuertasEmbarqueCollapsed.setOnAction(e -> loadPage("embargue"));
        btnEquipajeCollapsed.setOnAction(e -> loadPage("equipaje"));
        btnUsuariosCollapsed.setOnAction(e -> loadPage("usuarios"));
        btnCargaLogisticaCollapsed.setOnAction(e -> loadPage("carga"));
        btnEmpleadosCollapsed.setOnAction(e -> loadPage("empleado"));
        btnAerolineasCollapsed.setOnAction(e -> loadPage("aerolinea"));
        btnMantenimientoCollapsed.setOnAction(e -> loadPage("mantenimiento"));
        btnCargoCollapsed.setOnAction(e -> loadPage("cargo"));

        // Expandido
        btnVuelos.setOnAction(e -> loadPage("vuelos"));
        btnPasajeros.setOnAction(e -> loadPage("pasajeros"));
        btnPuertasEmbarque.setOnAction(e -> loadPage("embargue"));
        btnEquipaje.setOnAction(e -> loadPage("equipaje"));
        btnUsuarios.setOnAction(e -> loadPage("usuarios"));
        btnCargaLogistica.setOnAction(e -> loadPage("carga"));
        btnEmpleados.setOnAction(e -> loadPage("empleado"));
        btnAerolineas.setOnAction(e -> loadPage("aerolinea"));
        btnMantenimiento.setOnAction(e -> loadPage("mantenimiento"));
        btnCargo.setOnAction(e -> loadPage("cargo"));
    }

    private void loadPage(String pageName) {
        try {
            URL fxmlLocation = getClass().getResource("/com/basedatos/aerouq/view/" + pageName + ".fxml");
            if (fxmlLocation != null) {
                Parent root = FXMLLoader.load(fxmlLocation);
                contentArea.getChildren().setAll(root);
            } else {
                System.err.println("FXML no encontrado para: " + pageName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
