package com.github.hbq969.middleware.dbc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ConfigFileEntity implements DictModel, DictAware {
    private String app;
    private String username;
    private String serviceId;
    private String profileName;
    private String fileName;
    private String fileContent;
    private Long createdAt;
    private Long updatedAt;
    private String fmtCreatedAt;
    private String fmtUpdatedAt;
    private String backup;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        if (!UserContext.get().isAdmin()) {
            this.username = UserContext.get().getUserName();
        } else if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("账号名不能为空");
        }
    }

    public ConfigFileEntity propertySet(AccountServiceProfile asp) {
        this.app = asp.getApp();
        this.username = asp.getUsername();
        this.serviceId = asp.getServiceId();
        this.profileName = asp.getProfileName();
        return this;
    }

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if (createdAt != null) {
            this.fmtCreatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(createdAt.longValue());
        }
        if (updatedAt != null) {
            this.fmtUpdatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(updatedAt.longValue());
        }
    }
}
