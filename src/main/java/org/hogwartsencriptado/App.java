package org.hogwartsencriptado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Se encarga de inicializar JavaFX, cargar la interfaz principal definida en {@code MainView.fxml}
 * y configurar los recursos de internacionalización (i18n) según el idioma.
 * </p>
 * <p>
 * También inicializa el controlador principal y establece el icono, título y tamaño de la ventana.
 * </p>
 * <p>
 * Autor: Xiker
 * </p>
 */
public class App extends Application {

    /** Logger de la clase (usado para registrar eventos de inicio y errores) */
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /**
     * Método principal de inicio de la aplicación JavaFX.
     * <p>
     * Carga el archivo FXML, asigna el controlador, configura idioma, icono y ventana principal.
     * </p>
     *
     * @param stage Stage principal de JavaFX
     * @throws Exception si ocurre un error al cargar el FXML o los recursos
     */
    @Override
    public void start(Stage stage) throws Exception {
        log.info("Iniciando aplicación Hogwarts Encriptado...");

        try {
            // Selección de idioma por defecto (Español)
            Locale locale = new Locale("es");
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
            log.debug("Idioma por defecto establecido: {}", locale.getLanguage());

            // Cargar el FXML junto con el ResourceBundle (traducciones)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundle);
            Parent root = fxmlLoader.load();
            log.info("Archivo FXML principal cargado correctamente.");

            // Obtener el controlador y pasarle el Stage y la App
            MainController controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.setApp(this);
            log.debug("Controlador principal inicializado y vinculado al Stage.");

            // Cargar icono de la aplicación
            Image icon = new Image(getClass().getResource("/imagenes/hogwarts_escudo.png").toExternalForm());
            stage.getIcons().add(icon);
            log.debug("Icono de la aplicación establecido correctamente.");

            // Configurar título de la ventana
            stage.setTitle(bundle.getString("app.title"));
            log.info("Título de la ventana configurado: {}", bundle.getString("app.title"));

            // Crear la escena y asignarla al Stage
            Scene scene = new Scene(root, 900, 580);
            stage.setScene(scene);
            log.debug("Escena principal creada (900x580).");

            // Tamaños mínimos de la ventana
            stage.setMinWidth(800);
            stage.setMinHeight(580);
            log.trace("Tamaños mínimos definidos: 800x580.");

            // Mostrar ventana principal
            stage.show();
            log.info("Ventana principal mostrada correctamente.");

        } catch (Exception e) {
            // Captura general de errores en la inicialización
            log.error("Error al iniciar la aplicación: {}", e.toString(), e);
            throw e; // Se relanza para que JavaFX gestione el fallo
        }
    }

    /**
     * Método main de la aplicación.
     * <p>
     * Lanza el entorno JavaFX y delega en el método {@link #start(Stage)}.
     * </p>
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        log.info("Ejecutando método main(): lanzamiento de JavaFX.");
        launch();  // Llama internamente a start(Stage)
    }
}

