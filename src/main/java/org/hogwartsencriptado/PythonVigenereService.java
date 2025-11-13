package org.hogwartsencriptado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Servicio que ejecuta el script de Python {@code vigenere.py}.
 *
 * <p>
 * Reglas de uso:
 * <ul>
 *     <li>Si el texto es pequeño, se pasa a Python como argumento de línea.</li>
 *     <li>Si el texto es largo (para evitar el error 206 de Windows), se escribe en un archivo temporal
 *         y se llama a Python en modo "cifrar-archivo" o "descifrar-archivo".</li>
 * </ul>
 * </p>
 *
 * @author Salca
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
     *
     * @param scriptPath Ruta al archivo {@code vigenere.py}
     */
    public PythonVigenereService(String scriptPath) {
        this.scriptPath = scriptPath;
        log.debug("PythonVigenereService inicializado. scriptPath={}", scriptPath);
    }

    /**
     * Procesa texto usando Vigenère (no archivo).
     *
     * @param modo  "cifrar" o "descifrar"
     * @param texto Texto a procesar
     * @param clave Clave de Vigenère
     * @return Resultado con stdout y stderr
     * @throws Exception si falla el proceso de Python
     */
    public PythonResult procesarTexto(String modo, String texto, String clave) throws Exception {
        log.info("procesarTexto() llamado. modo={}, longitudTexto={}, longitudClave={}",
                modo, (texto == null ? 0 : texto.length()), (clave == null ? 0 : clave.length()));

        String comandoSimulado = "python " + scriptPath + " " + modo + " " + texto + " " + clave;
        int longitud = comandoSimulado.length();
        log.debug("Longitud de comando simulado: {} (límite CMD_MAX: {})", longitud, CMD_MAX);

        if (longitud > CMD_MAX) {
            log.info("El comando supera CMD_MAX: se usará ruta por archivo temporal.");
            return procesarTextoMedianteArchivo(modo, texto, clave);
        }

        log.info("El comando cabe en CMD: se usará paso de argumentos directos.");
        return ejecutarTextoPorArgumentos(modo, texto, clave);
    }

    /**
     * Cuando el texto es largo: se escribe en un archivo temporal y se llama
     * al script en modo archivo.
     *
     * @param modo  "cifrar" o "descifrar"
     * @param texto Texto a procesar
     * @param clave Clave de Vigenère
     * @return Resultado con stdout y stderr
     * @throws Exception si falla el proceso de Python
     */
    private PythonResult procesarTextoMedianteArchivo(String modo, String texto, String clave) throws Exception {
        log.debug("procesarTextoMedianteArchivo() → modo={}", modo);

        var tmpIn = Files.createTempFile("scriptum_in_", ".txt");
        Files.writeString(tmpIn, texto, StandardCharsets.UTF_8);
        log.info("Archivo temporal de entrada creado: {}", tmpIn);

        var tmpOut = Files.createTempFile("scriptum_out_", ".txt");
        log.info("Archivo temporal de salida creado: {}", tmpOut);

        String modoArchivo = modo + "-archivo";
        log.debug("Invocando Python en modo de archivo: {}", modoArchivo);
        PythonResult r = procesarArchivo(modoArchivo, tmpIn.toString(), tmpOut.toString(), clave);

        String resultado = Files.readString(tmpOut, StandardCharsets.UTF_8);
        log.debug("Salida leída de archivo temporal. bytes={}", resultado == null ? 0 : resultado.getBytes(StandardCharsets.UTF_8).length);

        try {
            Files.deleteIfExists(tmpIn);
            Files.deleteIfExists(tmpOut);
            log.debug("Temporales eliminados: in={}, out={}", tmpIn, tmpOut);
        } catch (Exception delEx) {
            log.warn("No se pudo eliminar temporales: in={}, out={}, error={}", tmpIn, tmpOut, delEx.toString());
        }

        return new PythonResult(resultado.trim(), r.stderr);
    }

    /**
     * Ejecuta Python pasándole el texto por argumentos (solo si es corto).
     *
     * @param modo  "cifrar" o "descifrar"
     * @param texto Texto a procesar
     * @param clave Clave de Vigenère
     * @return Resultado con stdout y stderr
     * @throws Exception si falla el proceso de Python
     */
    private PythonResult ejecutarTextoPorArgumentos(String modo, String texto, String clave) throws Exception {
        log.debug("ejecutarTextoPorArgumentos() → modo={}", modo);

        ProcessBuilder pb = new ProcessBuilder(
                "python",
                scriptPath,
                modo,
                texto,
                clave
        );
        pb.redirectErrorStream(false);
        log.info("Lanzando proceso Python con argumentos directos. scriptPath={}", scriptPath);
        Process process = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append("\n");
        }

        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) err.append(line).append("\n");
        }

        int exit = process.waitFor();
        log.debug("Proceso Python finalizado. exitCode={}", exit);
        if (exit != 0) {
            log.error("Python devolvió código {}. Stderr:\n{}", exit, err);
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        return new PythonResult(out.toString().trim(), err.toString().trim());
    }

    /**
     * Llama al script en modo archivo.
     *
     * @param modo    "cifrar-archivo" o "descifrar-archivo"
     * @param rutaIn  Ruta del archivo de entrada
     * @param rutaOut Ruta del archivo de salida
     * @param clave   Clave de Vigenère
     * @return Resultado con stdout y stderr
     * @throws Exception si falla el proceso de Python
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
            while ((line = reader.readLine()) != null) out.append(line).append("\n");
        }

        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) err.append(line).append("\n");
        }

        int exit = process.waitFor();
        log.debug("Proceso Python (modo archivo) finalizado. exitCode={}", exit);
        if (exit != 0) {
            log.error("Python devolvió código {}. Stderr:\n{}", exit, err);
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        return new PythonResult(out.toString().trim(), err.toString().trim());
    }

    /**
     * Objeto de retorno: salida normal y avisos.
     */
    public static class PythonResult {
        /** Salida estándar del proceso Python (resultado Vigenère) */
        public final String stdout;
        /** Salida de error del proceso Python (avisos/errores) */
        public final String stderr;

        /**
         * Constructor de resultado Python.
         *
         * @param stdout Salida estándar
         * @param stderr Salida de error
         */
        public PythonResult(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
