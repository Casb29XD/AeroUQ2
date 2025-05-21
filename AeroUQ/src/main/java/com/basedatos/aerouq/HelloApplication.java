package com.basedatos.aerouq;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el archivo FXML del dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basedatos/aerouq/login.fxml"));
            Parent root = loader.load();

            // Configurar la escena principal
            Scene scene = new Scene(root);

            // Configurar el título y mostrar la ventana principal
            primaryStage.setTitle("AerUQ Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Maximizar la ventana al inicio
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el archivo dashboard.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args); // Iniciar la aplicación JavaFX
    }
}