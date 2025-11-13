package org.hogwartsencriptado;

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
 * Gestiona la interfaz, cifrado/descifrado, idioma y menús.
 */
public class MainController {

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

    private ToggleGroup algoritmoToggleGroup;
    private Stage stage;
    private App app;

    private ResourceBundle bundle;

    // Servicio Vigenère
    private final PythonVigenereService vigenereService = new PythonVigenereService("src/main/python/vigenere.py");

    @FXML
    private void initialize() {
        algoritmoToggleGroup = new ToggleGroup();
        aesRadioButton.setToggleGroup(algoritmoToggleGroup);
        vigenereRadioButton.setToggleGroup(algoritmoToggleGroup);
        aesRadioButton.setSelected(true);

        bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        if (closeMenuItem != null) closeMenuItem.setOnAction(e -> cerrarAplicacion());
        if (manualMenuItem != null) manualMenuItem.setOnAction(e -> abrirManual());
        if (ayudaMenuItem != null) ayudaMenuItem.setOnAction(e -> mostrarAyuda());
        if (aboutMenuItem != null) aboutMenuItem.setOnAction(e -> mostrarAcercaDe());

        if (esMenuItem != null) esMenuItem.setOnAction(e -> cambiarIdioma("es"));
        if (enMenuItem != null) enMenuItem.setOnAction(e -> cambiarIdioma("en"));
    }

    public void setStage(Stage stage) { this.stage = stage; }

    public void setApp(App app) { this.app = app; }

    public void setBundle(ResourceBundle bundle) { this.bundle = bundle; }

    @FXML
    private void cargarClave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.choose.key"));
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
                claveTextField.setText(br.readLine());
            } catch (IOException e) {
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void cargarEntrada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("file.choose.input"));
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            try {
                entradaTextArea.setText(new String(java.nio.file.Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void cifrar() {
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta(bundle.getString("error.keyRequiredTitle"), bundle.getString("error.keyRequired"));
            return;
        }
        if (texto == null || texto.isEmpty()) {
            mostrarAlerta(bundle.getString("error.textEmptyTitle"), bundle.getString("error.textEmpty"));
            return;
        }

        try {
            String textoCifrado;
            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                textoCifrado = aes.encrypt(texto);
            } else {
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("cifrar", texto, clave);
                textoCifrado = r.stdout;
                if (r.stderr != null && !r.stderr.isBlank())
                    mostrarAlerta(bundle.getString("warning.title"), r.stderr);
            }
            resultadoTextArea.setText(textoCifrado);
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("error.encryptTitle"), e.getMessage());
        }
    }

    @FXML
    private void descifrar() {
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta(bundle.getString("error.keyRequiredTitle"), bundle.getString("error.keyRequired"));
            return;
        }
        if (texto == null || texto.isEmpty()) {
            mostrarAlerta(bundle.getString("error.textEmptyTitle"), bundle.getString("error.textEmpty"));
            return;
        }

        try {
            String textoDescifrado;
            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                textoDescifrado = aes.decrypt(texto);
            } else {
                PythonVigenereService.PythonResult r = vigenereService.procesarTexto("descifrar", texto, clave);
                textoDescifrado = r.stdout;
                if (r.stderr != null && !r.stderr.isBlank())
                    mostrarAlerta(bundle.getString("warning.title"), r.stderr);
            }
            resultadoTextArea.setText(textoDescifrado);
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("error.decryptTitle"), e.getMessage());
        }
    }

    @FXML
    private void guardarArchivo() {
        String contenido = resultadoTextArea.getText();
        if (contenido == null || contenido.isEmpty()) {
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
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
                bw.write(contenido);
            } catch (IOException e) {
                mostrarAlerta(bundle.getString("error.title"), e.getMessage());
            }
        }
    }

    private void cerrarAplicacion() {
        if (stage != null) stage.close();
    }

    private void abrirManual() {
        if (app != null) {
            try {
                // Usar el bundle actual que refleja el idioma seleccionado
                String manualPath = bundle.getString("manual.url");
                URL manualURL = getClass().getResource(manualPath);

                if (manualURL != null) {
                    app.getHostServices().showDocument(manualURL.toExternalForm());
                } else {
                    mostrarAlerta(bundle.getString("error.title"),
                            bundle.getString("error.manualNotFound") + ": " + manualPath);
                }
            } catch (Exception e) {
                mostrarAlerta(bundle.getString("error.title"),
                        bundle.getString("error.manualOpenFail") + "\n" + e.getMessage());
            }
        }
    }

    private void mostrarAyuda() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("help.title"));
        alert.setHeaderText(bundle.getString("help.header"));
        alert.setContentText(bundle.getString("help.content"));
        alert.showAndWait();
    }

    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("about.title"));
        alert.setHeaderText(bundle.getString("about.header"));
        alert.setContentText(bundle.getString("about.content"));
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cambiarIdioma(String idioma) {
        try {
            Locale locale = new Locale(idioma);
            ResourceBundle bundleNuevo = ResourceBundle.getBundle("i18n.messages", locale);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundleNuevo);
            Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());

            MainController controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.setApp(app);
            controller.setBundle(bundleNuevo); // actualizar bundle

            stage.setScene(scene);
            stage.setTitle(bundleNuevo.getString("app.title"));
        } catch (Exception e) {
            mostrarAlerta(bundle.getString("error.title"),
                    bundle.getString("error.changeLangFail") + "\n" + e.getMessage());
        }
    }
}
