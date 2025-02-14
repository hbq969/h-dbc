package com.github.hbq969.middleware.dbc.model;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AccountServiceProfile {
    private String app;
    private String username;
    private String serviceId;
    private String profileName;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        if (!UserContext.get().isAdmin()) {
            this.username = UserContext.get().getUserName();
        } else if (StringUtils.isEmpty(username)) {
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
}
