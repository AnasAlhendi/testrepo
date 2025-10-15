package com.yourco.tp.service;

import com.yourco.tp.config.TpConfig;
import com.yourco.tp.config.TpConfigLoader;
import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigWatcher {

    private final TpConfigLoader loader;
    private final ProgramService programService;
    private final PluginService pluginService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Instant lastEvent = Instant.EPOCH;

    public ConfigWatcher(TpConfigLoader loader, ProgramService programService, PluginService pluginService) {
        this.loader = loader;
        this.programService = programService;
        this.pluginService = pluginService;
    }

    @PostConstruct
    public void start() {
        Path cfg = loader.resolvedPath();
        Path dir = cfg.getParent();
        if (dir == null || !Files.exists(dir)) return;

        executor.submit(() -> {
            try (WatchService watch = FileSystems.getDefault().newWatchService()) {
                dir.register(watch, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                while (true) {
                    WatchKey key = watch.take();
                    boolean reload = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = dir.resolve((Path) event.context());
                        if (changed.getFileName().toString().equals(cfg.getFileName().toString())) {
                            reload = true;
                        }
                    }
                    key.reset();
                    if (reload && debounce()) applyConfig();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // swallow, watcher stops
            }
        });
    }

    private boolean debounce() {
        Instant now = Instant.now();
        if (now.isBefore(lastEvent.plusMillis(500))) return false;
        lastEvent = now;
        try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        return true;
    }

    private void applyConfig() {
        TpConfig cfg = loader.loadIfPresent();
        if (cfg == null) return;
        if (cfg.getPrograms() != null) for (ProgramSpec p : cfg.getPrograms()) programService.install(p);
        if (cfg.getPlugins() != null) for (PluginSpec p : cfg.getPlugins()) pluginService.install(p);
        if (cfg.isAutoStart()) {
            programService.list().forEach(p -> { if (!"running".equalsIgnoreCase(p.getStatus())) programService.start(p.getId()); });
            pluginService.list().forEach(p -> { if (!"started".equalsIgnoreCase(p.getStatus())) pluginService.start(p.getId()); });
        }
    }
}

