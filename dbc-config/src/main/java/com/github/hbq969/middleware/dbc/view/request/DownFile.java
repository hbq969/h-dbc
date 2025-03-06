package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class DownFile {
    private String app;
    private String serviceId;
    private String username;
    private String profileName;
    private String fileSuffix;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        if (StringUtils.isEmpty(this.username)) {
            throw new IllegalArgumentException(I18nUtils.getMessage(context, "DownFile.message1"));
        }
        if (StringUtils.isEmpty(this.serviceId)) {
            throw new IllegalArgumentException(I18nUtils.getMessage(context, "DownFile.message2"));
        }
    }

    public AccountServiceProfile propertySet() {
        AccountServiceProfile asp = new AccountServiceProfile();
        asp.setUsername(this.username);
        asp.setServiceId(this.serviceId);
        asp.setProfileName(this.profileName);
        asp.setApp(this.app);
        return asp;
    }

    public String getFilename() {
        if (StringUtils.isEmpty(this.profileName) || StringUtils.equals("default", this.profileName)) {
            return String.join(".", "application", fileSuffix);
        } else {
            return String.join("", "application", "-", this.profileName, fileSuffix);
        }
    }
}
