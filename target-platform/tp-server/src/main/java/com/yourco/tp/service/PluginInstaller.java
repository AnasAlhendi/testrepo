package com.yourco.tp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.util.ZipSafe;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class PluginInstaller {
    private final HomeService home;
    private final DownloadService downloadService;
    private final MavenResolver mavenResolver;
    private final ObjectMapper mapper = new ObjectMapper();

    public PluginInstaller(HomeService home, DownloadService downloadService, MavenResolver mavenResolver) {
        this.home = home;
        this.downloadService = downloadService;
        this.mavenResolver = mavenResolver;
    }

    public void installBundle(PluginSpec spec) {
        Path zip = resolveBundle(spec);
        Path pluginDir = home.home().resolve("plugins").resolve(spec.getId()).resolve("versions").resolve(spec.getVersion() == null ? "unknown" : spec.getVersion());
        try (InputStream in = Files.newInputStream(zip)) {
            ZipSafe.unzip(in, pluginDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to install plugin bundle: " + zip, e);
        }
        // Validate plugin.json exists and minimal schema
        Path manifest = pluginDir.resolve("plugin.json");
        if (!Files.exists(manifest)) {
            throw new RuntimeException("MANIFEST_INVALID: plugin.json missing in " + pluginDir);
        }
        try {
            Map<?,?> json = mapper.readValue(Files.readString(manifest), Map.class);
            require(json, "id");
            require(json, "version");
            require(json, "entry");
            // Optional fields: name, coreRange, permissions, db
        } catch (Exception e) {
            throw new RuntimeException("MANIFEST_INVALID: " + e.getMessage(), e);
        }
    }

    private void require(Map<?,?> json, String key) {
        if (!json.containsKey(key) || String.valueOf(json.get(key)).isBlank())
            throw new IllegalArgumentException("missing key: " + key);
    }

    private Path resolveBundle(PluginSpec spec) {
        if (spec.getSource() == null) throw new IllegalArgumentException("plugin source required");
        if (spec.getSource().getPath() != null && !spec.getSource().getPath().isBlank()) {
            Path p = Path.of(spec.getSource().getPath());
            if (!Files.exists(p)) throw new IllegalArgumentException("plugin path not found: " + p);
            return p;
        }
        if (spec.getSource().getUrl() != null && !spec.getSource().getUrl().isBlank()) {
            String name = spec.getId() + (spec.getVersion() != null ? ("-" + spec.getVersion()) : "") + ".zip";
            return downloadService.downloadUrl(spec.getSource().getUrl(), name, spec.getSource().getSha256());
        }
        if (spec.getSource().getMaven() != null && !spec.getSource().getMaven().isBlank()) {
            // Expect caller to have pre-fetched into downloads (offline mode). Try .zip first.
            String safe = spec.getSource().getMaven().replace(':', '-');
            Path zip = downloadService.downloadsDir().resolve(safe + ".zip");
            if (Files.exists(zip)) return zip;
            return mavenResolver.resolve(spec.getSource().getMaven());
        }
        throw new IllegalArgumentException("Unsupported source for plugin: " + spec.getId());
    }
}
