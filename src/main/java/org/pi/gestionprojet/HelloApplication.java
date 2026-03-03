package org.pi.gestionprojet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("UI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        scene.getStylesheets().add(HelloApplication.class.getResource("app.css").toExternalForm());
        stage.setTitle("Plateforme de financement artistique");
        stage.setScene(scene);
        stage.show();
    }
}
