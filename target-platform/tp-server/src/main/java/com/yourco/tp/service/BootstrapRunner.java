package com.yourco.tp.service;

import com.yourco.tp.config.TpConfig;
import com.yourco.tp.config.TpConfigLoader;
import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BootstrapRunner implements ApplicationRunner {

    private final TpConfigLoader loader;
    private final ProgramService programService;
    private final PluginService pluginService;
    private final StateStore stateStore;
    private final com.yourco.tp.registry.RegistryClient registryClient;
    private final HomeService homeService;

    @org.springframework.beans.factory.annotation.Value("${tp.registry.url:}")
    private String registryUrl;
    @org.springframework.beans.factory.annotation.Value("${tp.registry.autoSyncOnStart:false}")
    private boolean autoSyncOnStart;

    public BootstrapRunner(TpConfigLoader loader,
                           ProgramService programService,
                           PluginService pluginService,
                           StateStore stateStore,
                           com.yourco.tp.registry.RegistryClient registryClient,
                           HomeService homeService) {
        this.loader = loader;
        this.programService = programService;
        this.pluginService = pluginService;
        this.stateStore = stateStore;
        this.registryClient = registryClient;
        this.homeService = homeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Ensure directory structure
        homeService.ensureStructure();
        // 1) restore state if present
        List<ProgramSpec> savedPrograms = stateStore.loadPrograms();
        List<PluginSpec> savedPlugins = stateStore.loadPlugins();
        if (!savedPrograms.isEmpty()) programService.loadAll(savedPrograms);
        if (!savedPlugins.isEmpty()) pluginService.loadAll(savedPlugins);

        // 2) load config (if present) and merge
        TpConfig cfg = loader.loadIfPresent();
        if (cfg != null) {
            if (cfg.getPrograms() != null) {
                for (ProgramSpec p : cfg.getPrograms()) programService.install(p);
            }
            if (cfg.getPlugins() != null) {
                for (PluginSpec p : cfg.getPlugins()) pluginService.install(p);
            }
            if (cfg.isAutoStart()) {
                // Start installed items
                programService.list().forEach(p -> {
                    if (!"running".equalsIgnoreCase(p.getStatus())) programService.start(p.getId());
                });
                pluginService.list().forEach(p -> {
                    if (!"started".equalsIgnoreCase(p.getStatus())) pluginService.start(p.getId());
                });
            }
        }

        // 3) registry auto-sync if configured
        try {
            if ((registryUrl != null && !registryUrl.isBlank() && autoSyncOnStart)) {
                var cat = registryClient.fetchAndVerify();
                cat.getPrograms().forEach(programService::install);
                cat.getPlugins().forEach(pluginService::install);
            }
        } catch (Exception ignored) { }
    }
}
