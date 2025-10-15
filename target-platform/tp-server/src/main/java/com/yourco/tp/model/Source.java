package com.yourco.tp.model;

public class Source {
    // One of maven|url|path
    private String maven;
    private String url;
    private String path;

    public String getMaven() { return maven; }
    public void setMaven(String maven) { this.maven = maven; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

