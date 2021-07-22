package dev.kaua.squash.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class EncryptHelper {

    private static final Logger L = LoggerFactory.getLogger(EncryptHelper.class);

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY_BYTES = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
    };

    public static String encrypt(String str) {
        try {
            // str(utf8) -> bytes -> encrypt -> bytes -> base64(ascii)
            return new String(Base64.getEncoder().encode(encrypt(str.getBytes("UTF-8"))), "ISO-8859-1");
        } catch (Exception e) {
            //UnsupportedEncodingException...
            if (L.isWarnEnabled()) {
                L.warn("encrypt error:", e);
            }
            return str;
        }
    }

    public static byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey());
            return cipher.doFinal(data);
        } catch (Exception e) {
            // GeneralSecurityException
            if (L.isWarnEnabled()) {
                L.warn("encrypt error:", e);
            }
            return data;
        }
    }

    public static String decrypt(String str) {
        try {
            if (str == null || str.equals("") || str.equals(" ")) return null;
            else
                // base64(ascii) -> bytes --> decrypt -> bytes -> str(utf8)
                return new String(decrypt(Base64.getDecoder().decode(str.getBytes("ISO-8859-1"))), "UTF-8");
        } catch (Exception e) {
            //UnsupportedEncodingException...
            if (L.isWarnEnabled()) {
                L.warn("decrypt error:", e);
            }
            return str;
        }
    }

    public static byte[] decrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey());
            return cipher.doFinal(data);
        } catch (Exception e) {
            // GeneralSecurityException
            if (L.isWarnEnabled()) {
                L.warn("decrypt error:", e);
            }
            return data;
        }
    }

    private static Key getEncryptionKey() {
        try {
            return new SecretKeySpec(MessageDigest.getInstance("MD5").digest(KEY_BYTES), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("failed to get encryption key", e);
        }
    }
}