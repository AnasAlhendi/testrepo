package com.yourco.app.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PluginsController {

    @GetMapping("/plugins")
    public ResponseEntity<List<Map<String, Object>>> list() {
        Path dir = pluginsDir();
        try {
            if (!Files.exists(dir)) return ResponseEntity.ok(List.of());
            var list = Files.list(dir)
                    .filter(Files::isDirectory)
                    .map(p -> Map.<String, Object>of(
                            "id", p.getFileName().toString(),
                            "path", p.toString()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    private Path pluginsDir() {
        String appData = System.getProperty("APPDATA_DIR", System.getenv().getOrDefault("APPDATA_DIR", System.getProperty("user.home") + "/.local/share/YourApp"));
        String plugins = System.getProperty("pf4j.pluginsDir", System.getenv().getOrDefault("PF4J_PLUGINS_DIR", appData + "/plugins"));
        return Path.of(plugins);
    }
}

