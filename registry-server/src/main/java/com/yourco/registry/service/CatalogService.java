package com.yourco.registry.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CatalogService {
    private final List<Map<String, Object>> plugins = new ArrayList<>();
    private final List<Map<String, Object>> programs = new ArrayList<>();
    private final ObjectMapper mapper;
    private final StorageService storageService;

    public CatalogService(StorageService storageService) {
        this.storageService = storageService;
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.storageService.init();
        // Load existing catalog.json if present
        try {
            var catPath = storageService.root().resolve("catalog.json");
            if (java.nio.file.Files.exists(catPath)) {
                var json = java.nio.file.Files.readString(catPath);
                var cat = mapper.readValue(json, java.util.Map.class);
                var pls = (java.util.List<java.util.Map<String, Object>>) cat.getOrDefault("plugins", java.util.List.of());
                var prs = (java.util.List<java.util.Map<String, Object>>) cat.getOrDefault("programs", java.util.List.of());
                plugins.addAll(pls);
                programs.addAll(prs);
            }
        } catch (Exception ignored) {}
    }

    public List<Map<String, Object>> getPlugins() { return plugins; }
    public List<Map<String, Object>> getPrograms() { return programs; }

    public void addPlugin(Map<String, Object> plugin) { plugins.add(plugin); }
    public void addProgram(Map<String, Object> program) { programs.add(program); }

    public String catalogJson() {
        Map<String, Object> cat = Map.of(
                "plugins", List.copyOf(plugins),
                "programs", List.copyOf(programs)
        );
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cat);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] catalogBytes() { return catalogJson().getBytes(StandardCharsets.UTF_8); }

    public void persist() {
        try {
            var path = storageService.root().resolve("catalog.json");
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.writeString(path, catalogJson(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist catalog", e);
        }
    }
}
