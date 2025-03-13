package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.model.CurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.CompareCurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service("dbc-ConfigServiceRBACImpl")
public class ConfigServiceRBACImpl implements ConfigService {

    @Autowired
    private SpringContext context;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    @Qualifier("dbc-ConfigServiceImpl")
    private ConfigService target;

    @Override
    public PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize) {
        String currentUserName=UserContext.get().getUserName();
        if(UserContext.get().isAdmin() || UserContext.permitAllow(q.getUsername()) && serviceDao.querySelectCountByUser(q.getServiceId(),currentUserName)>0){
        }else{
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        return target.queryConfigProfileList(q, pageNum, pageSize);
    }

    @Override
    public void saveConfig(AccountServiceProfile asp, ConfigEntity config) {
        rbac(asp);
        target.saveConfig(asp, config);
    }

    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        rbac(asp);
        target.updateConfig(asp, config);
    }

    @Override
    public void batchUpdateConfig(List<ServiceConfigEntity> rows) {
        for (ServiceConfigEntity row : rows) {
            if(!UserContext.permitAllow(row.getUsername())){
                throw new UnsupportedOperationException(I18nUtils.getMessage(context, "ConfigServiceImpl.batchUpdateConfig.msg2"));
            }
        }
        target.batchUpdateConfig(rows);
    }

    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        rbac(asp);
        target.deleteConfig(asp, q);
    }

    @Override
    public void batchDeleteConfig(List<ServiceConfigEntity> rows) {
        for (ServiceConfigEntity row : rows) {
            if(!UserContext.permitAllow(row.getUsername())){
                throw new UnsupportedOperationException(I18nUtils.getMessage(context, "ConfigServiceImpl.batchDeleteConfig.msg1"));
            }
        }
        target.batchDeleteConfig(rows);
    }

    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
        rbac(dcm.getAsp());
        target.deleteConfigMultiple(dcm);
    }

    @Override
    public PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize) {
        rbac(asp);
        return target.queryConfigList(asp, q, pageNum, pageSize);
    }

    @Override
    public void configImport(AccountServiceProfile asp, MultipartFile file, String cover, String backup) {
        rbac(asp);
        target.configImport(asp, file, cover, backup);
    }

    @Override
    public ConfigFileEntity queryConfigFile(AccountServiceProfile asp) {
        rbac(asp);
        return target.queryConfigFile(asp);
    }

    @Override
    public void updateConfigFile(ConfigFileEntity cfe) {
        String currentUserName = UserContext.get().getUserName();
        if (UserContext.get().isAdmin() || UserContext.permitAllow(cfe.getUsername())
                && serviceDao.querySelectCountByUser(cfe.getServiceId(), currentUserName) > 0) {
        }else{
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        target.updateConfigFile(cfe);
    }

    @Override
    public void downFile(HttpServletResponse response, DownFile downFile) {
        target.downFile(response, downFile);
    }

    @Override
    public void downBootstrapFile(HttpServletResponse response, Map map) {
        target.downBootstrapFile(response, map);
    }

    @Override
    public List<ServiceConfigEntity> queryAllProfilesThisConfig(Map map) {
        return target.queryAllProfilesThisConfig(map);
    }

    @Override
    public void backup(AccountServiceProfile asp) {
        rbac(asp);
        target.backup(asp);
    }

    @Override
    public CurrentAndBackupFile getCurrentAndBackupFile(CompareCurrentAndBackupFile ccabf) {
        rbac(ccabf.getCurrent());
        return target.getCurrentAndBackupFile(ccabf);
    }

    private void rbac(AccountServiceProfile asp) {
        String currentUserName=UserContext.get().getUserName();
        if(UserContext.get().isAdmin() || UserContext.permitAllow(asp.getUsername()) && serviceDao.querySelectCountByUser(asp.getServiceId(),currentUserName)>0){
        }else{
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }
}
