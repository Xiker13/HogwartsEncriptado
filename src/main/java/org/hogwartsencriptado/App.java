package org.hogwartsencriptado;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación AES.
 * Carga la interfaz definida en MainView.fxml y muestra la ventana principal.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 750);
        stage.setTitle("Cifrado AES");
        stage.setScene(scene);

        // Tamaños mínimos para evitar que se aplaste
        stage.setMinWidth(900);
        stage.setMinHeight(780);

        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
