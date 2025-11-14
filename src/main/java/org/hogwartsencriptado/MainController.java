package org.hogwartsencriptado;

import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.stage.FileChooser.ExtensionFilter;
import java.nio.file.Files;

/**
 * Controlador principal de Hogwarts Encriptado.
 * <p>
 * Gestiona la interfaz de usuario, incluyendo:
 * <ul>
 *     <li>Carga de claves y textos desde archivos.</li>
 *     <li>Cifrado y descifrado de texto con AES o Vigenère.</li>
 *     <li>Guardado de resultados en archivos.</li>
 *     <li>Menús de ayuda, manual, acerca de y cierre de aplicación.</li>
 *     <li>Cambio de idioma dinámico mediante recarga de FXML y ResourceBundle.</li>
 * </ul>
 * </p>
 * <p>
 * Esta versión incluye trazas de logging mediante SLF4J + Logback para registrar
 * eventos importantes, advertencias y errores, tanto en consola como en ficheros.
 * </p>
 * <p>
 * Está vinculada a {@code MainView.fxml} y utiliza {@link PythonVigenereService}
 * para el cifrado/descifrado Vigenère en Python.
 * </p>
 *
 * @author Xiker, Salca (modifier)
 */
public class MainController {

    /**
     * Logger de la clase, para registrar eventos y errores
     */
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // === ELEMENTOS DE LA INTERFAZ (FXML) ===
    @FXML
    private TextArea claveTextField; // cambiado a TextArea para soportar multilinea
    @FXML
    private Button cargarClaveButton;
    @FXML
    private TextArea entradaTextArea;
    @FXML
    private Button cargarEntradaButton;
    @FXML
    private Button cifrarButton;
    @FXML
    private Button descifrarButton;
    @FXML
    private TextArea resultadoTextArea;
    @FXML
    private TextField nombreArchivoTextField;
    @FXML
    private Button guardarArchivoButton;
    @FXML
    private RadioButton aesRadioButton;
    @FXML
    private RadioButton vigenereRadioButton;

    // === MENÚS ===
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem manualMenuItem;
    @FXML
    private MenuItem ayudaMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    // === MENÚ DE IDIOMA ===
    @FXML
    private Menu menuIdioma;
    @FXML
    private MenuItem esMenuItem;
    @FXML
    private MenuItem enMenuItem;

    @FXML
    private Menu menuTema;
    @FXML
    private MenuItem temaClasicoMenuItem;
    @FXML
    private MenuItem temaBibliotecaMenuItem;
    @FXML
    private MenuItem temaNocturnaMenuItem;

    @FXML private RadioButton radioTexto;
    @FXML private RadioButton radioImagen;
    private ToggleGroup tipoArchivoToggleGroup;

    // Para almacenar la imagen cifrada/descifrada temporalmente
    private byte[] imagenProcesada;

    /**
     * Guarda el tema CSS activo
     */
    private String temaActual = "styles.css"; // tema por defecto


    /**
     * Agrupador de RadioButtons de algoritmo (AES/Vigenère)
     */
    private ToggleGroup algoritmoToggleGroup;

    /**
     * Ventana principal de la aplicación
     */
    private Stage stage;

    /**
     * Referencia a la clase principal App
     */
    private App app;

    /**
     * ResourceBundle actual para internacionalización
     */
    private ResourceBundle bundle;

    /**
     * Servicio Python para el cifrado/descifrado Vigenère
     */
    private final PythonVigenereService vigenereService = new PythonVigenereService("src/main/python/vigenere.py");

