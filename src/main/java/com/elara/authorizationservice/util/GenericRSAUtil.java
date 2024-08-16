package com.elara.authorizationservice.util;

import com.elara.authorizationservice.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class GenericRSAUtil {

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

    public static String decryptWithPrivateKey(String encrypted, String privateKey) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] message = encrypted.getBytes(StandardCharsets.UTF_8);
            PrivateKey apiPrivateKey= getRSAPrivateKeyFromString(privateKey);
            Cipher rsaCipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            rsaCipher.init(Cipher.DECRYPT_MODE, apiPrivateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(message);
            byte[] decrypted = rsaCipher.doFinal(encryptedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
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
