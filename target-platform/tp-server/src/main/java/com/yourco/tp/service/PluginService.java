package com.yourco.tp.service;

import com.yourco.tp.model.PluginSpec;
import org.springframework.stereotype.Service;
import com.yourco.tp.util.TpValidator;

import java.util.*;

@Service
public class PluginService {
    private final Map<String, PluginSpec> plugins = new LinkedHashMap<>();
    private final StateStore stateStore;
    private final TpValidator validator = new TpValidator();

    public PluginService(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    public List<PluginSpec> list() { return new ArrayList<>(plugins.values()); }

    public void loadAll(List<PluginSpec> specs) {
        plugins.clear();
        for (PluginSpec s : specs) plugins.put(s.getId(), s);
    }

    public PluginSpec install(PluginSpec spec) {
        if (spec.getId() == null || spec.getId().isBlank()) throw new IllegalArgumentException("id required");
        validator.validate(spec);
        plugins.putIfAbsent(spec.getId(), spec);
        spec = plugins.get(spec.getId());
        if (spec.getStatus() == null || spec.getStatus().isBlank()) spec.setStatus("installed");
        stateStore.savePlugins(list());
        return spec;
    }

    public PluginSpec start(String id) {
        PluginSpec spec = get(id);
        spec.setStatus("started");
        stateStore.savePlugins(list());
        return spec;
    }

    public PluginSpec stop(String id) {
        PluginSpec spec = get(id);
        spec.setStatus("stopped");
        stateStore.savePlugins(list());
        return spec;
    }

    public void uninstall(String id) {
        if (plugins.remove(id) == null) throw new NoSuchElementException("plugin not found: " + id);
        stateStore.savePlugins(list());
    }

    private PluginSpec get(String id) {
        PluginSpec spec = plugins.get(id);
        if (spec == null) throw new NoSuchElementException("plugin not found: " + id);
        return spec;
    }
}
