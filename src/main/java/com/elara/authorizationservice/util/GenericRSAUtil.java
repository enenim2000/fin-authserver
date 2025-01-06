package com.elara.authorizationservice.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
public class GenericRSAUtil {

    private static  final List<String> transformations = new ArrayList<>(){{
        add("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        add("RSA/None/PKCS1Padding");
    }};

    public static void generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PrivateKey privateKey;
        PublicKey publicKey;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
        System.out.println("private-key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        System.out.println("public-key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }

    public String encryptWithPublicKey(String clearText, String publicKey) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] message = clearText.getBytes(StandardCharsets.UTF_8);
        PublicKey apiPublicKey= getRSAPublicKeyFromString(publicKey);
        Cipher rsaCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
        rsaCipher.init(Cipher.ENCRYPT_MODE, apiPublicKey);
        byte[] encrypted = rsaCipher.doFinal(message);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decryptWithPrivateKey(String encrypted, String privateKey, String transformation) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] message = encrypted.getBytes(StandardCharsets.UTF_8);
        PrivateKey apiPrivateKey= getRSAPrivateKeyFromString(privateKey);
        Cipher rsaCipher = Cipher.getInstance(transformation, "BC");
        rsaCipher.init(Cipher.DECRYPT_MODE, apiPrivateKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(message);
        byte[] decrypted = rsaCipher.doFinal(encryptedBytes);
        return new String(decrypted);
    }

    public static String decryptWithPrivateKey(String encrypted, String privateKey) {
        try {
            return decryptWithPrivateKey(encrypted, privateKey, transformations.get(0));
        } catch (Exception ex) {
            log.info("Decryption error message({}) for transformation ({}), will now try the next transformation ({})", ex.getMessage(), transformations.get(0), transformations.get(1));
            try {
                return decryptWithPrivateKey(encrypted, privateKey, transformations.get(1));
            } catch (Exception exx) {
                log.info("Decryption error after second transformation {} was used", transformations.get(1));
                log.error("Decryption error root cause: ", ex);
            }
        }
        return null;
    }

    private PublicKey getRSAPublicKeyFromString(String publicKey) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(x509KeySpec);
    }

    private static PrivateKey getRSAPrivateKeyFromString(String base64PrivateKey) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PrivateKey privateKey;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes(StandardCharsets.UTF_8)));
        KeyFactory keyFactory;
        keyFactory = KeyFactory.getInstance("RSA", "BC");
        privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
}
