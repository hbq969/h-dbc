package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.code.common.spring.context.SpringContext;
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
            throw new IllegalArgumentException("账号名不能为空");
        }
        if (StringUtils.isEmpty(serviceId)) {
            throw new IllegalArgumentException("服务id不能为空");
        }
    }
}
