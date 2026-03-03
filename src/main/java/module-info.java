module org.pi.gestionprojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.net.http;
    requires java.mail;

    opens org.pi.gestionprojet to javafx.fxml;
    opens org.pi.gestionprojet.controllers to javafx.fxml;
    opens org.pi.gestionprojet.entities to javafx.base;
    exports org.pi.gestionprojet;
}