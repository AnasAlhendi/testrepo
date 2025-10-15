package com.yourco.tp.model;

public class HealthCheck {
    private String http;
    private int timeoutSec = 30;

    public String getHttp() { return http; }
    public void setHttp(String http) { this.http = http; }
    public int getTimeoutSec() { return timeoutSec; }
    public void setTimeoutSec(int timeoutSec) { this.timeoutSec = timeoutSec; }
}

