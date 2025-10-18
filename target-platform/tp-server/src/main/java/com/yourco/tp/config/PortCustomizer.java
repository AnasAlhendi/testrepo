package com.yourco.tp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Component
public class PortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    private static final Logger log = LoggerFactory.getLogger(PortCustomizer.class);
    private final Environment env;

    public PortCustomizer(Environment env) {
        this.env = env;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        int preferred = getInt("server.port", 19080);
        int maxAttempts = getInt("tp.port.maxAttempts", 10);
        String addrProp = env.getProperty("server.address");
        InetAddress bindAddress = null;
        try {
            if (addrProp != null && !addrProp.isBlank()) {
                bindAddress = InetAddress.getByName(addrProp);
                factory.setAddress(bindAddress);
            }
        } catch (Exception ignored) {}

        int candidate = preferred;
        boolean selected = false;
        for (int i = 0; i <= maxAttempts; i++) {
            if (isPortAvailable(candidate, bindAddress)) {
                if (candidate != preferred) {
                    log.warn("Preferred port {} is in use; switching to {}", preferred, candidate);
                } else {
                    log.info("Using configured port {}", candidate);
                }
                factory.setPort(candidate);
                selected = true;
                break;
            }
            candidate++;
        }
        if (!selected) {
            log.error("All ports from {} to {} are in use; falling back to random port", preferred, preferred + maxAttempts);
            factory.setPort(0); // random
        }
    }

    private int getInt(String key, int def) {
        try {
            String v = env.getProperty(key);
            return v == null ? def : Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    private boolean isPortAvailable(int port, InetAddress addr) {
        // Try TCP and UDP bind
        try (ServerSocket ss = new ServerSocket()) {
            ss.setReuseAddress(true);
            ss.bind(addr == null ? new InetSocketAddress(port) : new InetSocketAddress(addr, port));
        } catch (Exception e) {
            return false;
        }
        try (DatagramSocket ds = new DatagramSocket(null)) {
            ds.setReuseAddress(true);
            ds.bind(addr == null ? new InetSocketAddress(port) : new InetSocketAddress(addr, port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

