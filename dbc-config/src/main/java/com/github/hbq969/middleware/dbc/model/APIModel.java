package com.github.hbq969.middleware.dbc.model;

import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.view.request.ConfigQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.google.common.base.Joiner;
import lombok.Data;

@Data
public class APIModel {
    private String app;
    private String username;
    private String serviceName;
    private String profileName;
    private ConfigType type = ConfigType.PROP;

    public boolean isProp() {
        return ConfigType.PROP.equals(type);
    }

    public enum ConfigType {
        PROP, YAML
    }

    public String key() {
        return Joiner.on(",").join(serviceName, profileName, type.name());
    }

    public APIModel of(BackupEntity be) {
        if (be == null)
            return this;
        this.serviceName = be.getServiceName();
        this.profileName = be.getProfileName();
        return this;
    }

    public APIModel of(ConfigQuery cq) {
        if (cq == null)
            return this;
        AccountServiceProfile asp = cq.getAsp();
        if (asp == null)
            return this;
        this.serviceName = asp.getServiceName();
        this.profileName = asp.getProfileName();
        return this;
    }

    public APIModel of(ServiceConfigEntity sce) {
        if (sce == null)
            return this;
        this.serviceName = sce.getServiceName();
        this.profileName = sce.getProfileName();
        return this;
    }

    public APIModel of(DeleteConfigMultiple dcm) {
        if (dcm == null)
            return this;
        AccountServiceProfile asp = dcm.getAsp();
        if (asp == null)
            return this;
        this.serviceName = asp.getServiceName();
        this.profileName = asp.getProfileName();
        return this;
    }

    public APIModel of(AccountServiceProfile asp) {
        if (asp == null)
            return this;
        this.serviceName = asp.getServiceName();
        this.profileName = asp.getProfileName();
        return this;
    }

    public APIModel of(ConfigFileEntity cfe) {
        if (cfe == null)
            return this;
        this.serviceName = null;
        this.profileName = cfe.getProfileName();
        return this;
    }
}
