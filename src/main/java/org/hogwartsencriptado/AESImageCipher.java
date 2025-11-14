package org.hogwartsencriptado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Clase que implementa cifrado y descifrado AES para imágenes.
 * <p>
 * La imagen se representa como un array de bytes y se cifra/descifra utilizando
 * AES-128 en modo ECB (sin IV) para mantener la simplicidad.
 * La clave se deriva a partir de un String proporcionado por el usuario usando SHA-256.
 * </p>
 * <p>
 * Uso típico:
 * <pre>
 * AESImageCipher aesImage = new AESImageCipher("miClaveSecreta");
 * byte[] cifrada = aesImage.encrypt(imagenBytes);
 * byte[] original = aesImage.decrypt(cifrada);
 * </pre>
 * </p>
 * <p>
 * Autor: Xiker
 * </p>
 */
public class AESImageCipher {

    private static final Logger log = LoggerFactory.getLogger(AESImageCipher.class);
    private static final String ALGORITHM = "AES";

    private final SecretKeySpec secretKey;

    /**
     * Constructor que genera la clave AES a partir de un String.
     * <p>
     * La clave se deriva usando SHA-256 y se recorta a 128 bits (16 bytes).
     * </p>
     *
     * @param key Clave proporcionada por el usuario
     */
    public AESImageCipher(String key) {
        log.debug("Inicializando AESImageCipher con clave proporcionada (longitud={} caracteres).", key.length());
        this.secretKey = generateKey(key);
        log.info("Clave AES generada correctamente para imágenes (algoritmo={}).", ALGORITHM);
    }

    /**
     * Cifra un array de bytes (imagen) usando AES.
     *
     * @param data Bytes de la imagen
     * @return Imagen cifrada en bytes
     * @throws Exception Si ocurre un error durante el cifrado
     */
    public byte[] encrypt(byte[] data) throws Exception {
        log.info("Iniciando cifrado AES de imagen...");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data);
            log.debug("Cifrado completado (longitud salida={} bytes).", encryptedBytes.length);
            return encryptedBytes;
        } catch (Exception e) {
            log.error("Error durante el cifrado AES de imagen: {}", e.toString());
            throw e;
        }
    }

    /**
     * Descifra un array de bytes (imagen) usando AES.
     *
     * @param data Bytes cifrados de la imagen
     * @return Imagen descifrada en bytes
     * @throws Exception Si ocurre un error durante el descifrado
     */
    public byte[] decrypt(byte[] data) throws Exception {
        log.info("Iniciando descifrado AES de imagen...");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(data);
            log.debug("Descifrado completado (longitud salida={} bytes).", decryptedBytes.length);
            return decryptedBytes;
        } catch (Exception e) {
            log.error("Error durante el descifrado AES de imagen: {}", e.toString());
            throw e;
        }
    }

    /**
     * Genera la clave AES de 128 bits a partir del String proporcionado por el usuario.
     * <p>
     * La clave se deriva mediante SHA-256 y se recorta a 16 bytes (128 bits)
     * para cumplir con AES-128.
     * </p>
     *
     * @param key Clave de usuario
     * @return SecretKeySpec derivada de la clave
     * @throws RuntimeException Si ocurre un error al generar la clave
     */
    private SecretKeySpec generateKey(String key) {
        log.trace("Generando clave AES para imagen a partir del texto de usuario...");
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            keyBytes = Arrays.copyOf(keyBytes, 16); // AES-128
            log.debug("Clave derivada con SHA-256 truncada a 128 bits.");
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            log.error("Error al generar la clave AES para imagen: {}", e.toString());
            throw new RuntimeException("Error al generar la clave AES para imagen", e);
        }
    }
}
