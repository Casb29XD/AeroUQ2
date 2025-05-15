module com.basedatos.aerouq {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.basedatos.aerouq to javafx.fxml;
    exports com.basedatos.aerouq;
}