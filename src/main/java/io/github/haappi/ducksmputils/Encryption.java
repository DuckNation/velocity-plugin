package io.github.haappi.ducksmputils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Encryption {

    public Encryption() {
        throw new RuntimeException("Unable to load a static class.");
    }

    public static String encrypt(final String strToEncrypt) {
        setKey();
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: ");
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(final String strToEncrypt, String default_) {
        String encrypted = encrypt(strToEncrypt);
        if (encrypted == null) {
            return default_;
        }
        return encrypted;
    }

    public static SecretKeySpec setKey() {
        MessageDigest sha;
        final String myKey = DuckSMPUtils.getInstance().getEncryptionKey();
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            secretKey = keySpec;
            return keySpec;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static SecretKeySpec secretKey = setKey();


}