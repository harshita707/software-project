package com.harshita.imagestenographyapp.algorithms;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESencryption {

    public static String encryptText(String inputText, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(inputText.getBytes());
        String outputText = Base64.encodeToString(encVal, Base64.DEFAULT);
        return outputText;
    }

    private static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key =digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public static String decryptText(String inputText, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(inputText,Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        String outputText = new String(decValue);
        return outputText;
    }

   // public static String

}
