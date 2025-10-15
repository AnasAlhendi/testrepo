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

