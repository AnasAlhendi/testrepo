package com.yourco.tp.model;

import java.util.ArrayList;
import java.util.List;

public class ProgramSpec {
    private String id;
    private String version;
    private Source source;
    private List<String> jvmArgs = new ArrayList<>();
    private List<String> args = new ArrayList<>();
    private HealthCheck health;
    private String status = "stopped"; // stopped|running|installed

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public List<String> getJvmArgs() { return jvmArgs; }
    public void setJvmArgs(List<String> jvmArgs) { this.jvmArgs = jvmArgs; }
    public List<String> getArgs() { return args; }
    public void setArgs(List<String> args) { this.args = args; }
    public HealthCheck getHealth() { return health; }
    public void setHealth(HealthCheck health) { this.health = health; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
