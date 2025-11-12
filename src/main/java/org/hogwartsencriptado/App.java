package org.hogwartsencriptado;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación AES.
 * Carga la interfaz definida en MainView.fxml y muestra la ventana principal.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargar el FXML desde la carpeta fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 560);

        // Cargar el icono desde la carpeta imagenes
        Image icon = new Image(
                App.class.getResource("imagenes/hogwarts_escudo.png").toExternalForm()
        );
        stage.getIcons().add(icon);

        stage.setTitle("Hogwarts Encriptado");
        stage.setScene(scene);

        // Tamaños mínimos para evitar que se aplaste
        stage.setMinWidth(800);
        stage.setMinHeight(560);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}