package com.yourco.tp.registry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PublicKeyLoader {
    private final String publicKeyPath;
    private volatile PublicKey publicKey;

    public PublicKeyLoader(@Value("${tp.keys.publicKeyPath:}") String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
        if (publicKeyPath != null && !publicKeyPath.isBlank()) {
            try { this.publicKey = load(Path.of(publicKeyPath)); } catch (Exception ignored) {}
        }
    }

    public boolean isConfigured() { return publicKey != null; }

    public PublicKey get() { return publicKey; }

    private PublicKey load(Path path) throws Exception {
        String pem = Files.readString(path, StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}

