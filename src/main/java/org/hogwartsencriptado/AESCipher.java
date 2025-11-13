package org.hogwartsencriptado;

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

    private static final String ALGORITHM = "AES";
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
        this.secretKey = generateKey(key);
    }

    /**
     * Cifra un texto plano y devuelve el resultado en Base64.
     *
     * @param plainText Texto a cifrar
     * @return Texto cifrado en Base64
     * @throws Exception Si ocurre un error durante el cifrado
     */
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Descifra un texto cifrado en Base64 y devuelve el texto original.
     *
     * @param cipherText Texto cifrado en Base64
     * @return Texto descifrado
     * @throws Exception Si ocurre un error durante el descifrado
     */
    public String decrypt(String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
    }

    /**
     * Genera una clave AES de 128 bits a partir de un String usando SHA-256.
     *
     * @param key Clave proporcionada por el usuario
     * @return SecretKey lista para usar en cifrado AES
     */
    private SecretKeySpec generateKey(String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            keyBytes = Arrays.copyOf(keyBytes, 16); // AES-128
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la clave AES", e);
        }
    }
}
