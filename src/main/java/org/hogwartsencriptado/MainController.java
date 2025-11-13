package org.hogwartsencriptado;

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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador principal de Hogwarts Encriptado.
 * <p>
 * Gestiona la interfaz de usuario, las acciones de cifrado y descifrado,
 * la carga y guardado de archivos, y el cambio de idioma.
 * </p>
 *
 * Esta versión incluye trazas de logging usando SLF4J + Logback,
 * lo que permite registrar eventos relevantes en consola y archivos.
 */
public class MainController {

    /** Logger de la clase (gestiona trazas informativas, advertencias y errores) */
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // === ELEMENTOS DE LA INTERFAZ ===
    @FXML private TextField claveTextField;
    @FXML private Button cargarClaveButton;
    @FXML private TextArea entradaTextArea;
    @FXML private Button cargarEntradaButton;
    @FXML private Button cifrarButton;
    @FXML private Button descifrarButton;
    @FXML private TextArea resultadoTextArea;
    @FXML private TextField nombreArchivoTextField;
    @FXML private Button guardarArchivoButton;
    @FXML private RadioButton aesRadioButton;
    @FXML private RadioButton vigenereRadioButton;

    // === MENÚS ===
    @FXML private MenuItem closeMenuItem;
    @FXML private MenuItem manualMenuItem;
    @FXML private MenuItem ayudaMenuItem;
    @FXML private MenuItem aboutMenuItem;

    // === MENÚ DE IDIOMA ===
    @FXML private Menu menuIdioma;
    @FXML private MenuItem esMenuItem;
    @FXML private MenuItem enMenuItem;

    /** Agrupador para los botones de algoritmo (AES/Vigenère) */
    private ToggleGroup algoritmoToggleGroup;

    /** Referencia al Stage principal */
    private Stage stage;

    /** Referencia a la clase App principal */
    private App app;

    /** Bundle de idioma actual */
    private ResourceBundle bundle;

    /** Servicio de cifrado/descifrado Vigenère en Python */
    private final PythonVigenereService vigenereService = new PythonVigenereService("src/main/python/vigenere.py");

    /**
     * Método de inicialización automática de JavaFX.
     * Se ejecuta al cargar el FXML.
     */
    @FXML
    private void initialize() {
        log.debug("Inicializando MainController...");

        // Grupo de selección entre AES y Vigenère
        algoritmoToggleGroup = new ToggleGroup();
        aesRadioButton.setToggleGroup(algoritmoToggleGroup);
        vigenereRadioButton.setToggleGroup(algoritmoToggleGroup);
        aesRadioButton.setSelected(true);

        // Carga del idioma por defecto
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        log.info("Idioma cargado: {} ({} claves)", Locale.getDefault(), bundle.keySet().size());

        // Asignación de acciones a los menús
        if (closeMenuItem != null) closeMenuItem.setOnAction(e -> cerrarAplicacion());
        if (manualMenuItem != null) manualMenuItem.setOnAction(e -> abrirManual());
        if (ayudaMenuItem != null) ayudaMenuItem.setOnAction(e -> mostrarAyuda());
        if (aboutMenuItem != null) aboutMenuItem.setOnAction(e -> mostrarAcercaDe());

        // Asignación de cambio de idioma
        if (esMenuItem != null) esMenuItem.setOnAction(e -> cambiarIdioma("es"));
        if (enMenuItem != null) enMenuItem.setOnAction(e -> cambiarIdioma("en"));

        log.debug("MainController inicializado correctamente.");
    }

    /** Asigna el Stage principal */
    public void setStage(Stage stage) {
        this.stage = stage;
        log.trace("Stage principal asignado.");
    }

    /** Asigna la referencia a la App */
    public void setApp(App app) {
        this.app = app;
        log.trace("Referencia a App asignada.");
    }

