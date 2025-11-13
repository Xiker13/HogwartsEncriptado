package org.hogwartsencriptado;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Clase principal de la aplicación Hogwarts Encriptado.
 * <p>
 * Carga la interfaz definida en {@code MainView.fxml} y muestra la ventana principal.
 * Soporta internacionalización mediante archivos de propiedades en la carpeta {@code i18n}.
 * </p>
 * <p>
 * Autor: Xiker
 * </p>
 */
public class App extends Application {

    /**
     * Metodo de inicio de la aplicación JavaFX.
     * <p>
     * Configura el {@code Stage} principal, carga el FXML con soporte de traducción,
     * inicializa el controlador y establece el icono de la aplicación.
     * </p>
     *
     * @param stage Stage principal de JavaFX
     * @throws Exception si ocurre un error al cargar el FXML o los recursos
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Selección de idioma por defecto (Español)
        Locale locale = new Locale("es");
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

        // Cargar FXML con ResourceBundle
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundle);
        Parent root = fxmlLoader.load();

        // Obtener controlador y pasarle el Stage
        MainController controller = fxmlLoader.getController();
        controller.setStage(stage);
        controller.setApp(this);

        // Cargar icono de la aplicación
        Image icon = new Image(getClass().getResource("/imagenes/hogwarts_escudo.png").toExternalForm());
        stage.getIcons().add(icon);

        // Configurar título de la ventana
        stage.setTitle(bundle.getString("app.title"));

        // Crear escena
        Scene scene = new Scene(root, 900, 580);
        stage.setScene(scene);

        // Tamaños mínimos
        stage.setMinWidth(800);
        stage.setMinHeight(580);

        // Mostrar ventana
        stage.show();
    }

    /**
     * Metodo principal de la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch();
    }
}
