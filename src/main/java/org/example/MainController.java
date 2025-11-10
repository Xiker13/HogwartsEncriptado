package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Controlador principal de la interfaz JavaFX para el cifrado Vigenère.
 * Maneja la interacción con la API de Python para cifrar y descifrar texto.
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

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem manualMenuItem;

    @FXML
    private MenuItem ayudaMenuItem;

    @FXML
    private MenuItem aboutMenuItem;

    // Cliente de la API
    private VigenereApiClient apiClient;

    // Ruta del último archivo cargado
    private String ultimaRutaCargada = null;

    /**
     * Inicialización del controlador (se llama automáticamente después de cargar el FXML)
     */
    @FXML
    public void initialize() {
        apiClient = new VigenereApiClient();

        // Configurar manejadores de eventos del menú
        configurarMenus();

        // Verificar conexión con la API
        verificarConexionApi();

        // Configurar tooltips
        configurarTooltips();
    }

    /**
     * Configura los manejadores de eventos de los elementos del menú
     */
    private void configurarMenus() {
        closeMenuItem.setOnAction(e -> cerrarAplicacion());
        manualMenuItem.setOnAction(e -> mostrarManual());
        ayudaMenuItem.setOnAction(e -> mostrarAyuda());
        aboutMenuItem.setOnAction(e -> mostrarAcercaDe());
    }

    /**
     * Configura los tooltips de los componentes
     */
    private void configurarTooltips() {
        claveTextField.setTooltip(new Tooltip("Introduce la clave de cifrado (mínimo 3 letras)"));
        entradaTextArea.setTooltip(new Tooltip("Escribe el texto aquí o carga un archivo"));
        resultadoTextArea.setTooltip(new Tooltip("Aquí aparecerá el resultado"));
    }

    /**
     * Verifica que la API de Python esté en funcionamiento
     */
    private void verificarConexionApi() {
        Thread thread = new Thread(() -> {
            boolean conectado = apiClient.verificarConexion();
            Platform.runLater(() -> {
                if (!conectado) {
                    mostrarError("Conexión con API",
                            "No se pudo conectar con la API de Python.\n" +
                                    "Asegúrate de que el servidor Flask esté corriendo en:\n" +
                                    "http://localhost:5000\n\n" +
                                    "Para iniciar el servidor, ejecuta:\n" +
                                    "python api_vigenere.py");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ========================================================================
    // MÉTODOS DE ACCIÓN DE BOTONES
    // ========================================================================

    /**
     * Carga la clave desde un archivo de texto
     */
    @FXML
    private void cargarClave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de clave");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
        );

        File archivo = fileChooser.showOpenDialog(getStage());
        if (archivo != null) {
            try {
                String contenido = leerArchivo(archivo.getAbsolutePath());
                // Tomar solo la primera línea como clave
                String clave = contenido.lines().findFirst().orElse("").trim();
                claveTextField.setText(clave);
                mostrarInformacion("Clave cargada", "Clave cargada correctamente desde: " + archivo.getName());
            } catch (IOException e) {
                mostrarError("Error al cargar clave", "No se pudo leer el archivo: " + e.getMessage());
            }
        }
    }

    /**
     * Carga el texto de entrada desde un archivo
     */
    @FXML
    private void cargarEntrada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de entrada");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivo = fileChooser.showOpenDialog(getStage());
        if (archivo != null) {
            try {
                String contenido = leerArchivo(archivo.getAbsolutePath());
                entradaTextArea.setText(contenido);
                ultimaRutaCargada = archivo.getAbsolutePath();
                mostrarInformacion("Archivo cargado",
                        "Archivo cargado: " + archivo.getName() + "\n" +
                                "Tamaño: " + contenido.length() + " caracteres");
            } catch (IOException e) {
                mostrarError("Error al cargar archivo", "No se pudo leer el archivo: " + e.getMessage());
            }
        }
    }

    /**
     * Cifra el texto introducido
     */
    @FXML
    private void cifrar() {
        String texto = entradaTextArea.getText();
        String clave = claveTextField.getText();

        // Validaciones
        if (texto.isEmpty()) {
            mostrarAdvertencia("Campo vacío", "Por favor, introduce un texto o carga un archivo para cifrar.");
            return;
        }

        if (clave.isEmpty()) {
            mostrarAdvertencia("Clave vacía", "Por favor, introduce una clave de cifrado.");
            return;
        }

        // Deshabilitar botones durante el proceso
        cifrarButton.setDisable(true);
        descifrarButton.setDisable(true);

        // Ejecutar cifrado en segundo plano
        Thread thread = new Thread(() -> {
            try {
                VigenereApiClient.ResultadoCifrado resultado = apiClient.cifrarTexto(texto, clave);

                Platform.runLater(() -> {
                    if (resultado.isExito()) {
                        resultadoTextArea.setText(resultado.getTextoCifrado());
                        mostrarInformacion("Cifrado exitoso",
                                "Texto cifrado correctamente.\n" +
                                        "Longitud: " + resultado.getTextoCifrado().length() + " caracteres");
                    } else {
                        mostrarError("Error de cifrado", resultado.getError());
                    }
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });

            } catch (VigenereApiClient.VigenereApiException e) {
                Platform.runLater(() -> {
                    mostrarError("Error de la API", e.getMessage());
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    mostrarError("Error de conexión",
                            "No se pudo conectar con la API.\n" +
                                    "Asegúrate de que el servidor Python esté corriendo.\n\n" +
                                    "Error: " + e.getMessage());
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Descifra el texto introducido
     */
    @FXML
    private void descifrar() {
        String textoCifrado = entradaTextArea.getText();
        String clave = claveTextField.getText();

        // Validaciones
        if (textoCifrado.isEmpty()) {
            mostrarAdvertencia("Campo vacío", "Por favor, introduce un texto cifrado o carga un archivo para descifrar.");
            return;
        }

        if (clave.isEmpty()) {
            mostrarAdvertencia("Clave vacía", "Por favor, introduce la clave de descifrado.");
            return;
        }

        // Deshabilitar botones durante el proceso
        cifrarButton.setDisable(true);
        descifrarButton.setDisable(true);

        // Ejecutar descifrado en segundo plano
        Thread thread = new Thread(() -> {
            try {
                VigenereApiClient.ResultadoDescifrado resultado = apiClient.descifrarTexto(textoCifrado, clave);

                Platform.runLater(() -> {
                    if (resultado.isExito()) {
                        resultadoTextArea.setText(resultado.getTextoDescifrado());
                        mostrarInformacion("Descifrado exitoso",
                                "Texto descifrado correctamente.\n" +
                                        "Longitud: " + resultado.getTextoDescifrado().length() + " caracteres");
                    } else {
                        mostrarError("Error de descifrado", resultado.getError());
                    }
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });

            } catch (VigenereApiClient.VigenereApiException e) {
                Platform.runLater(() -> {
                    mostrarError("Error de la API", e.getMessage());
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    mostrarError("Error de conexión",
                            "No se pudo conectar con la API.\n" +
                                    "Asegúrate de que el servidor Python esté corriendo.\n\n" +
                                    "Error: " + e.getMessage());
                    cifrarButton.setDisable(false);
                    descifrarButton.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Guarda el resultado en un archivo
     */
    @FXML
    private void guardarArchivo() {
        String resultado = resultadoTextArea.getText();

        if (resultado.isEmpty()) {
            mostrarAdvertencia("Sin resultado", "No hay ningún resultado para guardar.");
            return;
        }

        // Si hay una ruta especificada en el campo, usarla
        String rutaEspecificada = rutaGuardarTextField.getText().trim();

        if (!rutaEspecificada.isEmpty()) {
            // Guardar directamente en la ruta especificada
            try {
                guardarEnArchivo(rutaEspecificada, resultado);
                mostrarInformacion("Guardado exitoso", "Archivo guardado en:\n" + rutaEspecificada);
                rutaGuardarTextField.clear();
            } catch (IOException e) {
                mostrarError("Error al guardar", "No se pudo guardar el archivo: " + e.getMessage());
            }
        } else {
            // Mostrar diálogo de guardar
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar resultado");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
            );

            // Sugerir un nombre basado en la última ruta cargada
            if (ultimaRutaCargada != null) {
                File archivoOriginal = new File(ultimaRutaCargada);
                String nombre = archivoOriginal.getName();
                String nombreSinExtension = nombre.contains(".")
                        ? nombre.substring(0, nombre.lastIndexOf('.'))
                        : nombre;
                fileChooser.setInitialFileName(nombreSinExtension + "_resultado.txt");
                fileChooser.setInitialDirectory(archivoOriginal.getParentFile());
            } else {
                fileChooser.setInitialFileName("resultado.txt");
            }

            File archivo = fileChooser.showSaveDialog(getStage());
            if (archivo != null) {
                try {
                    guardarEnArchivo(archivo.getAbsolutePath(), resultado);
                    mostrarInformacion("Guardado exitoso", "Archivo guardado correctamente en:\n" + archivo.getName());
                } catch (IOException e) {
                    mostrarError("Error al guardar", "No se pudo guardar el archivo: " + e.getMessage());
                }
            }
        }
    }

    // ========================================================================
    // MÉTODOS DEL MENÚ
    // ========================================================================

    private void cerrarAplicacion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar salida");
        alert.setHeaderText("¿Estás seguro de que quieres salir?");
        alert.setContentText("Se cerrará la aplicación.");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void mostrarManual() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Manual de uso");
        alert.setHeaderText("Cómo usar el Cifrador Vigenère");
        alert.setContentText(
                "1. CLAVE:\n" +
                        "   - Introduce una clave de al menos 3 letras\n" +
                        "   - También puedes cargarla desde un archivo\n\n" +
                        "2. ENTRADA:\n" +
                        "   - Escribe el texto en el área de entrada\n" +
                        "   - O carga un archivo de texto\n\n" +
                        "3. CIFRAR/DESCIFRAR:\n" +
                        "   - Haz clic en 'Cifrar' para cifrar el texto\n" +
                        "   - Haz clic en 'Descifrar' para descifrar\n\n" +
                        "4. GUARDAR:\n" +
                        "   - Introduce una ruta o deja vacío\n" +
                        "   - Haz clic en 'Guardar archivo'\n\n" +
                        "IMPORTANTE: La API de Python debe estar corriendo."
        );
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    private void mostrarAyuda() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayuda");
        alert.setHeaderText("¿Necesitas ayuda?");
        alert.setContentText(
                "INICIAR LA API:\n" +
                        "1. Abre una terminal\n" +
                        "2. Navega a la carpeta del proyecto\n" +
                        "3. Ejecuta: python api_vigenere.py\n" +
                        "4. Verás el mensaje: 'Servidor corriendo en...'\n\n" +
                        "SOLUCIÓN DE PROBLEMAS:\n" +
                        "- Si aparece 'Error de conexión', verifica que\n" +
                        "  el servidor Python esté corriendo\n" +
                        "- Si aparece 'Error de la API', revisa que\n" +
                        "  vigenere.py esté en la misma carpeta\n" +
                        "- La clave debe tener mínimo 3 letras\n\n" +
                        "¿Más dudas? Consulta el README.md"
        );
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Cifrado Vigenère - Hogwarts Encriptado");
        alert.setContentText(
                "Versión: 1.0\n" +
                        "Reto 2 de ETHAZI\n\n" +
                        "DESCRIPCIÓN:\n" +
                        "Programa de encriptación y desencriptación\n" +
                        "usando el cifrado clásico de Vigenère.\n\n" +
                        "TECNOLOGÍAS:\n" +
                        "- Frontend: JavaFX\n" +
                        "- Backend: Python + Flask\n" +
                        "- API REST para comunicación\n\n" +
                        "AUTORES:\n" +
                        "Salca Bachir\n\n" +
                        "Xiker\n\n" +
                        "© 2025 - Todos los derechos reservados"
        );
        alert.getDialogPane().setMinWidth(450);
        alert.showAndWait();
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Lee el contenido de un archivo de texto
     */
    private String leerArchivo(String ruta) throws IOException {
        return new String(Files.readAllBytes(Paths.get(ruta)), StandardCharsets.UTF_8);
    }

    /**
     * Guarda contenido en un archivo
     */
    private void guardarEnArchivo(String ruta, String contenido) throws IOException {
        Files.write(Paths.get(ruta), contenido.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Obtiene el Stage (ventana) actual
     */
    private Stage getStage() {
        return (Stage) entradaTextArea.getScene().getWindow();
    }

    // ========================================================================
    // MÉTODOS PARA MOSTRAR DIÁLOGOS
    // ========================================================================

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}