    /** Actualiza el ResourceBundle de idioma */
    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
        log.trace("Bundle actualizado a idioma {}.", bundle.getLocale());
    }

    /**
     * Carga una clave desde un archivo de texto.
     * Solo se lee la primera línea.
     */
    @FXML
    private void cargarClave() {
        log.info("Acción: cargar clave desde archivo.");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.choose.key"));
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            log.debug("Archivo de clave seleccionado: {}", archivo.getAbsolutePath());
            try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
                String linea = br.readLine();
                claveTextField.setText(linea);
                log.info("Clave cargada ({} caracteres).", linea == null ? 0 : linea.length());
            } catch (IOException e) {
                log.error("Error al leer clave: {}", e.toString());
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        } else {
            log.debug("Selección de archivo de clave cancelada.");
        }
    }

    /**
     * Carga texto plano o cifrado desde un archivo.
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
     */
    @FXML
    private void cifrar() {
        log.info("Acción: cifrar texto.");
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            log.warn("Cifrado abortado: clave vacía.");
            mostrarAlerta(bundle.getString("error.keyRequiredTitle"), bundle.getString("error.keyRequired"));
            return;
        }
        if (texto == null || texto.isEmpty()) {
            log.warn("Cifrado abortado: texto vacío.");
            mostrarAlerta(bundle.getString("error.textEmptyTitle"), bundle.getString("error.textEmpty"));
            return;
        }

        try {
            String textoCifrado;
            if (aesRadioButton.isSelected()) {
                log.debug("Usando algoritmo AES.");
                AESCipher aes = new AESCipher(clave);
                textoCifrado = aes.encrypt(texto);
            } else {
                log.debug("Usando algoritmo Vigenère (Python).");
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("cifrar", texto, clave);
                textoCifrado = r.stdout;
                if (r.stderr != null && !r.stderr.isBlank())
                    log.warn("Avisos desde Python: {}", r.stderr);
            }
            resultadoTextArea.setText(textoCifrado);
            log.info("Cifrado completado ({} caracteres resultado).", textoCifrado.length());
        } catch (Exception e) {
            log.error("Error durante cifrado: {}", e.toString());
            mostrarAlerta(bundle.getString("error.encryptTitle"), e.getMessage());
        }
    }

    /**
     * Descifra el texto del área de entrada usando AES o Vigenère.
     */
    @FXML
    private void descifrar() {
        log.info("Acción: descifrar texto.");
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            log.warn("Descifrado abortado: clave vacía.");
            mostrarAlerta(bundle.getString("error.keyRequiredTitle"), bundle.getString("error.keyRequired"));
            return;
        }
        if (texto == null || texto.isEmpty()) {
            log.warn("Descifrado abortado: texto vacío.");
            mostrarAlerta(bundle.getString("error.textEmptyTitle"), bundle.getString("error.textEmpty"));
            return;
        }

        try {
            String textoDescifrado;
            if (aesRadioButton.isSelected()) {
                log.debug("Usando algoritmo AES.");
                AESCipher aes = new AESCipher(clave);
                textoDescifrado = aes.decrypt(texto);
            } else {
                log.debug("Usando algoritmo Vigenère (Python).");
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("descifrar", texto, clave);
                textoDescifrado = r.stdout;
                if (r.stderr != null && !r.stderr.isBlank())
                    log.warn("Avisos desde Python: {}", r.stderr);
            }
            resultadoTextArea.setText(textoDescifrado);
            log.info("Descifrado completado ({} caracteres resultado).", textoDescifrado.length());
        } catch (Exception e) {
            log.error("Error durante descifrado: {}", e.toString());
            mostrarAlerta(bundle.getString("error.decryptTitle"), e.getMessage());
        }
    }

    /**
     * Guarda el texto cifrado o descifrado en un archivo.
     */
    @FXML
    private void guardarArchivo() {
        log.info("Acción: guardar resultado en archivo.");
        String contenido = resultadoTextArea.getText();
        if (contenido == null || contenido.isEmpty()) {
            log.warn("Guardar abortado: no hay contenido.");
            mostrarAlerta(bundle.getString("warning.title"), bundle.getString("error.nothingToSave"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.save.result"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(bundle.getString("file.filter.text"), "*.txt"));

        String nombre = nombreArchivoTextField.getText();
        if (nombre != null && !nombre.isBlank()) {
            if (!nombre.toLowerCase().endsWith(".txt")) nombre += ".txt";
            fileChooser.setInitialFileName(nombre);
        }

        File archivo = fileChooser.showSaveDialog(stage);
        if (archivo != null) {
            log.debug("Guardando archivo: {}", archivo.getAbsolutePath());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
                bw.write(contenido);
                log.info("Archivo guardado correctamente ({} bytes).", contenido.getBytes(StandardCharsets.UTF_8).length);
            } catch (IOException e) {
                log.error("Error al guardar archivo: {}", e.toString());
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        } else {
            log.debug("Guardado cancelado por el usuario.");
        }
    }

    /** Cierra la aplicación. */
    private void cerrarAplicacion() {
        log.info("Cerrando aplicación.");
        if (stage != null) stage.close();
    }

    /** Abre el manual del usuario. */
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
                    log.warn("Manual no encontrado en el classpath: {}", manualPath);
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

    /** Muestra un cuadro de ayuda informativa. */
    private void mostrarAyuda() {
        log.info("Mostrando ventana de ayuda.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("help.title"));
        alert.setHeaderText(bundle.getString("help.header"));
        alert.setContentText(bundle.getString("help.content"));
        alert.showAndWait();
    }

    /** Muestra el cuadro "Acerca de". */
    private void mostrarAcercaDe() {
        log.info("Mostrando ventana Acerca de.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("about.title"));
        alert.setHeaderText(bundle.getString("about.header"));
        alert.setContentText(bundle.getString("about.content"));
        alert.showAndWait();
    }

    /** Muestra una alerta simple en pantalla. */
    private void mostrarAlerta(String titulo, String mensaje) {
        log.debug("Mostrando alerta: {}", titulo);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cambia el idioma de la interfaz.
     * Recarga el FXML con el nuevo ResourceBundle.
     */
    private void cambiarIdioma(String idioma) {
        log.info("Cambiando idioma a '{}'", idioma);
        try {
            Locale locale = new Locale(idioma);
            ResourceBundle bundleNuevo = ResourceBundle.getBundle("i18n.messages", locale);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundleNuevo);
            Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());

            MainController controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.setApp(app);
            controller.setBundle(bundleNuevo);

            stage.setScene(scene);
            stage.setTitle(bundleNuevo.getString("app.title"));
            log.info("Idioma cambiado correctamente a {}.", locale);
        } catch (Exception e) {
            log.error("Error cambiando idioma: {}", e.toString());
            mostrarAlerta(bundle.getString("error.title"),
                    bundle.getString("error.changeLangFail") + "\n" + e.getMessage());
        }
    }
}
