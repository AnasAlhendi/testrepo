package com.yourco.tp.registry;

import com.yourco.tp.model.PluginSpec;
import com.yourco.tp.model.ProgramSpec;

import java.util.ArrayList;
import java.util.List;

public class RegistryCatalog {
    private List<PluginSpec> plugins = new ArrayList<>();
    private List<ProgramSpec> programs = new ArrayList<>();

    public List<PluginSpec> getPlugins() { return plugins; }
    public void setPlugins(List<PluginSpec> plugins) { this.plugins = plugins; }
    public List<ProgramSpec> getPrograms() { return programs; }
    public void setPrograms(List<ProgramSpec> programs) { this.programs = programs; }
}

