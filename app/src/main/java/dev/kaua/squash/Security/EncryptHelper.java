package dev.kaua.squash.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import dev.kaua.squash.EncryptDep.StorageKeys;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class EncryptHelper {
    private static final Logger L = LoggerFactory.getLogger(EncryptHelper.class);

    public static String encrypt(String str) {
        try {
            if(str == null) return null;
            // str(utf8) -> bytes -> encrypt -> bytes -> base64(ascii)
            return new String(Base64.getEncoder().encode(encrypt(str.getBytes(StandardCharsets.UTF_8))), StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            //UnsupportedEncodingException...
            if (L.isWarnEnabled()) {
                L.warn("encrypt error:", e);
            }
            return str;
        }
    }

    private static byte[] encrypt(byte[] data) {
        try {
            final Cipher cipher = Cipher.getInstance(StorageKeys.ALGORITHM);
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
            if(str == null) return null;
            else{
                if(!str.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) return str;
                else{
                    if (str.equals("") || str.equals(" ")) return null;
                    else
                        // base64(ascii) -> bytes --> decrypt -> bytes -> str(utf8)
                        return new String(decrypt(Base64.getDecoder()
                                .decode(str.getBytes(StandardCharsets.ISO_8859_1))), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            //UnsupportedEncodingException...
            if (L.isWarnEnabled()) {
                L.warn("decrypt error:", e);
            }
            return str;
        }
    }

    private static byte[] decrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(StorageKeys.ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey());
            return cipher.doFinal(data);
        } catch (Exception e) {
            // GeneralSecurityException
            if (L.isWarnEnabled())
                L.warn("decrypt error:", e);
            return data;
        }
    }

    private static Key getEncryptionKey() {
        try {
            return new SecretKeySpec(MessageDigest.getInstance(StorageKeys.ALGORITHM_DIGEST)
                    .digest(StorageKeys.getKeyBytesSocial()), StorageKeys.ALGORITHM_TAG);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("failed to get encryption key", e);
        }
    }
}