package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Servicio que ejecuta el script de Python vigenere.py.
 *
 * IMPORTANTE:
 * - Si el texto es pequeño, se lo pasa a Python como argumento.
 * - Si el texto es grande (para no disparar el error 206 de Windows),
 *   lo escribe en un archivo temporal y llama a Python en modo
 *   "cifrar-archivo" o "descifrar-archivo".
 *
 * Así cumplimos el documento de requisitos: se controla antes
 * que la línea de comandos sea demasiado larga.
 */
public class PythonVigenereService {

    /** Ruta al script vigenere.py dentro del proyecto */
    private final String scriptPath;

    /** Límite seguro de caracteres para Windows (cmd ~8191) */
    private static final int CMD_MAX = 8000;

    public PythonVigenereService(String scriptPath) {
        this.scriptPath = scriptPath;
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
        // ¿cabe en la línea de comandos?
        String comandoSimulado = "python " + scriptPath + " " + modo + " " + texto + " " + clave;
        if (comandoSimulado.length() > CMD_MAX) {
            // demasiado largo → lo pasamos por archivo
            return procesarTextoMedianteArchivo(modo, texto, clave);
        }
        // cabe → lo mandamos normal
        return ejecutarTextoPorArgumentos(modo, texto, clave);
    }

    /**
     * Cuando el texto es largo: lo escribimos en un archivo temporal y llamamos
     * al script en modo archivo.
     */
    private PythonResult procesarTextoMedianteArchivo(String modo, String texto, String clave) throws Exception {
        // 1. crear archivo temporal de entrada
        var tmpIn = Files.createTempFile("scriptum_in_", ".txt");
        Files.writeString(tmpIn, texto, StandardCharsets.UTF_8);

        // 2. crear archivo temporal de salida
        var tmpOut = Files.createTempFile("scriptum_out_", ".txt");

        // 3. modo de python: "cifrar-archivo" o "descifrar-archivo"
        String modoArchivo = modo + "-archivo";
        PythonResult r = procesarArchivo(modoArchivo, tmpIn.toString(), tmpOut.toString(), clave);

        // 4. leer lo que generó python
        String resultado = Files.readString(tmpOut, StandardCharsets.UTF_8);

        // 5. borrar temporales (requisito: no dejar archivos expuestos)
        Files.deleteIfExists(tmpIn);
        Files.deleteIfExists(tmpOut);

        return new PythonResult(resultado.trim(), r.stderr);
    }

    /**
     * Ejecuta Python pasándole el texto por argumentos (solo si es corto).
     */
    private PythonResult ejecutarTextoPorArgumentos(String modo, String texto, String clave) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "python",     // pon "py" si en tu PC se llama así
                scriptPath,
                modo,
                texto,
                clave
        );
        pb.redirectErrorStream(false);
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
        if (exit != 0) {
            // aquí ya no te saldrá el 206 porque lo controlamos antes,
            // pero por si Python falla de verdad:
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        return new PythonResult(out.toString().trim(), err.toString().trim());
    }

    /**
     * Llama al script en modo archivo.
     */
    public PythonResult procesarArchivo(String modo, String rutaIn, String rutaOut, String clave) throws Exception {
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
        if (exit != 0) {
            throw new RuntimeException("Python devolvió código " + exit + ":\n" + err);
        }

        return new PythonResult(out.toString().trim(), err.toString().trim());
    }

    /**
     * Objeto de retorno: salida normal y avisos.
     */
    public static class PythonResult {
        public final String stdout;
        public final String stderr;

        public PythonResult(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
