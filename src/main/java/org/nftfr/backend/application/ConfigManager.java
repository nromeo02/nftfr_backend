package org.nftfr.backend.application;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class ConfigManager {
    private record ConfigData(String dbPort, String dbName, String dbUsername, String dbPassword, String jwtSecret, String nftImagePath) {}
    static private ConfigManager instance = null;
    private final ConfigData configData;

    private ConfigManager() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nftfr_config.json");

        try {
             configData = mapper.readValue(inputStream, ConfigData.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null)
            instance = new ConfigManager();

        return instance;
    }

    public String getDBPort() { return configData.dbPort(); }
    public String getDBName() { return configData.dbName(); }
    public String getDBUsername() { return configData.dbUsername(); }
    public String getDBPassword() { return configData.dbPassword(); }
    public String getJwtSecret() { return configData.jwtSecret(); }
    public String getNftImagePath() { return configData.nftImagePath(); }
}