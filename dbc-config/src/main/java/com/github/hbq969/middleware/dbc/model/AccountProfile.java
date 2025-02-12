package com.github.hbq969.middleware.dbc.model;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.sm.login.session.UserContext;
import lombok.Data;

@Data
public class AccountProfile {
    private String app;
    private String username;
    private String profileName;

    public void userInitial(SpringContext context) {
        this.app = context.getProperty("spring.application.name");
        this.username = UserContext.get().getUserName();
    }
}
