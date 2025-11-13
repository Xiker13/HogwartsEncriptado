package org.hogwartsencriptado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Clase que implementa cifrado y descifrado AES.
 * <p>
 * Permite trabajar con texto plano y generar resultados codificados en Base64.
 * También puede manejar claves proporcionadas por el usuario.
 * </p>
 */
public class AESCipher {

    /** Logger de la clase para registrar operaciones y errores */
    private static final Logger log = LoggerFactory.getLogger(AESCipher.class);

    /** Nombre del algoritmo de cifrado */
    private static final String ALGORITHM = "AES";

    /** Clave secreta generada a partir del texto del usuario */
    private final SecretKeySpec secretKey;

    /**
     * Constructor que genera una clave AES a partir de un String.
     * <p>
     * Si la longitud de la clave es menor a 16 bytes, se rellena automáticamente.
     * </p>
     *
     * @param key Clave proporcionada por el usuario
     */
    public AESCipher(String key) {
        log.debug("Inicializando AESCipher con clave proporcionada (longitud={} caracteres).", key.length());
        this.secretKey = generateKey(key);
        log.info("Clave AES generada correctamente (algoritmo={}).", ALGORITHM);
    }

    /**
     * Cifra un texto plano y devuelve el resultado en Base64.
     *
     * @param plainText Texto a cifrar
     * @return Texto cifrado en Base64
     * @throws Exception Si ocurre un error durante el cifrado
     */
    public String encrypt(String plainText) throws Exception {
        log.info("Iniciando cifrado AES...");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String resultado = Base64.getEncoder().encodeToString(encryptedBytes);
            log.debug("Cifrado completado (longitud salida={} caracteres).", resultado.length());
            return resultado;
        } catch (Exception e) {
            log.error("Error durante el proceso de cifrado AES: {}", e.toString());
            throw e;
        }
    }

    /**
     * Descifra un texto cifrado en Base64 y devuelve el texto original.
     *
     * @param cipherText Texto cifrado en Base64
     * @return Texto descifrado
     * @throws Exception Si ocurre un error durante el descifrado
     */
    public String decrypt(String cipherText) throws Exception {
        log.info("Iniciando descifrado AES...");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            String resultado = new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
            log.debug("Descifrado completado (longitud salida={} caracteres).", resultado.length());
            return resultado;
        } catch (Exception e) {
            log.error("Error durante el proceso de descifrado AES: {}", e.toString());
            throw e;
        }
    }

    /**
     * Genera una clave AES de 128 bits a partir de un String usando SHA-256.
     *
     * @param key Clave proporcionada por el usuario
     * @return SecretKey lista para usar en cifrado AES
     */
    private SecretKeySpec generateKey(String key) {
        log.trace("Generando clave AES a partir del texto de usuario...");
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            keyBytes = Arrays.copyOf(keyBytes, 16); // AES-128
            log.debug("Clave derivada con SHA-256 truncada a 128 bits.");
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            log.error("Error al generar la clave AES: {}", e.toString());
            throw new RuntimeException("Error al generar la clave AES", e);
        }
    }
}
