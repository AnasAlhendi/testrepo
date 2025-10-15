package com.yourco.tp.config;

import com.yourco.tp.util.TpValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TpConfigLoader {

    private final String home;
    private final String configPath;
    private final TpValidator validator = new TpValidator();

    public TpConfigLoader(
            @Value("${tp.home}") String home,
            @Value("${tp.configPath}") String configPath) {
        this.home = home;
        this.configPath = configPath;
    }

    public String getHome() { return home; }
    public String getConfigPath() { return configPath; }

    public Path resolvedPath() {
        return Path.of(configPath.replace("${tp.home}", home));
    }

    public TpConfig loadIfPresent() {
        Path path = resolvedPath();
        if (!Files.exists(path)) {
            return null;
        }
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(path)) {
            TpConfig cfg = yaml.loadAs(in, TpConfig.class);
            validator.validate(cfg);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read TP config: " + path, e);
        }
    }
}
