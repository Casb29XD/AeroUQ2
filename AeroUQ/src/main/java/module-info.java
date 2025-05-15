module com.basedatos.aerouq {
    // JavaFX módulos requeridos
    requires javafx.controls;
    requires javafx.fxml;

    // Dependencias adicionales
    requires org.controlsfx.controls; // Para controles avanzados de JavaFX
    requires com.dlsc.formsfx;        // Para formularios JavaFX
    requires org.kordamp.bootstrapfx.core; // Para estilos Bootstrap en JavaFX
    requires java.sql;               // Para conexiones a bases de datos

    // Abre paquetes para ser accesibles por el cargador FXML
    opens com.basedatos.aerouq to javafx.fxml;
    opens com.basedatos.aerouq.controller to javafx.fxml;

    // Exporta el paquete base para otros módulos
    exports com.basedatos.aerouq;
}