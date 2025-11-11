package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Controlador principal JavaFX.
 */
public class MainController {

    @FXML
    private TextField claveTextField;

    @FXML
    private TextArea entradaTextArea;

    @FXML
    private TextArea resultadoTextArea;

    @FXML
    private TextField rutaGuardarTextField;

    // servicio que llama al python
    private final PythonVigenereService vigenereService =
            new PythonVigenereService("src/main/python/vigenere.py");

    // ================== BOTONES DE TEXTO ==================

    @FXML
    private void cifrar() {
        String texto = entradaTextArea.getText().trim();
        String clave = claveTextField.getText().trim();

        if (texto.isEmpty()) {
            mostrarAlerta("Texto vacío", "Introduce un texto o carga un archivo.");
            return;
        }
        if (clave.isEmpty()) {
            mostrarAlerta("Clave vacía", "Introduce una clave.");
            return;
        }

        try {
            PythonVigenereService.PythonResult r =
                    vigenereService.procesarTexto("cifrar", texto, clave);

            resultadoTextArea.setText(r.stdout);

            if (r.stderr != null && !r.stderr.isBlank()) {
                mostrarAlerta("Aviso", r.stderr);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo ejecutar Python:\n" + e.getMessage());
        }
    }

    @FXML
    private void descifrar() {
        String texto = entradaTextArea.getText().trim();
        String clave = claveTextField.getText().trim();

        if (texto.isEmpty()) {
            mostrarAlerta("Texto vacío", "Introduce un texto o carga un archivo.");
            return;
        }
        if (clave.isEmpty()) {
            mostrarAlerta("Clave vacía", "Introduce una clave.");
            return;
        }

        try {
            PythonVigenereService.PythonResult r =
                    vigenereService.procesarTexto("descifrar", texto, clave);

            resultadoTextArea.setText(r.stdout);

            if (r.stderr != null && !r.stderr.isBlank()) {
                mostrarAlerta("Aviso", r.stderr);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo ejecutar Python:\n" + e.getMessage());
        }
    }

    // ================== ARCHIVOS (cargar/guardar) ==================

    @FXML
    private void cargarEntrada() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona archivo de entrada");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));

        Window w = entradaTextArea.getScene().getWindow();
        File file = fc.showOpenDialog(w);

        if (file != null) {
            try {
                String contenido = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                entradaTextArea.setText(contenido);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo leer el archivo.");
            }
        }
    }

    @FXML
    private void cargarClave() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona archivo de clave");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));

        Window w = claveTextField.getScene().getWindow();
        File file = fc.showOpenDialog(w);

        if (file != null) {
            try {
                String contenido = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String primeraLinea = contenido.lines().findFirst().orElse("");
                claveTextField.setText(primeraLinea.trim());
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo leer la clave.");
            }
        }
    }

    @FXML
    private void guardarArchivo() {
        String contenido = resultadoTextArea.getText();
        if (contenido == null || contenido.isBlank()) {
            mostrarAlerta("Sin contenido", "No hay nada que guardar.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar resultado");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
        fc.setInitialFileName("resultado_vigenere.txt");

        File file = fc.showSaveDialog(resultadoTextArea.getScene().getWindow());
        if (file == null) return;

        try {
            Files.writeString(
                    file.toPath(),
                    contenido,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            if (rutaGuardarTextField != null) {
                rutaGuardarTextField.setText(file.getAbsolutePath());
            }
            mostrarAlerta("Guardado", "Archivo guardado en:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el archivo.");
        }
    }

    // ================== UTIL ==================

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}


