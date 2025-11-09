package org.hogwartsencriptado;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    // Métodos de acción (vacíos por ahora)
    @FXML
    private void cargarClave() {}

    @FXML
    private void cargarEntrada() {}

    @FXML
    private void cifrar() {}

    @FXML
    private void descifrar() {}

    @FXML
    private void guardarArchivo() {}
}
