package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ConfigProfileQuery {
    private String app;
    private String username;
    private String serviceId;
    private String profileName;
    private String profileDesc;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        if (StringUtils.isEmpty(this.username)) {
            throw new IllegalArgumentException(I18nUtils.getMessage(context, "ConfigProfileQuery.message1"));
        }
        if (StringUtils.isEmpty(serviceId)) {
            throw new IllegalArgumentException(I18nUtils.getMessage(context, "ConfigProfileQuery.message2"));
        }
    }
}
