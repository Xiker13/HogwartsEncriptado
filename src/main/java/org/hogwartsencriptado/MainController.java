package org.hogwartsencriptado;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

/**
 * Controlador principal de la aplicación de cifrado AES.
 * Gestiona la interacción con la interfaz de usuario definida en MainView.fxml.
 * Permite ingresar una clave, cargar texto o archivos, cifrar/descifrar y guardar resultados.
 */
public class MainController {

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

    /** Stage principal, necesario para FileChooser */
    private Stage stage;

    /**
     * Permite al controlador conocer el Stage principal para abrir diálogos.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Carga la clave desde un archivo y la coloca en claveTextField.
     */
    @FXML
    private void cargarClave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivo de clave");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder clave = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    clave.append(line);
                }
                claveTextField.setText(clave.toString());
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo leer el archivo de clave.");
            }
        }
    }

    /**
     * Carga la entrada desde un archivo y la coloca en entradaTextArea.
     */
    @FXML
    private void cargarEntrada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivo de entrada");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder contenido = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    contenido.append(line).append(System.lineSeparator());
                }
                entradaTextArea.setText(contenido.toString());
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo leer el archivo de entrada.");
            }
        }
    }

    /**
     * Verifica que haya clave y entrada antes de cifrar.
     * Aquí se debería llamar a la lógica AES real.
     */
    @FXML
    private void cifrar() {
        String clave = claveTextField.getText();
        String entrada = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta("Clave requerida", "Debes ingresar una clave antes de cifrar.");
            return;
        }
        if (entrada == null || entrada.isEmpty()) {
            mostrarAlerta("Entrada requerida", "Debes ingresar texto o cargar un archivo antes de cifrar.");
            return;
        }

        // TODO: Lógica AES real aquí
        resultadoTextArea.setText("CIFRADO: " + entrada);
    }

    /**
     * Verifica que haya clave y entrada antes de descifrar.
     * Aquí se debería llamar a la lógica AES real.
     */
    @FXML
    private void descifrar() {
        String clave = claveTextField.getText();
        String entrada = entradaTextArea.getText();

        if (clave == null || clave.isEmpty()) {
            mostrarAlerta("Clave requerida", "Debes ingresar una clave antes de descifrar.");
            return;
        }
        if (entrada == null || entrada.isEmpty()) {
            mostrarAlerta("Entrada requerida", "Debes ingresar texto o cargar un archivo antes de descifrar.");
            return;
        }

        // TODO: Lógica AES real aquí
        resultadoTextArea.setText("DESCIFRADO: " + entrada);
    }

    /**
     * Guarda el contenido de resultadoTextArea en un archivo en la ruta indicada.
     */
    @FXML
    private void guardarArchivo() {
        String ruta = rutaGuardarTextField.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Ruta requerida", "Debes ingresar la ruta donde guardar el archivo.");
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write(resultadoTextArea.getText());
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar el archivo en la ruta especificada.");
        }
    }

    /**
     * Muestra una alerta de advertencia con el título y mensaje especificados.
     *
     * @param titulo  Título de la ventana de alerta
     * @param mensaje Mensaje que se mostrará al usuario
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
