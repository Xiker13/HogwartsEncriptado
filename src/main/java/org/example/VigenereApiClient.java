

package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Cliente HTTP para comunicarse con la API REST de Vigenère (Python/Flask).
 *
 * Esta clase maneja todas las peticiones HTTP a los endpoints de cifrado y descifrado.
 */
public class VigenereApiClient {

    private static final String BASE_URL = "http://localhost:5000/api";
    private static final int TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;
    private final Gson gson;

    public VigenereApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.gson = new Gson();
    }

    /**
     * Verifica que la API esté funcionando.
     * @return true si la API responde correctamente
     */
    public boolean verificarConexion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error al verificar conexión con la API: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cifra un texto usando el endpoint /api/cifrar
     *
     * @param texto Texto a cifrar
     * @param clave Clave de cifrado
     * @return Resultado con el texto cifrado y metadatos
     * @throws IOException Si hay error de conexión
     * @throws VigenereApiException Si la API devuelve un error
     */
    public ResultadoCifrado cifrarTexto(String texto, String clave)
            throws IOException, VigenereApiException {

        // Crear JSON de petición
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("texto", texto);
        requestBody.addProperty("clave", clave);

        // Enviar petición
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cifrar"))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Parsear respuesta
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

            if (response.statusCode() == 200 && jsonResponse.get("success").getAsBoolean()) {
                return new ResultadoCifrado(
                        true,
                        jsonResponse.get("texto_cifrado").getAsString(),
                        jsonResponse.get("texto_original").getAsString(),
                        jsonResponse.get("clave").getAsString(),
                        null
                );
            } else {
                String error = jsonResponse.has("error")
                        ? jsonResponse.get("error").getAsString()
                        : "Error desconocido";
                throw new VigenereApiException(error);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }

    /**
     * Descifra un texto usando el endpoint /api/descifrar
     *
     * @param textoCifrado Texto cifrado
     * @param clave Clave de descifrado
     * @return Resultado con el texto descifrado
     * @throws IOException Si hay error de conexión
     * @throws VigenereApiException Si la API devuelve un error
     */
    public ResultadoDescifrado descifrarTexto(String textoCifrado, String clave)
            throws IOException, VigenereApiException {

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("texto_cifrado", textoCifrado);
        requestBody.addProperty("clave", clave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/descifrar"))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

            if (response.statusCode() == 200 && jsonResponse.get("success").getAsBoolean()) {
                return new ResultadoDescifrado(
                        true,
                        jsonResponse.get("texto_descifrado").getAsString(),
                        jsonResponse.get("texto_cifrado").getAsString(),
                        jsonResponse.get("clave").getAsString(),
                        null
                );
            } else {
                String error = jsonResponse.has("error")
                        ? jsonResponse.get("error").getAsString()
                        : "Error desconocido";
                throw new VigenereApiException(error);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }

    /**
     * Cifra contenido de archivo usando el endpoint /api/cifrar-archivo
     */
    public ResultadoCifrado cifrarArchivo(String contenido, String clave, String nombreArchivo)
            throws IOException, VigenereApiException {

        JsonObject requestBody = new JsonObject();

        // Codificar contenido en Base64
        String contenidoBase64 = Base64.getEncoder().encodeToString(
                contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        requestBody.addProperty("contenido", contenidoBase64);
        requestBody.addProperty("clave", clave);
        requestBody.addProperty("nombre_archivo", nombreArchivo);
        requestBody.addProperty("es_base64", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cifrar-archivo"))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

            if (response.statusCode() == 200 && jsonResponse.get("success").getAsBoolean()) {
                return new ResultadoCifrado(
                        true,
                        jsonResponse.get("contenido_cifrado").getAsString(),
                        contenido,
                        jsonResponse.get("clave").getAsString(),
                        null
                );
            } else {
                String error = jsonResponse.has("error")
                        ? jsonResponse.get("error").getAsString()
                        : "Error desconocido";
                throw new VigenereApiException(error);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }

    /**
     * Descifra contenido de archivo usando el endpoint /api/descifrar-archivo
     */
    public ResultadoDescifrado descifrarArchivo(String contenidoCifrado, String clave, String nombreArchivo)
            throws IOException, VigenereApiException {

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("contenido_cifrado", contenidoCifrado);
        requestBody.addProperty("clave", clave);
        requestBody.addProperty("nombre_archivo", nombreArchivo);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/descifrar-archivo"))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

            if (response.statusCode() == 200 && jsonResponse.get("success").getAsBoolean()) {
                return new ResultadoDescifrado(
                        true,
                        jsonResponse.get("contenido_descifrado").getAsString(),
                        contenidoCifrado,
                        jsonResponse.get("clave").getAsString(),
                        null
                );
            } else {
                String error = jsonResponse.has("error")
                        ? jsonResponse.get("error").getAsString()
                        : "Error desconocido";
                throw new VigenereApiException(error);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }

    // ========================================================================
    // CLASES DE RESPUESTA
    // ========================================================================

    /**
     * Clase que representa el resultado de una operación de cifrado
     */
    public static class ResultadoCifrado {
        private final boolean exito;
        private final String textoCifrado;
        private final String textoOriginal;
        private final String clave;
        private final String error;

        public ResultadoCifrado(boolean exito, String textoCifrado, String textoOriginal,
                                String clave, String error) {
            this.exito = exito;
            this.textoCifrado = textoCifrado;
            this.textoOriginal = textoOriginal;
            this.clave = clave;
            this.error = error;
        }

        public boolean isExito() { return exito; }
        public String getTextoCifrado() { return textoCifrado; }
        public String getTextoOriginal() { return textoOriginal; }
        public String getClave() { return clave; }
        public String getError() { return error; }
    }

    /**
     * Clase que representa el resultado de una operación de descifrado
     */
    public static class ResultadoDescifrado {
        private final boolean exito;
        private final String textoDescifrado;
        private final String textoCifrado;
        private final String clave;
        private final String error;

        public ResultadoDescifrado(boolean exito, String textoDescifrado, String textoCifrado,
                                   String clave, String error) {
            this.exito = exito;
            this.textoDescifrado = textoDescifrado;
            this.textoCifrado = textoCifrado;
            this.clave = clave;
            this.error = error;
        }

        public boolean isExito() { return exito; }
        public String getTextoDescifrado() { return textoDescifrado; }
        public String getTextoCifrado() { return textoCifrado; }
        public String getClave() { return clave; }
        public String getError() { return error; }
    }

    /**
     * Excepción personalizada para errores de la API
     */
    public static class VigenereApiException extends Exception {
        public VigenereApiException(String message) {
            super(message);
        }
    }
}
