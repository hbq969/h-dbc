package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConfigService {
    PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize);

    @Transactional(rollbackFor = Exception.class)
    void saveConfig(AccountServiceProfile asp, ConfigEntity config);

    @Transactional(rollbackFor = Exception.class)
    void updateConfig(AccountServiceProfile asp, ConfigEntity config);

    @Transactional(rollbackFor = Exception.class)
    void deleteConfig(AccountServiceProfile asp, ConfigEntity q);

    @Transactional(rollbackFor = Exception.class)
    void deleteConfigMultiple(DeleteConfigMultiple dcm);

    PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize);

    @Transactional(rollbackFor = Exception.class)
    void configImport(AccountServiceProfile asp, MultipartFile file, String cover);

    ConfigFileEntity queryConfigFile(AccountServiceProfile asp);

    void updateConfigFile(ConfigFileEntity cfe);
}
