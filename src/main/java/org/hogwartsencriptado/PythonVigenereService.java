package org.hogwartsencriptado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Servicio que ejecuta el script de Python vigenere.py.
 *
 * Reglas de uso:
 * - Si el texto es pequeño, se pasa a Python como argumento de línea.
 * - Si es grande (para evitar el error 206 de Windows), se escribe en un archivo temporal
 *   y se llama a Python en modo "cifrar-archivo" o "descifrar-archivo".
 *
 * No se cambia la lógica original: solo se añade trazado de logging.
 */
public class PythonVigenereService {

    /** Logger de la clase */
    private static final Logger log = LoggerFactory.getLogger(PythonVigenereService.class);

    /** Ruta al script vigenere.py dentro del proyecto */
    private final String scriptPath;

    /** Límite seguro de caracteres para Windows (cmd ~8191) */
    private static final int CMD_MAX = 8000;

    /**
     * Crea el servicio indicando la ruta del script Python.
     * @param scriptPath ruta al archivo vigenere.py
     */
    public PythonVigenereService(String scriptPath) {
        this.scriptPath = scriptPath;
        log.debug("PythonVigenereService inicializado. scriptPath={}", scriptPath);
    }

    /**
     * Procesa TEXTO (no archivo).
     * @param modo  "cifrar" o "descifrar"
     * @param texto texto a procesar
     * @param clave clave de Vigenère
     * @return resultado con stdout y stderr
     * @throws Exception si falla el proceso de Python
     */
    public PythonResult procesarTexto(String modo, String texto, String clave) throws Exception {
        log.info("procesarTexto() llamado. modo={}, longitudTexto={}, longitudClave={}",
                modo, (texto == null ? 0 : texto.length()), (clave == null ? 0 : clave.length()));

        // ¿cabe en la línea de comandos?
        String comandoSimulado = "python " + scriptPath + " " + modo + " " + texto + " " + clave;
        int longitud = comandoSimulado.length();
        log.debug("Longitud de comando simulado: {} (límite CMD_MAX: {})", longitud, CMD_MAX);

        if (longitud > CMD_MAX) {
            log.info("El comando supera CMD_MAX: se usará ruta por archivo temporal.");
            // demasiado largo → lo pasamos por archivo
            return procesarTextoMedianteArchivo(modo, texto, clave);
        }

        // cabe → lo mandamos normal
        log.info("El comando cabe en CMD: se usará paso de argumentos directos.");
        return ejecutarTextoPorArgumentos(modo, texto, clave);
    }

    /**
     * Cuando el texto es largo: lo escribimos en un archivo temporal y llamamos
     * al script en modo archivo.
     */
    private PythonResult procesarTextoMedianteArchivo(String modo, String texto, String clave) throws Exception {
        log.debug("procesarTextoMedianteArchivo() → modo={}", modo);

        // 1. crear archivo temporal de entrada
        var tmpIn = Files.createTempFile("scriptum_in_", ".txt");
        Files.writeString(tmpIn, texto, StandardCharsets.UTF_8);
        log.info("Archivo temporal de entrada creado: {}", tmpIn);

        // 2. crear archivo temporal de salida
        var tmpOut = Files.createTempFile("scriptum_out_", ".txt");
        log.info("Archivo temporal de salida creado: {}", tmpOut);

        // 3. modo de python: "cifrar-archivo" o "descifrar-archivo"
        String modoArchivo = modo + "-archivo";
        log.debug("Invocando Python en modo de archivo: {}", modoArchivo);
        PythonResult r = procesarArchivo(modoArchivo, tmpIn.toString(), tmpOut.toString(), clave);

        // 4. leer lo que generó python
        String resultado = Files.readString(tmpOut, StandardCharsets.UTF_8);
        log.debug("Salida leída de archivo temporal. bytes={}", resultado == null ? 0 : resultado.getBytes(StandardCharsets.UTF_8).length);

        // 5. borrar temporales (requisito: no dejar archivos expuestos)
        try {
            Files.deleteIfExists(tmpIn);
            Files.deleteIfExists(tmpOut);
            log.debug("Temporales eliminados: in={}, out={}", tmpIn, tmpOut);
        } catch (Exception delEx) {
            // No se relanza: solo avisamos. El flujo original se mantiene.
            log.warn("No se pudo eliminar temporales: in={}, out={}, error={}", tmpIn, tmpOut, delEx.toString());
        }

        return new PythonResult(resultado.trim(), r.stderr);
    }

    /**
     * Ejecuta Python pasándole el texto por argumentos (solo si es corto).
     */
    private PythonResult ejecutarTextoPorArgumentos(String modo, String texto, String clave) throws Exception {
        log.debug("ejecutarTextoPorArgumentos() → modo={}", modo);

        ProcessBuilder pb = new ProcessBuilder(
                "python",     // pon "py" si en tu PC se llama así
                scriptPath,
                modo,
                texto,
                clave
        );
        pb.redirectErrorStream(false);
        log.info("Lanzando proceso Python con argumentos directos. scriptPath={}", scriptPath);
        Process process = pb.start();

        // stdout
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        // stderr
        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                err.append(line).append("\n");
            }
        }

        int exit = process.waitFor();
        log.debug("Proceso Python finalizado. exitCode={}", exit);
        if (exit != 0) {
            log.error("Python devolvió código {}. Stderr:\n{}", exit, err);
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        String stdout = out.toString().trim();
        String stderr = err.toString().trim();
        log.debug("stdout.len={}, stderr.len={}", stdout.length(), stderr.length());
        return new PythonResult(stdout, stderr);
    }

    /**
     * Llama al script en modo archivo.
     */
    public PythonResult procesarArchivo(String modo, String rutaIn, String rutaOut, String clave) throws Exception {
        log.info("procesarArchivo() → modo={}, rutaIn={}, rutaOut={}", modo, rutaIn, rutaOut);

        ProcessBuilder pb = new ProcessBuilder(
                "python",
                scriptPath,
                modo,
                rutaIn,
                rutaOut,
                clave
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                err.append(line).append("\n");
            }
        }

        int exit = process.waitFor();
        log.debug("Proceso Python (modo archivo) finalizado. exitCode={}", exit);
        if (exit != 0) {
            log.error("Python devolvió código {}. Stderr:\n{}", exit, err);
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        String stdout = out.toString().trim();
        String stderr = err.toString().trim();
        log.debug("stdout.len={}, stderr.len={}", stdout.length(), stderr.length());
        return new PythonResult(stdout, stderr);
    }

    /**
     * Objeto de retorno: salida normal y avisos.
     */
    public static class PythonResult {
        /** Salida estándar del proceso Python (resultado Vigenère) */
        public final String stdout;
        /** Salida de error del proceso Python (avisos/errores) */
        public final String stderr;

        public PythonResult(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
