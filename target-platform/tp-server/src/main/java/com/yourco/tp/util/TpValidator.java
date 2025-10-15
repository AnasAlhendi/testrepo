package com.yourco.tp.util;

import com.yourco.tp.config.TpConfig;
import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;
import com.yourco.tp.model.Source;

public class TpValidator {

    public void validate(TpConfig cfg) {
        if (cfg == null) return;
        if (cfg.getPrograms() != null) cfg.getPrograms().forEach(this::validate);
        if (cfg.getPlugins() != null) cfg.getPlugins().forEach(this::validate);
    }

    public void validate(ProgramSpec spec) {
        if (spec == null) throw new IllegalArgumentException("program is null");
        if (isBlank(spec.getId())) throw new IllegalArgumentException("program.id is required");
        validate(spec.getSource());
    }

    public void validate(PluginSpec spec) {
        if (spec == null) throw new IllegalArgumentException("plugin is null");
        if (isBlank(spec.getId())) throw new IllegalArgumentException("plugin.id is required");
        validate(spec.getSource());
    }

    public void validate(Source source) {
        if (source == null) throw new IllegalArgumentException("source is required");
        int count = 0;
        if (!isBlank(source.getMaven())) count++;
        if (!isBlank(source.getUrl())) count++;
        if (!isBlank(source.getPath())) count++;
        if (count == 0) throw new IllegalArgumentException("source.maven|url|path is required");
        if (count > 1) throw new IllegalArgumentException("source must have exactly one of maven|url|path");
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}

