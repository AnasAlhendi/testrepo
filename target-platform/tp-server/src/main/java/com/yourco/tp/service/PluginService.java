package com.yourco.tp.service;

import com.yourco.tp.model.PluginSpec;
import org.springframework.stereotype.Service;
import com.yourco.tp.util.TpValidator;

import java.util.*;

@Service
public class PluginService {
    private final Map<String, PluginSpec> plugins = new LinkedHashMap<>();
    private final StateStore stateStore;
    private final AuditLogService audit;
    private final TpValidator validator = new TpValidator();
    private final PluginInstaller installer;

    public PluginService(StateStore stateStore, AuditLogService audit, PluginInstaller installer) {
        this.stateStore = stateStore;
        this.audit = audit;
        this.installer = installer;
    }

    public List<PluginSpec> list() { return new ArrayList<>(plugins.values()); }

    public void loadAll(List<PluginSpec> specs) {
        plugins.clear();
        for (PluginSpec s : specs) plugins.put(s.getId(), s);
    }

    public PluginSpec install(PluginSpec spec) {
        if (spec.getId() == null || spec.getId().isBlank()) throw new IllegalArgumentException("id required");
        validator.validate(spec);
        // install bundle to TP_HOME/plugins if source provided
        try { installer.installBundle(spec); } catch (RuntimeException e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
        plugins.putIfAbsent(spec.getId(), spec);
        spec = plugins.get(spec.getId());
        if (spec.getStatus() == null || spec.getStatus().isBlank()) spec.setStatus("installed");
        stateStore.savePlugins(list());
        audit.plugin("INSTALL", spec.getId(), Map.of());
        return spec;
    }

    public PluginSpec start(String id) {
        PluginSpec spec = get(id);
        spec.setStatus("started");
        stateStore.savePlugins(list());
        audit.plugin("START", id, Map.of());
        return spec;
    }

    public PluginSpec stop(String id) {
        PluginSpec spec = get(id);
        spec.setStatus("stopped");
        stateStore.savePlugins(list());
        audit.plugin("STOP", id, Map.of());
        return spec;
    }

    public void uninstall(String id) {
        if (plugins.remove(id) == null) throw new NoSuchElementException("plugin not found: " + id);
        stateStore.savePlugins(list());
        audit.plugin("UNINSTALL", id, Map.of());
    }

    public PluginSpec update(PluginSpec spec) {
        if (spec.getId() == null || spec.getId().isBlank()) throw new IllegalArgumentException("id required");
        validator.validate(spec);
        plugins.put(spec.getId(), spec);
        if (spec.getStatus() == null || spec.getStatus().isBlank()) spec.setStatus("installed");
        stateStore.savePlugins(list());
        audit.plugin("UPDATE", spec.getId(), Map.of());
        return spec;
    }

    private PluginSpec get(String id) {
        PluginSpec spec = plugins.get(id);
        if (spec == null) throw new NoSuchElementException("plugin not found: " + id);
        return spec;
    }
}
