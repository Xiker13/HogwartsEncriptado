package org.hogwartsencriptado;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Controlador principal para la aplicación de cifrado.
 * Gestiona las interacciones del usuario, incluyendo carga de archivos,
 * operaciones de cifrado/descifrado y guardado del resultado.
 * Soporta AES y, temporalmente, devuelve un placeholder para Vigenère
 * hasta que se implemente la clase correspondiente.
 */
public class MainController {

    // === ELEMENTOS DE LA INTERFAZ ===
    @FXML
    private TextField claveTextField;

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
    private TextField rutaGuardarTextField;

    @FXML
    private Button guardarArchivoButton;

    // === RADIOBUTTONS ===
    @FXML
    private RadioButton aesRadioButton;

    @FXML
    private RadioButton vigenereRadioButton;

    // === TOGGLEGROUP (se crea programáticamente) ===
    private ToggleGroup algoritmoToggleGroup;

    private Stage stage;

    /**
     * Método de inicialización que se llama automáticamente después de que se carga el FXML
     * Aquí configuramos el ToggleGroup para los RadioButtons
     */
    @FXML
    private void initialize() {
        // Inicializar el ToggleGroup y asignarlo a los RadioButtons
        algoritmoToggleGroup = new ToggleGroup();
        aesRadioButton.setToggleGroup(algoritmoToggleGroup);
        vigenereRadioButton.setToggleGroup(algoritmoToggleGroup);

        // Seleccionar AES por defecto
        aesRadioButton.setSelected(true);
    }

    /** Establece el stage principal (usado para los diálogos de archivos) */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // === ACCIONES ===

    @FXML
    private void cargarClave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de clave");
        File archivo = fileChooser.showOpenDialog(stage);

        if (archivo != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
                String clave = br.readLine();
                claveTextField.setText(clave);
            } catch (IOException e) {
                mostrarAlerta("Error al leer el archivo de clave", e.getMessage());
            }
        }
    }

    @FXML
    private void cargarEntrada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de entrada");
        File archivo = fileChooser.showOpenDialog(stage);

        if (archivo != null) {
            try {
                String contenido = new String(java.nio.file.Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
                entradaTextArea.setText(contenido);
            } catch (IOException e) {
                mostrarAlerta("Error al leer el archivo de entrada", e.getMessage());
            }
        }
    }

    @FXML
    private void cifrar() {
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta("Clave requerida", "Debes ingresar una clave antes de cifrar.");
            return;
        }
        if (texto == null || texto.isEmpty()) {
            mostrarAlerta("Texto vacío", "Debes ingresar texto o cargar un archivo para cifrar.");
            return;
        }

        try {
            String textoCifrado;

            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                textoCifrado = aes.encrypt(texto);
            } else {
                textoCifrado = generateVigenerePlaceholderEncrypt(texto, clave);
            }

            resultadoTextArea.setText(textoCifrado);

        } catch (Exception e) {
            mostrarAlerta("Error al cifrar", e.getMessage());
        }
    }

    @FXML
    private void descifrar() {
        String clave = claveTextField.getText();
        String texto = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta("Clave requerida", "Debes ingresar una clave antes de descifrar.");
            return;
        }
        if (texto == null || texto.isEmpty()) {
            mostrarAlerta("Texto vacío", "Debes ingresar texto cifrado o cargar un archivo para descifrar.");
            return;
        }

        try {
            String textoDescifrado;

            if (aesRadioButton.isSelected()) {
                AESCipher aes = new AESCipher(clave);
                textoDescifrado = aes.decrypt(texto);
            } else {
                textoDescifrado = generateVigenerePlaceholderDecrypt(texto, clave);
            }

            resultadoTextArea.setText(textoDescifrado);

        } catch (Exception e) {
            mostrarAlerta("Error al descifrar", e.getMessage());
        }
    }

    @FXML
    private void guardarArchivo() {
        String ruta = rutaGuardarTextField.getText();
        String contenido = resultadoTextArea.getText();

        if (contenido == null || contenido.isEmpty()) {
            mostrarAlerta("Nada que guardar", "El área de resultado está vacía.");
            return;
        }

        try {
            File archivo;
            if (ruta == null || ruta.isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar archivo de resultado");
                archivo = fileChooser.showSaveDialog(stage);
            } else {
                archivo = new File(ruta);
            }

            if (archivo != null) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
                    bw.write(contenido);
                }
            }

        } catch (IOException e) {
            mostrarAlerta("Error al guardar archivo", e.getMessage());
        }
    }

    // === PLACEHOLDERS PARA VIGENÈRE (temporal) ===
    private String generateVigenerePlaceholderEncrypt(String texto, String clave) {
        String preview = texto.length() > 40 ? texto.substring(0, 40) + "..." : texto;
        String simulated = new StringBuilder(preview).reverse().toString();
        return "[VIGENÈRE - placeholder de cifrado]\n\nClave usada: " + summarize(clave) + "\n\nSimulación (preview invertida):\n" + simulated;
    }

    private String generateVigenerePlaceholderDecrypt(String texto, String clave) {
        String preview = texto.length() > 40 ? texto.substring(0, 40) + "..." : texto;
        String simulated = new StringBuilder(preview).reverse().toString();
        return "[VIGENÈRE - placeholder de descifrado]\n\nClave usada: " + summarize(clave) + "\n\nSimulación (preview invertida):\n" + simulated;
    }

    private String summarize(String s) {
        if (s == null) return "<vacía>";
        s = s.trim();
        if (s.length() <= 8) return s;
        return s.substring(0, 4) + "..." + s.substring(s.length() - 4);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}