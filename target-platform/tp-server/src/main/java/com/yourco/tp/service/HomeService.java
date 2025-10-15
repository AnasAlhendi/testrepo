package com.yourco.tp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class HomeService {
    private final Path home;

    public HomeService(@Value("${tp.home}") String home) {
        this.home = Path.of(home);
    }

    public Path home() { return home; }

    public void ensureStructure() {
        try {
            Files.createDirectories(home.resolve("config"));
            Files.createDirectories(home.resolve("plugins"));
            Files.createDirectories(home.resolve("programs"));
            Files.createDirectories(home.resolve("downloads"));
            Files.createDirectories(home.resolve("logs"));
            Files.createDirectories(home.resolve("keys"));
            Files.createDirectories(home.resolve("state"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to ensure TP_HOME structure", e);
        }
    }
}

