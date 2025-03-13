package com.github.hbq969.middleware.dbc.model;

import lombok.Data;

@Data
public class APIModel {
    private String app;
    private String username;
    private String serviceName;
    private String profileName;
    private ConfigType type = ConfigType.PROP;

    public boolean isProp(){
        return ConfigType.PROP.equals(type);
    }

    public enum ConfigType {
        PROP, YAML
    }
}
