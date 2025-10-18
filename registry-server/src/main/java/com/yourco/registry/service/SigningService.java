package com.yourco.registry.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class SigningService {

    private final String privateKeyPath;
    private volatile PrivateKey privateKey;
    private final StorageService storage;

    public SigningService(@Value("${registry.signing.privateKeyPath:}") String privateKeyPath, StorageService storage) {
        this.privateKeyPath = privateKeyPath;
        this.storage = storage;
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            try { this.privateKey = loadPrivateKey(Path.of(privateKeyPath)); } catch (Exception ignored) {}
        }
    }

    public boolean isConfigured() { return privateKey != null; }

    public String signToBase64(byte[] data) {
        if (privateKey == null) throw new IllegalStateException("signing key not configured");
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(data);
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign catalog", e);
        }
    }

    public void writeSignature(byte[] data) {
        // Always write sha256 alongside signature
        writeSha256(data);
        if (!isConfigured()) return;
        String sig = signToBase64(data);
        try {
            Files.writeString(storage.root().resolve("catalog.sig"), sig, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write signature", e);
        }
    }

    public void writeSha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            Files.writeString(storage.root().resolve("catalog.sha256"), sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write sha256", e);
        }
    }

    private PrivateKey loadPrivateKey(Path path) throws Exception {
        String pem = Files.readString(path, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
