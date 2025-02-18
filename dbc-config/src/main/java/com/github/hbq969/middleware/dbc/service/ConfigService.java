package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ConfigService {
    PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize);

    @Transactional(rollbackFor = Exception.class)
    void saveConfig(AccountServiceProfile asp, ConfigEntity config);

    @Transactional(rollbackFor = Exception.class)
    void updateConfig(AccountServiceProfile asp, ConfigEntity config);

    @Transactional(rollbackFor = Exception.class)
    void batchUpdateConfig(List<ServiceConfigEntity> rows);

    @Transactional(rollbackFor = Exception.class)
    void deleteConfig(AccountServiceProfile asp, ConfigEntity q);

    @Transactional(rollbackFor = Exception.class)
    void batchDeleteConfig(List<ServiceConfigEntity> rows);

    @Transactional(rollbackFor = Exception.class)
    void deleteConfigMultiple(DeleteConfigMultiple dcm);

    PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize);

    @Transactional(rollbackFor = Exception.class)
    void configImport(AccountServiceProfile asp, MultipartFile file, String cover,String backup);

    ConfigFileEntity queryConfigFile(AccountServiceProfile asp);

    void updateConfigFile(ConfigFileEntity cfe);

    void downFile(HttpServletResponse response, DownFile downFile);

    List<ServiceConfigEntity> queryAllProfilesThisConfig(Map map);

    void backup(AccountServiceProfile asp);
}
