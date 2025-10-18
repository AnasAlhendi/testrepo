package com.yourco.tp.model;

public class Source {
    // One of maven|url|path
    private String maven;
    private String url;
    private String path;
    private String sha256; // optional checksum for url/path sources

    public String getMaven() { return maven; }
    public void setMaven(String maven) { this.maven = maven; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
}

