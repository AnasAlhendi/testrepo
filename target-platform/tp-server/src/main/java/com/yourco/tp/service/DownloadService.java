package com.yourco.tp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

@Service
public class DownloadService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final HomeService homeService;

    public DownloadService(HomeService homeService) {
        this.homeService = homeService;
    }

    public Path downloadsDir() { return homeService.home().resolve("downloads"); }

    public Path downloadUrl(String url, String filename) {
        try {
            Files.createDirectories(downloadsDir());
            Path dest = downloadsDir().resolve(filename);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("Failed to download [" + resp.statusCode() + "]: " + url);
            }
            try (InputStream in = resp.body()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return dest;
        } catch (Exception e) {
            throw new RuntimeException("Download failed: " + url, e);
        }
    }

    public Path downloadUrl(String url, String filename, String expectedSha256LowerHex) {
        Path p = downloadUrl(url, filename);
        if (expectedSha256LowerHex != null && !expectedSha256LowerHex.isBlank()) {
            String actual = sha256Hex(p);
            if (!actual.equalsIgnoreCase(expectedSha256LowerHex)) {
                throw new RuntimeException("DL_CHECKSUM_MISMATCH: expected=" + expectedSha256LowerHex + " actual=" + actual);
            }
        }
        return p;
    }

    public String sha256Hex(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(file)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) > 0) md.update(buf, 0, r);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute sha256: " + file, e);
        }
    }

    public Path copyFromPath(Path src) {
        try {
            Files.createDirectories(downloadsDir());
            Path dest = downloadsDir().resolve(src.getFileName());
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            return dest;
        } catch (IOException e) {
            throw new RuntimeException("Copy failed: " + src, e);
        }
    }
}

