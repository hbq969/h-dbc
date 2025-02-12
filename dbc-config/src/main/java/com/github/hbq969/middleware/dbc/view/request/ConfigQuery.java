package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import lombok.Data;

@Data
public class ConfigQuery {
    private AccountServiceProfile asp;
    private ConfigEntity config;
}
