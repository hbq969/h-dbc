package com.github.hbq969.middleware.dbc.service.impl.cache;

import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.*;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.model.CurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.CompareCurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service("dbc-ConfigServiceCacheImpl")
public class ConfigServiceCacheImpl implements ConfigService {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    @Qualifier("dbc-ConfigServiceImpl")
    private ConfigService target;

    @Autowired
    private CacheService cacheService;

    @Override
    public PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize) {
        return this.target.queryConfigProfileList(q, pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveConfig(AccountServiceProfile asp, ConfigEntity config) {
        try {
            cacheService.clear(new APIModel().of(asp));
        } finally {
            this.target.saveConfig(asp, config);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        try {
            cacheService.clear(new APIModel().of(asp));
        } finally {
            this.target.updateConfig(asp, config);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateConfig(List<ServiceConfigEntity> rows) {
        try {
            if (CollectionUtils.isNotEmpty(rows)) {
                for (ServiceConfigEntity row : rows) {
                    APIModel model = new APIModel().of(row);
                    cacheService.clear(model);
                }
            }
        } finally {
            this.target.batchUpdateConfig(rows);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        try {
            cacheService.clear(new APIModel().of(asp));
        } finally {
            this.target.deleteConfig(asp, q);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteConfig(List<ServiceConfigEntity> rows) {
        try {
            if (CollectionUtils.isNotEmpty(rows)) {
                for (ServiceConfigEntity row : rows) {
                    cacheService.clear(new APIModel().of(row));
                }
            }
        } finally {
            this.target.batchDeleteConfig(rows);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
        try {
            cacheService.clear(new APIModel().of(dcm));
        } finally {
            this.target.deleteConfigMultiple(dcm);
        }
    }

    @Override
    public PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize) {
        return this.target.queryConfigList(asp, q, pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void configImport(AccountServiceProfile asp, MultipartFile file, String cover, String backup) {
        try {
            ServiceEntity service = serviceDao.queryService(asp.getServiceId());
            asp.setServiceName(service.getServiceName());
            cacheService.clear(new APIModel().of(asp));
        } finally {
            this.target.configImport(asp, file, cover, backup);
        }
    }

    @Override
    public ConfigFileEntity queryConfigFile(AccountServiceProfile asp) {
        return this.target.queryConfigFile(asp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateConfigFile(ConfigFileEntity cfe) {
        try {
            ServiceEntity service = serviceDao.queryService(cfe.getServiceId());
            APIModel model = new APIModel();
            model.setProfileName(cfe.getProfileName());
            model.setServiceName(service.getServiceName());
            cacheService.clear(model);
        } finally {
            this.target.updateConfigFile(cfe);
        }
    }

    @Override
    public void downFile(HttpServletResponse response, DownFile downFile) {
        this.target.downFile(response, downFile);
    }

    @Override
    public void downBootstrapFile(HttpServletResponse response, Map map) {
        this.target.downBootstrapFile(response, map);
    }

    @Override
    public List<ServiceConfigEntity> queryAllProfilesThisConfig(Map map) {
        return this.target.queryAllProfilesThisConfig(map);
    }

    @Override
    public void backup(AccountServiceProfile asp) {
        this.target.backup(asp);
    }

    @Override
    public CurrentAndBackupFile getCurrentAndBackupFile(CompareCurrentAndBackupFile ccabf) {
        return this.target.getCurrentAndBackupFile(ccabf);
    }
}
