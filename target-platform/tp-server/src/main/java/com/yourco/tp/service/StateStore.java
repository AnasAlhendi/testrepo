
package com.yourco.tp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class StateStore {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path stateDir;

    public StateStore(@Value("${tp.home}") String home) {
        this.stateDir = Path.of(home, "state");
    }

    public void savePrograms(List<ProgramSpec> programs) {
        save("programs.json", programs);
    }

    public void savePlugins(List<PluginSpec> plugins) {
        save("plugins.json", plugins);
    }

    public List<ProgramSpec> loadPrograms() {
        return load("programs.json", new TypeReference<>() {});
    }

    public List<PluginSpec> loadPlugins() {
        return load("plugins.json", new TypeReference<>() {});
    }

    private <T> void save(String name, T data) {
        try {
            Files.createDirectories(stateDir);
            Path file = stateDir.resolve(name);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save state " + name, e);
        }
    }

    private <T> T load(String name, TypeReference<T> type) {
        Path file = stateDir.resolve(name);
        if (!Files.exists(file)) return empty(type);
        try {
            return mapper.readValue(file.toFile(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load state " + name, e);
        }
    }

    private <T> T empty(TypeReference<T> type) {
        if (type.getType().getTypeName().contains("ProgramSpec")) {
            return (T) new ArrayList<ProgramSpec>();
        }
        if (type.getType().getTypeName().contains("PluginSpec")) {
            return (T) new ArrayList<PluginSpec>();
        }
        return null;
    }
}

