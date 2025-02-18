package com.github.hbq969.middleware.dbc.model;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Data
public class AccountServiceProfile {
    private String app;
    private String username;
    private String serviceId;
    private String serviceName;
    private String serviceDesc;
    private String profileName;
    private String profileDesc;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("账号名不能为空");
        }
    }

    public AccountServiceProfile propertySet(ConfigFileEntity cfe) {
        this.app = cfe.getApp();
        this.username = cfe.getUsername();
        this.serviceId = cfe.getServiceId();
        this.profileName = cfe.getProfileName();
        return this;
    }

    public String getFilename() {
        if (StringUtils.isEmpty(this.profileName) || StringUtils.equals("default", this.profileName)) {
            return String.join(".", "application", "yml");
        } else {
            return String.join("", "application", "-", this.profileName, "yml");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AccountServiceProfile that = (AccountServiceProfile) object;
        return Objects.equals(app, that.app)
                && Objects.equals(username, that.username)
                && Objects.equals(serviceId, that.serviceId)
                && Objects.equals(profileName, that.profileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, username, serviceId, profileName);
    }
}
