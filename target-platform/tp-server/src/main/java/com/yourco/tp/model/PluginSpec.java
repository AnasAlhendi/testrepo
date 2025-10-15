package com.yourco.tp.model;

public class PluginSpec {
    private String id;
    private String version;
    private Source source;
    private boolean verifySignature;
    private String status = "stopped"; // installed|started|stopped

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public boolean isVerifySignature() { return verifySignature; }
    public void setVerifySignature(boolean verifySignature) { this.verifySignature = verifySignature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
