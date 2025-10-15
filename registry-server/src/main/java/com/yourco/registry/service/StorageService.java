package com.yourco.registry.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
public class StorageService {
    private final Path root;

    public StorageService(@Value("${registry.storage.dir}") String rootDir) {
        this.root = Path.of(rootDir);
    }

    public Path root() { return root; }

    public void init() {
        try {
            Files.createDirectories(root);
            Files.createDirectories(root.resolve("plugins"));
            Files.createDirectories(root.resolve("programs"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    public Path storePlugin(String id, String version, MultipartFile file) {
        if (id == null || id.isBlank() || version == null || version.isBlank())
            throw new IllegalArgumentException("id and version required");
        Path dir = root.resolve(Path.of("plugins", id, "versions", version));
        try {
            Files.createDirectories(dir);
            String original = sanitizedFilename(file.getOriginalFilename(), Map.of("default", "plugin.zip"));
            Path dest = dir.resolve(original);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return dest;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store plugin", e);
        }
    }

    public Path storeProgram(String id, String version, MultipartFile file) {
        if (id == null || id.isBlank() || version == null || version.isBlank())
            throw new IllegalArgumentException("id and version required");
        Path dir = root.resolve(Path.of("programs", id, "versions", version));
        try {
            Files.createDirectories(dir);
            String original = sanitizedFilename(file.getOriginalFilename(), Map.of("default", "program.jar"));
            Path dest = dir.resolve(original);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return dest;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store program", e);
        }
    }

    private String sanitizedFilename(String name, Map<String, String> fallback) {
        if (name == null || name.isBlank()) return fallback.get("default");
        // prevent path traversal
        name = name.replace("\\", "/");
        int idx = name.lastIndexOf('/');
        if (idx >= 0) name = name.substring(idx + 1);
        return name;
    }
}