    /**
     * Inicialización automática de JavaFX.
     * <p>
     * Se ejecuta al cargar el FXML y realiza:
     * <ul>
     *     <li>Configuración del ToggleGroup de algoritmos.</li>
     *     <li>Asignación de acciones a los menús y cambio de idioma.</li>
     *     <li>Carga del ResourceBundle por defecto según la localización del sistema.</li>
     * </ul>
     * </p>
     */
    @FXML
    private void initialize() {
        log.debug("Inicializando MainController...");

        algoritmoToggleGroup = new ToggleGroup();
        aesRadioButton.setToggleGroup(algoritmoToggleGroup);
        vigenereRadioButton.setToggleGroup(algoritmoToggleGroup);
        aesRadioButton.setSelected(true);

        tipoArchivoToggleGroup = new ToggleGroup();
        radioTexto.setToggleGroup(tipoArchivoToggleGroup);
        radioImagen.setToggleGroup(tipoArchivoToggleGroup);

        radioTexto.setSelected(true);

        algoritmoToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (aesRadioButton.isSelected()) {
                radioImagen.setDisable(false);
            } else {
                radioImagen.setDisable(true);
                radioTexto.setSelected(true);
            }
        });

        cambiarTema(temaActual);

        // Asignar ResourceBundle por defecto
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        // Acciones de menús
        closeMenuItem.setOnAction(e -> cerrarAplicacion());
        manualMenuItem.setOnAction(e -> abrirManual());
        ayudaMenuItem.setOnAction(e -> mostrarAyuda());
        aboutMenuItem.setOnAction(e -> mostrarAcercaDe());
        esMenuItem.setOnAction(e -> cambiarIdioma("es"));
        enMenuItem.setOnAction(e -> cambiarIdioma("en"));

        temaClasicoMenuItem.setOnAction(e -> cambiarTema("styles.css"));
        temaBibliotecaMenuItem.setOnAction(e -> cambiarTema("styles1.css"));
        temaNocturnaMenuItem.setOnAction(e -> cambiarTema("styles2.css"));



        log.debug("MainController inicializado correctamente.");
    }





    /**
     * Asigna el Stage principal de la aplicación.
     *
     * @param stage Ventana principal {@link Stage}
     */


    public void setStage(Stage stage) {
        this.stage = stage;
        log.trace("Stage principal asignado.");
        // Aplicar el tema actual (por defecto o seleccionado)
        cambiarTema(temaActual);
    }



    private void cambiarTema(String cssFileName) {
        try {
            URL cssUrl = getClass().getResource("/estilo/" + cssFileName);
            if (cssUrl == null) {
                log.warn("Archivo CSS no encontrado: {}", cssFileName);
                return;
            }

            String css = cssUrl.toExternalForm();

            // Aplicar directamente al root, no solo a scene
            if (stage != null) {
                Scene scene = stage.getScene();
                if (scene != null && scene.getRoot() != null) {
                    scene.getRoot().getStylesheets().clear();
                    scene.getRoot().getStylesheets().add(css);
                    log.info("Tema cambiado a {} en root", cssFileName);
                } else {
                    log.warn("Stage o Scene o root es null, no se puede aplicar tema aún");
                }
            } else {
                log.warn("Stage es null, no se puede aplicar tema");
            }

            // Guardamos tema actual
            temaActual = cssFileName;

        } catch (Exception ex) {
            log.error("Error al cambiar tema: {}", ex.toString(), ex);
        }
    }




    /**
     * Asigna la referencia a la clase principal App.
     *
     * @param app Instancia de {@link App}
     */
    public void setApp(App app) {
        this.app = app;
        log.trace("Referencia a App asignada.");
    }


    /**
     * Actualiza el ResourceBundle usado para internacionalización.
     *
     * @param bundle ResourceBundle nuevo
     */
    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
        log.trace("Bundle actualizado a idioma {}.", bundle.getLocale());
    }

    /**
     * Carga la clave desde un archivo de texto.
     * <p>
     * Lee la totalidad del contenido del archivo, incluyendo saltos de línea y espacios,
     * y lo establece en el TextArea de clave. Esto permite claves multilinea.
     * </p>
     *
     * @author Xiker, Salca (modifier)
     */
    @FXML
    private void cargarClave() {
        log.info("Acción: cargar clave desde archivo.");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.choose.key"));
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            log.debug("Archivo de clave seleccionado: {}", archivo.getAbsolutePath());
            try {
                // Leer todo el contenido del archivo, incluyendo saltos de línea
                String clave = new String(java.nio.file.Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
                claveTextField.setText(clave);
                log.info("Clave cargada ({} caracteres).", clave.length());
            } catch (IOException e) {
                log.error("Error al leer clave: {}", e.toString());
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        } else {
            log.debug("Selección de archivo de clave cancelada.");
        }
    }

    /**
     * Carga el texto de entrada desde un archivo.
     * <p>El contenido completo se coloca en el TextArea de entrada.</p>
     *
     * @author Xiker
     */
    @FXML
    private void cargarEntrada() {
        log.info("Acción: cargar texto de entrada.");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.choose.input"));
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            log.debug("Archivo de entrada seleccionado: {}", archivo.getAbsolutePath());
            try {
                String contenido = new String(java.nio.file.Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
                entradaTextArea.setText(contenido);
                log.info("Texto de entrada cargado ({} caracteres).", contenido.length());
            } catch (IOException e) {
                log.error("Error al leer archivo de entrada: {}", e.toString());
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        } else {
            log.debug("Carga de archivo de entrada cancelada.");
        }
    }

    /**
     * Cifra el texto del área de entrada usando AES o Vigenère.
     * <p>El resultado se muestra en el TextArea de resultado.</p>
     *
     * @author Xiker, Salca (modifier)
     */

    @FXML
    private void cifrar() {
        log.info("Acción: cifrar");

        String clave = claveTextField.getText();
        if (clave == null || clave.isEmpty()) {
            mostrarAlertaSafe("error.title", "error.keyRequired", "Se requiere una clave para cifrar");
            return;
        }

        // =======================
        // CIFRAR IMAGEN (AES)
        // =======================
        if (aesRadioButton.isSelected() && radioImagen.isSelected()) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("file.choose.input"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.bmp"));

            File inputFile = fc.showOpenDialog(stage);
            if (inputFile == null) return;

            try {
                byte[] imagenBytes = Files.readAllBytes(inputFile.toPath());

                AESImageCipher aesImg = new AESImageCipher(clave);
                imagenProcesada = aesImg.encrypt(imagenBytes); // Guardamos bytes cifrados

                if (imagenProcesada == null || imagenProcesada.length == 0) {
                    mostrarAlertaSafe("error.title", "error.imageEncryptFailed", "No se pudo cifrar la imagen");
                    log.error("Error cifrando imagen: resultado vacío");
                    return;
                }

                FileChooser saveChooser = new FileChooser();
                saveChooser.setTitle(bundle.getString("file.save.result"));
                saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo AES", "*.aes"));

                File outputFile = saveChooser.showSaveDialog(stage);
                if (outputFile == null) return;

                Files.write(outputFile.toPath(), imagenProcesada);

                resultadoTextArea.setText(bundle.getString("image.encrypted.ready"));
                mostrarAlerta(bundle.getString("info.title"), bundle.getString("info.imageEncryptedOk"));

            } catch (Exception e) {
                log.error("Error cifrando imagen: {}", e.toString(), e);
                mostrarAlertaSafe("error.title", "error.imageEncryptFailed", "No se pudo cifrar la imagen");
            }
            return;
        }

        // =======================
        // CIFRAR TEXTO
        // =======================
        String texto = entradaTextArea.getText();
        if (texto == null || texto.isEmpty()) {
            mostrarAlertaSafe("error.textEmptyTitle", "error.textEmpty", "No hay texto para cifrar");
            return;
        }

        try {
            String resultado;

            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                resultado = aes.encrypt(texto);
            } else {
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("cifrar", texto, clave);
                resultado = r.stdout;

                if (r.stderr != null && !r.stderr.isBlank()) {
                    mostrarAlertaSafe("warning.title", "warning.vigenere", r.stderr);
                }
            }

            resultadoTextArea.setText(resultado);

        } catch (Exception e) {
            log.error("Error cifrando texto: {}", e.toString(), e);
            mostrarAlertaSafe("error.title", "error.textEncryptFailed", "No se pudo cifrar el texto");
        }
    }





    /**
     * Descifra el texto del área de entrada usando AES o Vigenère.
     * <p>El resultado se muestra en el TextArea de resultado.</p>
     *
     * @author Xiker, Salca (modifier)
     */

    @FXML
    private void descifrar() {
        log.info("Acción: descifrar");

        String clave = claveTextField.getText();
        if (clave == null || clave.isEmpty()) {
            mostrarAlertaSafe("error.title", "error.keyRequired", "Se requiere una clave para descifrar");
            return;
        }

        // =======================
        // DESCIFRAR IMAGEN (AES)
        // =======================
        if (aesRadioButton.isSelected() && radioImagen.isSelected()) {
            FileChooser fc = new FileChooser();
            fc.setTitle(bundle.getString("file.choose.input"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo AES", "*.aes"));

            File inputFile = fc.showOpenDialog(stage);
            if (inputFile == null) return;

            try {
                byte[] cifrada = Files.readAllBytes(inputFile.toPath());

                AESImageCipher aesImg = new AESImageCipher(clave);
                imagenProcesada = aesImg.decrypt(cifrada); // Guardamos bytes descifrados

                if (imagenProcesada == null || imagenProcesada.length == 0) {
                    mostrarAlertaSafe("error.title", "error.imageDecryptFailed", "No se pudo descifrar la imagen");
                    log.error("Error descifrando imagen: resultado vacío");
                    return;
                }

                FileChooser saveChooser = new FileChooser();
                saveChooser.setTitle(bundle.getString("file.save.result"));
                saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));

                File outputFile = saveChooser.showSaveDialog(stage);
                if (outputFile == null) return;

                Files.write(outputFile.toPath(), imagenProcesada);

                resultadoTextArea.setText(bundle.getString("image.decrypted.ready"));
                mostrarAlerta(bundle.getString("info.title"), bundle.getString("info.imageDecryptedOk"));

            } catch (Exception e) {
                log.error("Error descifrando imagen: {}", e.toString(), e);
                mostrarAlertaSafe("error.title", "error.imageDecryptFailed", "No se pudo descifrar la imagen");
            }
            return;
        }

        // =======================
        // DESCIFRAR TEXTO
        // =======================
        String texto = entradaTextArea.getText();
        if (texto == null || texto.isEmpty()) {
            mostrarAlertaSafe("error.textEmptyTitle", "error.textEmpty", "No hay texto para descifrar");
            return;
        }

        try {
            String resultado;

            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                resultado = aes.decrypt(texto);
            } else {
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("descifrar", texto, clave);
                resultado = r.stdout;

                if (r.stderr != null && !r.stderr.isBlank()) {
                    mostrarAlertaSafe("warning.title", "warning.vigenere", r.stderr);
                }
            }

            resultadoTextArea.setText(resultado);

        } catch (Exception e) {
            log.error("Error descifrando texto: {}", e.toString(), e);
            mostrarAlertaSafe("error.title", "error.textDecryptFailed", "No se pudo descifrar el texto");
        }
    }


    /**
     * Guarda el contenido del área de resultado en un archivo de texto.
     *
     * @author Xiker
     */

    @FXML
    private void guardarArchivo() {
        try {
            if (radioImagen.isSelected()) {
                if (imagenProcesada == null || imagenProcesada.length == 0) {
                    mostrarAlerta(bundle.getString("warning.title"), bundle.getString("error.nothingToSave"));
                    return;
                }
                FileChooser fc = new FileChooser();
                fc.setTitle(bundle.getString("file.save.result"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo AES", "*.aes"));
                File outputFile = fc.showSaveDialog(stage);
                if (outputFile != null) {
                    Files.write(outputFile.toPath(), imagenProcesada);
                    resultadoTextArea.setText(bundle.getString("info.imageSaved"));
                }
            } else {
                // Texto
                String contenido = resultadoTextArea.getText();
                if (contenido == null || contenido.isEmpty()) {
                    mostrarAlerta(bundle.getString("warning.title"), bundle.getString("error.nothingToSave"));
                    return;
                }
                FileChooser fc = new FileChooser();
                fc.setTitle(bundle.getString("file.save.result"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("file.filter.text"), "*.txt"));
                File archivo = fc.showSaveDialog(stage);
                if (archivo != null) {
                    Files.writeString(archivo.toPath(), contenido, StandardCharsets.UTF_8);
                    mostrarAlerta(bundle.getString("info.title"), bundle.getString("info.textSaved"));
                }
            }
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("error.title"), e.getMessage());
        }
    }

    private void mostrarAlertaSafe(String keyTitle, String keyMessage, String defaultMsg) {
        String titulo;
        String mensaje;
        try {
            titulo = bundle.getString(keyTitle);
            mensaje = bundle.getString(keyMessage);
        } catch (Exception e) {
            titulo = "Error";
            mensaje = defaultMsg;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    /**
     * Cierra la aplicación y registra el evento.
     *
     * @author Xiker
     */
    private void cerrarAplicacion() {
        log.info("Cerrando aplicación.");
        if (stage != null) stage.close();
    }

    /**
     * Abre el manual del usuario en el navegador.
     *
     * @author Xiker
     */
    private void abrirManual() {
        log.info("Abriendo manual del usuario.");
        if (app != null) {
            try {
                String manualPath = bundle.getString("manual.url");
                URL manualURL = getClass().getResource(manualPath);

                if (manualURL != null) {
                    log.debug("Manual encontrado: {}", manualURL);
                    app.getHostServices().showDocument(manualURL.toExternalForm());
                } else {
                    log.warn("Manual no encontrado: {}", manualPath);
                    mostrarAlerta(bundle.getString("error.title"),
                            bundle.getString("error.manualNotFound") + ": " + manualPath);
                }
            } catch (Exception e) {
                log.error("Error al abrir manual: {}", e.toString());
                mostrarAlerta(bundle.getString("error.title"),
                        bundle.getString("error.manualOpenFail") + "\n" + e.getMessage());
            }
        }
    }

    /**
     * Muestra un cuadro de ayuda informativa al usuario.
     *
     * @author Xiker
     */
    private void mostrarAyuda() {
        log.info("Mostrando ventana de ayuda.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("help.title"));
        alert.setHeaderText(bundle.getString("help.header"));
        alert.setContentText(bundle.getString("help.content"));
        alert.showAndWait();
    }

    /**
     * Muestra el cuadro "Acerca de" con información de la aplicación.
     *
     * @author Xiker
     */
    private void mostrarAcercaDe() {
        log.info("Mostrando ventana Acerca de.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("about.title"));
        alert.setHeaderText(bundle.getString("about.header"));
        alert.setContentText(bundle.getString("about.content"));
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de advertencia.
     *
     * @param titulo  Título de la alerta
     * @param mensaje Contenido de la alerta
     * @author Xiker
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        log.debug("Mostrando alerta: {}", titulo);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cambia el idioma de la aplicación recargando el FXML con un nuevo ResourceBundle.
     *
     * @param idioma Código de idioma (ej. "es" o "en")
     * @author Xiker
     */
    private void cambiarIdioma(String idioma) {
        log.info("Cambiando idioma a '{}'", idioma);
        try {
            // Crear nuevo Locale y ResourceBundle
            Locale locale = new Locale(idioma);
            ResourceBundle bundleNuevo = ResourceBundle.getBundle("i18n.messages", locale);

            // Cargar FXML con nuevo ResourceBundle
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundleNuevo);
            VBox nuevoRoot = fxmlLoader.load();

            // Obtener el nuevo controller
            MainController controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.setApp(app);
            controller.setBundle(bundleNuevo);

            // Pasar tema actual al nuevo controller
            controller.setTemaActual(this.temaActual);

            // Reemplazar root actual sin crear nueva escena
            if (stage.getScene() != null) {
                stage.getScene().setRoot(nuevoRoot);
            } else {
                stage.setScene(new Scene(nuevoRoot, 900, 580));
            }

            // Aplicar tema actual
            controller.cambiarTema(controller.getTemaActual());

            // Actualizar título de la ventana
            stage.setTitle(bundleNuevo.getString("app.title"));

            log.info("Idioma cambiado correctamente a {}.", locale);
        } catch (Exception e) {
            log.error("Error cambiando idioma: {}", e.toString());
            mostrarAlerta(bundle.getString("error.title"),
                    bundle.getString("error.changeLangFail") + "\n" + e.getMessage());
        }
    }



    public void setTemaActual(String temaActual) {
        this.temaActual = temaActual;
    }

    public String getTemaActual() {
        return temaActual;
    }



}

