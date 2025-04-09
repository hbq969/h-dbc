package com.github.hbq969.middleware.dbc.service.impl.rbac;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbc-BackupServiceRBACImpl")
public class BackupServiceRBACImpl implements BackupService {

    @Autowired
    private SpringContext context;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    @Qualifier("dbc-BackupProxyImpl")
    private BackupService target;

    @Override
    public void backupOnDeleteProfile(ProfileEntity profile) {
        target.backupOnDeleteProfile(profile);
    }

    @Override
    public void backupOnDeleteService(ServiceEntity service) {
        target.backupOnDeleteService(service);
    }

    @Override
    public void backupOnClearProfileConfig(AccountServiceProfile asp) {
        target.backupOnClearProfileConfig(asp);
    }

    @Override
    public void backupOnConfigImport(AccountServiceProfile asp) {
        target.backupOnConfigImport(asp);
    }

    @Override
    public void backupOnUpdateConfigFile(ConfigFileEntity file) {
        target.backupOnUpdateConfigFile(file);
    }

    @Override
    public PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize) {
        return target.queryBackupList(asp, pageNum, pageSize);
    }

    @Override
    public void deleteBackup(BackupEntity bk) {
        rbac(bk);
        target.deleteBackup(bk);
    }

    @Override
    public void deleteBackups(BatchDeleteBackup bdb) {
        if (!UserContext.permitAllow(bdb.getUsername())) {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        for (BackupEntity backup : bdb.getBackups()) {
            if (!UserContext.get().isAdmin() && !UserContext.sameUser(backup.getUsername())) {
                throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.deleteBackups.msg1"));
            }
        }
        target.deleteBackups(bdb);
    }

    @Override
    public void recoveryBackup(BackupEntity bk) {
        rbac(bk);
        target.recoveryBackup(bk);
    }

    @Override
    public void recoveryBackups(BatchDeleteRecovery bdr) {
        if (!UserContext.permitAllow(bdr.getUsername())) {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        target.recoveryBackups(bdr);
    }

    @Override
    public List<ProfileEntity> queryProfileList() {
        return target.queryProfileList();
    }

    private void rbac(BackupEntity bk) {
        String currentUserName = UserContext.get().getUserName();
        if (UserContext.get().isAdmin() || UserContext.permitAllow(bk.getUsername())
                && serviceDao.querySelectCountByUser(bk.getServiceId(), currentUserName) > 0) {
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }
}
