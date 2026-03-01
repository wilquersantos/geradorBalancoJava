package com.emissor.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

public class ConfigurationManager {

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String DEFAULT_PORT = "8084";
    private static final String DEFAULT_COLETA = "GERAL";

    private final Properties properties;
    private final Path configFilePath;

    public ConfigurationManager() {
        this.properties = new Properties();
        String userDir = System.getProperty("user.dir");
        this.configFilePath = Paths.get(userDir, CONFIG_FILE_NAME);
        loadConfiguration();
    }

    private void loadConfiguration() {
        if (Files.exists(configFilePath)) {
            try (InputStream input = new FileInputStream(configFilePath.toFile())) {
                properties.load(input);
            } catch (IOException e) {
                setDefaultValues();
            }
        } else {
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        properties.setProperty("server.port", DEFAULT_PORT);
        properties.setProperty("server.ip", detectLocalIp());
        properties.setProperty("ui.auto_update.enabled", "false");
        properties.setProperty("ui.auto_update.interval", "5");
        properties.setProperty("ui.selected_coleta", DEFAULT_COLETA);
    }

    public void saveConfiguration() throws IOException {
        try (OutputStream output = new FileOutputStream(configFilePath.toFile())) {
            properties.store(output, "Emissor Java - Configuracoes do Aplicativo");
        }
    }

    public int getServerPort() {
        String port = properties.getProperty("server.port", DEFAULT_PORT);
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULT_PORT);
        }
    }

    public void setServerPort(int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Porta deve estar entre 1024 e 65535");
        }
        properties.setProperty("server.port", String.valueOf(port));
    }

    public String getServerIp() {
        return properties.getProperty("server.ip", detectLocalIp());
    }

    public void refreshServerIp() {
        properties.setProperty("server.ip", detectLocalIp());
    }

    public boolean isAutoUpdateEnabled() {
        return Boolean.parseBoolean(properties.getProperty("ui.auto_update.enabled", "false"));
    }

    public void setAutoUpdateEnabled(boolean enabled) {
        properties.setProperty("ui.auto_update.enabled", String.valueOf(enabled));
    }

    public int getAutoUpdateInterval() {
        String interval = properties.getProperty("ui.auto_update.interval", "5");
        try {
            return Math.max(1, Integer.parseInt(interval));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public void setAutoUpdateInterval(int interval) {
        properties.setProperty("ui.auto_update.interval", String.valueOf(Math.max(1, interval)));
    }

    public String getSelectedColeta() {
        String coleta = properties.getProperty("ui.selected_coleta", DEFAULT_COLETA);
        if (coleta == null || coleta.trim().isEmpty()) {
            return DEFAULT_COLETA;
        }
        return coleta.trim();
    }

    public void setSelectedColeta(String coleta) {
        if (coleta == null || coleta.trim().isEmpty()) {
            properties.setProperty("ui.selected_coleta", DEFAULT_COLETA);
            return;
        }
        properties.setProperty("ui.selected_coleta", coleta.trim());
    }

    public static String detectLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String host = addr.getHostAddress();
                        if (host != null && !host.isBlank()) {
                            return host;
                        }
                    }
                }
            }
        } catch (SocketException ignored) {
        }

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String host = localHost.getHostAddress();
            if (host != null && !host.isBlank()) {
                return host;
            }
        } catch (UnknownHostException ignored) {
        }

        return "Nao detectado";
    }
}
