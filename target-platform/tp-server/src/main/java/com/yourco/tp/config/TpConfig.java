package com.yourco.tp.config;

import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;

import java.util.ArrayList;
import java.util.List;

public class TpConfig {
    private String tpHome; // supports `tpHome` from external YAML
    private String tpHomeDir; // alias if needed
    private boolean autoStart = true;
    private List<PluginSpec> plugins = new ArrayList<>();
    private List<ProgramSpec> programs = new ArrayList<>();

    public String getTpHome() { return tpHome != null ? tpHome : tpHomeDir; }
    public void setTpHome(String tpHome) { this.tpHome = tpHome; }
    public String getTpHomeDir() { return tpHomeDir; }
    public void setTpHomeDir(String tpHomeDir) { this.tpHomeDir = tpHomeDir; }
    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    public List<PluginSpec> getPlugins() { return plugins; }
    public void setPlugins(List<PluginSpec> plugins) { this.plugins = plugins; }
    public List<ProgramSpec> getPrograms() { return programs; }
    public void setPrograms(List<ProgramSpec> programs) { this.programs = programs; }
}

